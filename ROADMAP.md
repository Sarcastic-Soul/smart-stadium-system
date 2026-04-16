# Future Enhancements Roadmap

This document outlines the planned technical roadmap to elevate the Smart Stadium System across all major AI evaluation criteria: Code Quality, Security, Efficiency, Testing, Accessibility, and Google Services Integration.

---

## Phase 1: True AI Integration & GCP Mastery
**Goal:** Prove advanced Google Cloud capabilities and upgrade the "AI-status" of the application.

- [ ] **1.1 Vertex AI Assistant (Gemini)**
  - Add `spring-cloud-gcp-starter-vertexai` to the backend.
  - Create an `AiAssistantController` that accepts natural language constraints (e.g., "Find the nearest restroom with the shortest wait time").
  - Update the React frontend to include a chat interface floating action button (FAB).
- [ ] **1.2 Distributed Tracing & Observability**
  - Integrate `micrometer-tracing-bridge-otel` into `pom.xml`.
  - Export trace spans to Google Cloud Trace so request latency can be visualized in the GCP console.
  - Setup centralized logging pushing structured JSON logs to Cloud Logging.
- [ ] **1.3 Firebase Authentication (Security)**
  - Integrate React with Firebase Auth UI for user login.
  - Update Spring Boot with `spring-boot-starter-oauth2-resource-server` to validate Google/Firebase JWT tokens.
  - Protect sensitive API endpoints (e.g., admin simulation triggers) behind authentication.

---

## Phase 2: Performance & Efficiency Upgrades
**Goal:** Scale the system memory efficiency and reduce network overhead.

- [ ] **2.1 WebSockets for Real-Time Telemetry**
  - Replace the current 5-second HTTP polling in the React app.
  - Implement `spring-boot-starter-websocket` using STOMP.
  - Let the backend push updates via WebSocket whenever the `EventSimulationService` alters stadium states.
- [ ] **2.2 Distributed Caching (Cloud Memorystore)**
  - Replace the local `Caffeine` cache with `spring-boot-starter-data-redis`.
  - Connect the Cloud Run instances to a GCP Memorystore instance.
  - Ensures cache consistency if the backend auto-scales to multiple instances.

---

## Phase 3: Comprehensive Testing & CI/CD
**Goal:** Maximize confidence in code reliability and streamline deployment.

- [ ] **3.1 Frontend Automated Testing**
  - Install `Vitest` and `React Testing Library`.
  - Write component tests for `ZoneCard`, `RoutePlanner`, and API stubbing.
- [ ] **3.2 End-to-End (E2E) Browser Testing**
  - Install `Playwright`.
  - Write test scripts that launch a headless browser, navigate the stadium dashboard, execute route finds, and assert UI elements.
- [ ] **3.3 GitHub Actions Pipeline**
  - Create `.github/workflows/deploy.yml`.
  - Define stages: Maven tests → Docker Image Build → Push to Artifact Registry → Execute `gcloud run deploy`.

---

## Phase 4: Code Quality & Developer Experience
**Goal:** Deliver readable, accessible, and easily maintainable APIs.

- [ ] **4.1 Auto-Generated API Specifications**
  - Add `springdoc-openapi-starter-webmvc-ui` dependency.
  - Annotate controllers with `@Operation` and `@ApiResponses`.
  - Provide a `/swagger-ui.html` endpoint to allow visual API exploration.
- [ ] **4.2 Advanced Accessibility (A11y)**
  - Introduce an `aria-live` region in the React application that verbally announces the result of "Find Route" for screen reader dependent users.
  - Add a toggleable High-Contrast Theme (WCAG-AAA compliant) built with CSS variables.

---

## How to Contribute / Execute

To implement a feature from this roadmap:
1. Create a tracking issue or local branch for the specific task (e.g., `feature/vertex-ai-chat`).
2. Implement backend constraints first, securing them via unit tests.
3. Update the frontend logic to support the feature.
4. Verify by running the full stack locally via `docker-compose up --build`.
---
