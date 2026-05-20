# FamilyTree

Cross-platform family tree application with:

- Flutter mobile/desktop app (iOS, Android, macOS, Web)
- Spring Boot 3.5 / Java 21 backend with Postgres + MinIO (S3)
- Username/password auth + social login scaffolding (Gmail, Facebook, Microsoft)
- Distinct control-plane and data-plane APIs
- Dockerized local development setup with single-command start

## Repository structure

```text
start-local.sh          # Start full Docker stack (Postgres + MinIO + backend)
stop-local.sh           # Stop and destroy Docker stack + volumes
run-all.sh              # start-local.sh + flutter run
run-ios.sh              # iOS Simulator runner
run-android.sh          # Android emulator runner
run-web.sh              # Chrome (web) runner
run-macos.sh            # macOS desktop runner
app/                    # Flutter app
  lib/
    app/                # App + routing
    core/
      services/         # ApiClient, AuthService, FamilyService
      theme/
      widgets/
    features/
      auth/             # LoginScreen, RegisterScreen, AuthController
      family_tree/      # Tree UI, FamilyTreeController, FamilyRepository
  test/
  pubspec.yaml
backend/
  user-management-service/   # Spring Boot backend
    src/main/java/com/familytree/usermgmt/
      config/           # SecurityConfig, S3Config, AppProperties
      controller/       # Control-plane and data-plane controllers
      dto/              # Request/response records
      exception/        # GlobalExceptionHandler + exception types
      model/            # JPA entities + AuthProvider enum
      repository/       # Spring Data JPA repositories
      service/          # AuthService, FamilyService, UserService, InvitationService, StorageService
    src/main/resources/
      db/migration/     # Flyway SQL migrations
      application.yml
docker-compose.yml
```

## Local dev quickstart

### Prerequisites

- Docker Desktop
- Flutter SDK (stable channel)
- Java 21 (only needed to build outside Docker)

### Start the full stack

```bash
./start-local.sh
```

This builds the backend image, starts Postgres, MinIO, and the API server, and waits for each to be healthy before exiting.

### Start everything + Flutter

```bash
./run-all.sh
```

### Stop + clean up

```bash
./stop-local.sh   # stops containers and destroys volumes
```

### Service URLs

| Service       | URL / Port                         | Credentials               |
|---------------|------------------------------------|---------------------------|
| Backend API   | http://localhost:8080              |                           |
| Postgres      | localhost:5433 / db: `familytree`  | familytree / familytree   |
| MinIO API     | http://localhost:9000              | familytree / familytree   |
| MinIO Console | http://localhost:9001              | familytree / familytree   |

Photos are stored in the `family-tree-photos` bucket (public-read).

---

## Control-plane API

All endpoints under `/api/control-plane/v1/`.

### Auth

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/auth/register` | Username/password registration |
| `POST` | `/auth/login` | Username/password login |
| `POST` | `/auth/social` | Social auth (GMAIL works; FACEBOOK/GOOGLE return 400 — future use) |

**Register body:**
```json
{ "username": "alice", "password": "secret123", "email": "alice@example.com", "displayName": "Alice" }
```

**Auth response:**
```json
{
  "userId": "...", "accessToken": "dev-access-...", "refreshToken": "dev-refresh-...",
  "displayName": "Alice", "email": "alice@example.com", "provider": "LOCAL", "familyId": null
}
```

### Users

| Method | Path | Header | Description |
|--------|------|--------|-------------|
| `GET` | `/users/me` | `X-User-Id` | Get current user profile |
| `DELETE` | `/users/me` | `X-User-Id` | Delete account + clean up S3 photos |

### Families

| Method | Path | Header | Description |
|--------|------|--------|-------------|
| `POST` | `/families` | `X-User-Id` | Create a new family |
| `POST` | `/families/{id}/join` | `X-User-Id` | Join via join code |
| `DELETE` | `/families/{id}/members/me` | `X-User-Id` | Leave family |

### Invitations

| Method | Path | Header | Description |
|--------|------|--------|-------------|
| `POST` | `/invitations` | `X-User-Id` | Create invitation (optional email) |
| `GET` | `/invitations/{code}/details` | — | Public lookup of invite (family name, inviter) |
| `POST` | `/invitations/{code}/accept` | `X-User-Id` | Accept invitation and join family |

---

## Data-plane API

All endpoints under `/api/data-plane/v1/`.

### Bootstrap

| Method | Path | Header | Description |
|--------|------|--------|-------------|
| `GET` | `/families/bootstrap` | `X-User-Id` | User + family context snapshot |

### Family members (tree nodes)

| Method | Path | Header | Description |
|--------|------|--------|-------------|
| `POST` | `/families/{familyId}/members` | `X-User-Id` | Add a person to the tree |
| `GET` | `/families/{familyId}/members` | `X-User-Id` | List all members (flat, with parent pointers) |
| `PUT` | `/families/{familyId}/members/{memberId}` | `X-User-Id` | Update member metadata |
| `POST` | `/families/{familyId}/members/{memberId}/photo` | `X-User-Id` | Upload photo (multipart `photo` field) |

### Export

| Method | Path | Header | Description |
|--------|------|--------|-------------|
| `GET` | `/families/{familyId}/export` | `X-User-Id` | Full JSON export of tree |

---

## Database schema

Managed by Flyway. Migration in `src/main/resources/db/migration/V1__init.sql`.

Tables: `users`, `families`, `family_members`, `invitations`.

---

## Flutter app

### Auth flow

- App starts on `LoginScreen` if no persisted session, otherwise goes straight to `FamilyTreeScreen`.
- `AuthController` (ChangeNotifier via `provider`) manages auth state.
- Session is persisted in `SharedPreferences`.

### Tree screen

- Displays the interactive family tree. If a `familyId` is available, loads members from the backend and maps them to the `FamilyTree` model.
- Falls back to sample data when offline or not connected to a backend.
- Drawer provides family management: create, join, invite, export, sign out.

### Adding new features

- HTTP calls go through `app/lib/core/services/api_client.dart`.
- Auth operations go through `app/lib/core/services/auth_service.dart`.
- Family + member operations go through `app/lib/core/services/family_service.dart`.

---

## Run backend tests

```bash
cd backend/user-management-service
mvn test
```

## Run Flutter tests

```bash
cd app
flutter test
```

## Notes

- Dev access/refresh tokens are simple prefixed UUIDs (`dev-access-{userId}`). Replace with JWT for production.
- MinIO is configured with `forcePathStyle=true` to work with the AWS SDK v2.
- `ddl-auto: validate` ensures Hibernate validates against the Flyway schema without modifying it.
