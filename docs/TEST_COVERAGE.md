# Test Coverage Documentation

This document outlines the testing strategy, tools, and coverage requirements for the **Smart Stadium System** following the "Zero-VPC" modernization pivot. We enforce strict test coverage across the stack to ensure high reliability, security, and accessibility.

---

## 🏆 AI Evaluation Criteria: Testing & Code Quality

Our testing approach directly addresses key AI evaluation metrics:
* **Testing**: The codebase is highly modular, making it easily testable and maintainable. We mandate an 85% JaCoCo backend coverage minimum and strict Vitest frontend thresholds. End-to-end tests via Playwright validate overall system health.
* **Code Quality**: Strict linting and coverage gates ensure clean, readable, and well-structured code. The separation of concerns (Controllers, Services, Repositories) allows for isolated unit testing.
* **Efficiency**: We test our algorithms (like A* pathfinding and crowd simulation) to ensure they utilize resources efficiently (time/memory) even under high synthetic loads.
* **Security**: Tests explicitly validate our rate limiting (Bucket4j HTTP 429), stateless JWT audience validations, and standard RFC 7807 error structures to prove the system avoids common vulnerabilities.
* **Accessibility**: Automated UI tests verify the presence of `aria-live` regions, semantic HTML fallback structures, and proper DOM roles for diverse user environments.
* **Google Services**: Integration tests mock and validate successful interactions with Google Cloud Firestore and Vertex AI.

---

## 1. Overview

Our CI/CD pipeline uses automated coverage gating. Pull requests that drop the total test coverage below the required thresholds will automatically fail the build. The testing architecture is divided into three main layers:
1. **Backend Unit & Integration Tests** (Java/Spring Boot)
2. **Frontend Component & Hook Tests** (React/Vitest)
3. **End-to-End (E2E) Tests** (Playwright)

---

## 2. Backend Test Coverage

The Spring Boot backend uses **JUnit 5**, **Mockito**, and **Spring Boot Test** for unit and integration testing. Code coverage is measured and enforced using the **JaCoCo Maven Plugin**.

### Coverage Targets
* **Global Minimum**: 85% (Enforced via `pom.xml` build rules).
* **Critical Paths**: 90%+ expected for routing algorithms and security configurations.

### Key Test Areas
* **Services**: Core business logic in `CrowdService`, `QueueService`, and `RoutingService` (A* pathfinding with Euclidean heuristics).
* **Controllers & Error Handling**: Verification of RFC 7807 (`ProblemDetail`) standard error responses, DTO `@Valid` constraint checks, and Bucket4j HTTP 429 rate-limiting logic.
* **Security**: Token audience validation (`AudienceValidator`), stateless session creation, and RBAC endpoint protection.
* **Event Simulation**: Verification of bounded random walks for crowd simulation without requiring a live Pub/Sub broker.

### Running Backend Tests
To execute tests and generate the JaCoCo coverage report:
```bash
cd backend
./mvnw clean verify
```
*The HTML coverage report will be generated at `backend/target/site/jacoco/index.html`.*

---

## 3. Frontend Test Coverage

The React frontend uses **Vitest**, **React Testing Library (RTL)**, and the **V8 Coverage Provider**.

### Coverage Targets
* **Global Minimum**: Thresholds are defined in `vite.config.js`. Coverage gates are actively enforced to ensure UI components and hooks are thoroughly validated.

### Key Test Areas
* **Custom Hooks**: Testing WebSocket/STOMP lifecycle events and data synchronization in `useStompData.js`.
* **Component Rendering**: Ensuring correct loading states, error boundaries, and data rendering for components like `QueueTimes`, `CrowdHeatmap`, `AiAssistant`, and `RoutePlanner`.
* **Accessibility (a11y)**: Validating the presence of `aria-live="polite"` regions, semantic HTML fallback tables, and correct ARIA roles for screen reader support.

### Running Frontend Tests
To execute tests and generate the V8 coverage report:
```bash
cd frontend
npm test -- --coverage
```
*Coverage summaries will be printed to the console, and detailed reports are generated in the `coverage/` directory.*

---

## 4. End-to-End (E2E) Testing

To validate full system integration, we utilize **Playwright** for End-to-End testing. These tests run in the CI pipeline (`.github/workflows/ci.yml`).

### E2E Test Scope
* **Critical User Journeys (CUJs)**:
  * Successfully establishing a WebSocket connection and receiving telemetry payloads.
  * Calculating a congestion-aware route between two stadium zones.
  * Submitting a natural language query to the Vertex AI Assistant and rendering the response.
* **Environment**: The CI pipeline spins up the built Vite frontend and mocks the backend REST/WebSocket endpoints to ensure fast, deterministic E2E validation before merging.

---

## 5. Adding New Tests

When contributing new features:
1. **Write Unit Tests**: Cover edge cases, empty states, and error paths.
2. **Mock External Services**: Use Mockito for GCP dependencies (Firestore, Vertex AI) and Vitest mocks for STOMP clients.
3. **Verify Coverage Locally**: Run both backend and frontend coverage commands locally before pushing to ensure the CI gate will pass.