# Campus Club Management - MVP Scope

## Objectives
- Deliver core functionality from the specification focusing on onboarding, club discovery, and basic management.
- Provide a RESTful Spring Boot backend with JWT authentication and role-based authorization.
- Ship a Vue 3 (Vite) SPA that consumes the backend APIs for key user journeys.

## Included Features
- **Authentication:** student/manager/admin registration with BCrypt password hashing, login via JWT, and role-based menus.
- **Interest Tags:** predefined list stored server-side, selectable during registration (3-5 tags), persisted for recommendation signals.
- **Clubs:** CRUD for club metadata (name, description, logo URL, category, tags, contact) with search and filter endpoints; manager-limited write access.
- **Memberships:** students request to join clubs, managers approve/reject; membership statuses tracked for analytics.
- **Activities:** club managers create events; students browse and sign up; attendance status tracked for later dashboards.
- **Messages:** minimal inbox for approval/sign-up notifications; read/unread status maintained.
- **Dashboard:** skeleton statistics endpoint aggregating counts for cards/charts (uses current data to produce sample metrics).

## Excluded / Deferred
- Binary uploads (logo, video, photo walls) replaced by URL fields.
- AI features (subtitle generation, intelligent matching) stubbed with TODO notes and deterministic placeholder logic.
- External notifications (SMS, WeChat) and cross-club collaboration workflows left as future enhancements.
- Advanced analytics, A/B testing, and real-time updates deferred.

## High-Level Architecture
- **Backend:** Spring Boot 3.5, Spring Data JPA, Spring Security, MySQL (with optional H2 profile), Mapstruct-less DTO mapping, Flyway for schema versioning.
- **Frontend:** Vite + Vue 3 + TypeScript, Pinia store, Vue Router, Axios API layer, component-based UI using Element Plus for rapid layout.
- **Build/Run:** Backend via Maven (`mvn spring-boot:run`), frontend via pnpm (`pnpm dev`), both documented in README.

