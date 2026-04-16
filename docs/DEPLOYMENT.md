# Deployment Guide

Step-by-step instructions for deploying the Smart Stadium System using Docker and Google Cloud Run.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Configuration](#environment-configuration)
3. [Docker Build](#docker-build)
4. [Local Docker Compose](#local-docker-compose)
5. [Google Cloud Setup](#google-cloud-setup)
6. [Cloud Run Deployment](#cloud-run-deployment)
7. [Firestore Setup](#firestore-setup)
8. [Memorystore Redis Setup](#memorystore-redis-setup)
9. [VPC Access Connector](#vpc-access-connector)
10. [Pub/Sub Setup](#pubsub-setup)
11. [Verification](#verification)

---

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) 20+
- [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) (`gcloud`)
- A GCP project with billing enabled (project ID: `promptwars-493516`)

---

## Environment Configuration

The application uses environment variables for all configuration. **No secrets are hardcoded.**

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | Server port (Cloud Run sets this automatically) |
| `GCP_PROJECT_ID` | `promptwars-493516` | Google Cloud project ID |
| `FRONTEND_URL` | `http://localhost:5173` | Allowed CORS origin |
| `SPRING_PROFILES_ACTIVE` | *(none)* | Set to `cloud` for GCP integration |
| `SPRING_REDIS_HOST` | `localhost` | Redis server address |
| `SPRING_REDIS_PORT` | `6379` | Redis server port |
| `PUBSUB_ENABLED` | `false` | Enable Pub/Sub messaging |
| `GOOGLE_APPLICATION_CREDENTIALS` | *(auto on Cloud Run)* | Path to service account key (local only) |

---

## Docker Build

### Backend

```bash
cd backend
docker build -t smart-stadium-backend .
```

### Frontend

```bash
cd frontend
docker build -t smart-stadium-frontend .
```

---

## Local Docker Compose

Run the full stack locally:

```bash
docker-compose up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Health Check | http://localhost:8080/actuator/health |

Stop:

```bash
docker-compose down
```

---

## Google Cloud Setup

### 1. Authenticate

```bash
gcloud auth login
gcloud config set project promptwars-493516
```

### 2. Enable Required APIs

```bash
gcloud services enable \
  run.googleapis.com \
  firestore.googleapis.com \
  pubsub.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com
```

### 3. Create Artifact Registry Repository

```bash
gcloud artifacts repositories create smart-stadium \
  --repository-format=docker \
  --location=us-central1 \
  --description="Smart Stadium container images"
```

### 4. Configure Docker Authentication

```bash
gcloud auth configure-docker us-central1-docker.pkg.dev
```

---

## Cloud Run Deployment

### Deploy Backend

```bash
# Build and push image
cd backend
docker build -t us-central1-docker.pkg.dev/promptwars-493516/smart-stadium/backend:latest .
docker push us-central1-docker.pkg.dev/promptwars-493516/smart-stadium/backend:latest

# Deploy to Cloud Run
gcloud run deploy smart-stadium-backend \
  --image=us-central1-docker.pkg.dev/promptwars-493516/smart-stadium/backend:latest \
  --platform=managed \
  --region=us-central1 \
  --allow-unauthenticated \
  --memory=512Mi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=3 \
  --vpc-connector=stadium-connector \
  --set-env-vars="SPRING_PROFILES_ACTIVE=cloud,GCP_PROJECT_ID=promptwars-493516,PUBSUB_ENABLED=true,SPRING_REDIS_HOST=YOUR_REDIS_IP"
```

Note the backend URL from the output (e.g., `https://smart-stadium-backend-xxxxx.run.app`).

### Deploy Frontend

```bash
# Build with backend URL
cd frontend
docker build -t us-central1-docker.pkg.dev/promptwars-493516/smart-stadium/frontend:latest .
docker push us-central1-docker.pkg.dev/promptwars-493516/smart-stadium/frontend:latest

# Deploy to Cloud Run
gcloud run deploy smart-stadium-frontend \
  --image=us-central1-docker.pkg.dev/promptwars-493516/smart-stadium/frontend:latest \
  --platform=managed \
  --region=us-central1 \
  --allow-unauthenticated \
  --port=80 \
  --memory=256Mi \
  --cpu=1
```

### Update Backend CORS

After deploying the frontend, update the backend's CORS configuration:

```bash
gcloud run services update smart-stadium-backend \
  --region=us-central1 \
  --set-env-vars="FRONTEND_URL=https://smart-stadium-frontend-xxxxx.run.app"
```

---

## Firestore Setup

### 1. Create Firestore Database

```bash
gcloud firestore databases create --location=us-central1
```

### 2. Collections

The application automatically creates two collections on startup:

| Collection | Documents | Fields |
|---|---|---|
| `crowd_data` | One per zone (e.g., `GATE_A`) | `zone`, `currentCount`, `capacity`, `densityLevel`, `timestamp` |
| `queue_data` | One per zone | `zone`, `queueLength`, `avgServiceTimeSeconds`, `timestamp` |

No manual schema setup is required — Firestore is schemaless.

### 3. Security Rules

For production, restrict Firestore access to the backend service account:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if false;  // Only accessible via Admin SDK
    }
  }
}
```

---

## Pub/Sub Setup

### 1. Create Topic

```bash
gcloud pubsub topics create crowd-updates
```

### 2. Create Subscription

```bash
gcloud pubsub subscriptions create crowd-updates-sub \
  --topic=crowd-updates \
  --ack-deadline=30
```

### 3. IAM Permissions

Ensure the Cloud Run service account has `roles/pubsub.publisher` and `roles/pubsub.subscriber`:

```bash
SERVICE_ACCOUNT=$(gcloud run services describe smart-stadium-backend \
  --region=us-central1 --format='value(spec.template.spec.serviceAccountName)')

gcloud projects add-iam-policy-binding promptwars-493516 \
  --member="serviceAccount:${SERVICE_ACCOUNT}" \
  --role="roles/pubsub.editor"
```

---

## Verification

### 1. Health Check

```bash
curl https://smart-stadium-backend-xxxxx.run.app/actuator/health
# Expected: {"status":"UP"}
```

### 2. API Test

```bash
curl https://smart-stadium-backend-xxxxx.run.app/api/crowd-density
# Expected: JSON array of zone density data
```

### 3. Frontend

Open the frontend Cloud Run URL in a browser. You should see:
- Crowd density heatmap updating every 5 seconds
- Functional route planner
- Queue wait times

---

## Troubleshooting

| Issue | Solution |
|---|---|
| CORS errors in browser | Ensure `FRONTEND_URL` env var matches the frontend Cloud Run URL |
| Firestore permission denied | Verify the service account has `roles/datastore.user` |
| Cold start latency | Increase `--min-instances` or enable startup CPU boost |
| Pub/Sub not receiving | Check topic/subscription names and IAM permissions |
