# Epic E - React Frontend Architecture

## Current integration points discovered

- API entry point is `api-gateway` on `http://localhost:8080`.
- Auth endpoints are under `/auth` (`/auth/login`, `/auth/logout`, `/auth/register/...`).
- Business APIs are under `/api/*` and consistently wrapped in `ApiResult<T>`.
- Pagination payload uses `PageResponse<T>` (`items`, `page`, `totalPages`, etc.).
- Role rules in gateway security:
  - `DELETE /api/**` requires `ROLE_ADMIN`.
  - `POST /api/instructors/**` and `POST /api/vehicles/**` require `ROLE_INSTRUCTOR` or `ROLE_ADMIN`.
  - `/api/students/**` requires `ROLE_STUDENT` or `ROLE_ADMIN`.

## Frontend structure added

- `frontend/` Vite + React + TypeScript app.
- API layer in `src/api.ts` with JWT bearer injection.
- Token store in `src/auth.ts`.
- Router with protected routes in `src/router.tsx`.
- Error handling:
  - Route fallback `404`.
  - UI `ErrorBoundary` for runtime `500`.
- Initial list page wired to backend: `StudentsPage`.
- Stub modules prepared for: instructors, vehicles, courses, lessons, payments, maintenances.

## Environment strategy (local + cloud ready)

- `VITE_API_BASE_URL` controls backend address:
  - local dev: empty (`""`) and Vite proxy forwards `/api` + `/auth` to gateway.
  - production: set to gateway public URL (Ingress host or reverse proxy path).
- This avoids hardcoded service URLs in browser and keeps frontend aligned with API Gateway.

## Azure + Minikube readiness

- Frontend should be deployed as a separate container (Nginx static serving).
- For both Minikube and AKS:
  - frontend communicates only with gateway ingress host/path.
  - no direct browser calls to internal microservice DNS names.
- Recommended ingress shape:
  - `/` -> frontend service
  - `/api` + `/auth` -> gateway service
- Build-time env for AKS:
  - `VITE_API_BASE_URL=https://<gateway-or-shared-host>`
- Build-time env for Minikube:
  - `VITE_API_BASE_URL=` with ingress/rewrite or `http://<minikube-ip-or-tunnel>`

## Next implementation steps

1. CRUD forms for all entities with client-side validation.
2. Generic error mapper for 400/404/409/500 using `ApiResult.errorCode`.
3. Role-aware navigation and action guards (hide forbidden actions in UI).
4. Reusable table/form components for pagination + sorting controls.
5. Frontend Dockerfile + K8s manifests (`k8s/base`, `overlays/minikube`, `overlays/azure`).
