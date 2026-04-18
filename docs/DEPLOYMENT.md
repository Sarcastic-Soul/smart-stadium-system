# Deployment Guide

This guide covers deploying the Smart Stadium System to Google Cloud Platform (GCP) using the modernized **Zero-VPC Architecture**.

By migrating distributed state management to Google Cloud Firestore and utilizing STOMP WebSockets, we have eliminated the need for Redis caching and Serverless VPC Access Connectors. This significantly reduces deployment complexity, latency, and infrastructure costs.

## Prerequisites

- A Google Cloud Project with billing enabled.
- Google Cloud CLI (`gcloud`) installed and authenticated.
- Node.js 18+ and Maven installed.

## 1. Enable Required GCP APIs

Run the following command to enable the necessary APIs for the architecture:

```bash
gcloud services enable \
  run.googleapis.com \
  firestore.googleapis.com \
  pubsub.googleapis.com \
  aiplatform.googleapis.com \
  cloudbuild.googleapis.com
```

## 2. Configure Firestore (Distributed State)

The system uses Firestore in Native mode for distributed, real-time state management instead of Redis.

1. Go to the **Firestore Console** in GCP.
2. Create a new database in **Native mode**.
3. Choose a region (e.g., `us-central1`).

## 3. Deploy the Backend (Cloud Run)

You can deploy the Spring Boot backend directly from the source code using Cloud Build. Cloud Run natively supports the WebSockets required by our STOMP configuration.

```bash
cd backend

gcloud run deploy smart-stadium-backend \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=cloud,GCP_PROJECT_ID=<YOUR_PROJECT_ID>,GCP_LOCATION=us-central1,smartstadium.vertexai.mock=false"
```

**Environment Variables Explained:**
* `SPRING_PROFILES_ACTIVE=cloud`: Disables the in-memory fallback and activates Firestore and Pub/Sub beans.
* `smartstadium.vertexai.mock=false`: Connects the AI Assistant to the live Gemini 2.5 Flash model instead of using local mock responses.

*Make sure to copy the deployed Service URL from the CLI output (e.g., `https://smart-stadium-backend-xxxxxx-uc.a.run.app`).*

## 4. Deploy the Frontend

Before deploying, ensure your frontend is configured to communicate with the deployed backend URL instead of `localhost`.

1. In `frontend/vite.config.js`, update your proxy targets or environment variables to point to the Cloud Run backend URL. (Make sure WebSocket proxying points to `wss://` instead of `ws://`).
2. Build the production assets:

```bash
cd frontend
npm install
npm run build
```

3. Deploy the compiled `dist/` directory.

### Option A: Firebase Hosting (Recommended)
Firebase Hosting is the fastest and most cost-effective way to host static React/Vite applications on GCP.

```bash
npm install -g firebase-tools
firebase login
firebase init hosting

# 1. Select your GCP project
# 2. Set the public directory to "dist"
# 3. Configure as a single-page app (Yes)

firebase deploy --only hosting
```

### Option B: Cloud Run
If you prefer to keep all services in Cloud Run, you can deploy the frontend using the provided Dockerfile:

```bash
gcloud run deploy smart-stadium-frontend \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

## 5. Security & IAM Permissions

If your backend experiences HTTP 403 Forbidden errors when attempting to access Firestore or Vertex AI, ensure that your Cloud Run service account (usually the Default Compute Service Account: `<PROJECT_NUMBER>-compute@developer.gserviceaccount.com`) has the correct IAM roles:

* **Cloud Datastore User** (Allows reading/writing to Firestore)
* **Vertex AI User** (Allows calling the Gemini API)
* **Pub/Sub Publisher & Subscriber** (Allows event simulation messaging)

```bash
# Example command to grant Vertex AI User role
gcloud projects add-iam-policy-binding <YOUR_PROJECT_ID> \
  --member="serviceAccount:<PROJECT_NUMBER>-compute@developer.gserviceaccount.com" \
  --role="roles/aiplatform.user"
```

## 6. Architecture Advantages (Zero-VPC)

* **No VPC Connector Required:** Because Firestore, Vertex AI, and Pub/Sub are accessed via standard Google HTTPS APIs, Cloud Run does not need to be bridged to a VPC. 
* **True Serverless Auto-Scaling:** The backend is completely stateless. It can safely scale from 0 to N instances without losing data or requiring session affinity (sticky sessions).
* **WebSocket Native:** Cloud Run supports concurrent WebSocket streams natively, allowing the STOMP telemetry push engine to operate with massive fan-out efficiency.