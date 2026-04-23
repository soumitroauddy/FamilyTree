# FamilyTree

Cross-platform family tree application with:

- Flutter mobile app (iOS + Android)
- New Spring Boot backend for user management and family membership
- Social account creation (Gmail, Facebook, Microsoft)
- Distinct control-plane and data-plane APIs
- Dockerized local development setup

## Repository structure

```text
app/                                   # Flutter app
  lib/
    app/
    core/
    features/family_tree/
  test/                                # Flutter tests
  ios/                                 # iOS platform
  android/                             # Android platform
  macos/                               # macOS platform
  web/                                 # Web platform
  pubspec.yaml
backend/
  user-management-service/             # Java Spring Boot backend
    src/main/java/com/familytree/usermgmt/
      config/
      controller/controlplane/
      controller/dataplane/
      dto/
      exception/
      model/
      repository/
      service/
    src/test/java/com/familytree/usermgmt/
      controller/
      service/
docker-compose.yml
```

## Backend capabilities

### Auth and account creation

`POST /api/control-plane/v1/auth/social`

Accepts:

- `provider`: `GMAIL`, `FACEBOOK`, or `MICROSOFT`
- `providerUserId`
- `email`
- `displayName`

Returns app tokens + user profile payload.

### Family membership

- Create family: `POST /api/control-plane/v1/families`
- Join family: `POST /api/control-plane/v1/families/join`
- Leave family: `POST /api/control-plane/v1/families/leave`

All membership endpoints require `X-User-Id`.

### Control-plane APIs

For account/management operations:

- `POST /api/control-plane/v1/auth/social`
- `GET /api/control-plane/v1/users/me`
- `POST /api/control-plane/v1/families`
- `POST /api/control-plane/v1/families/join`
- `POST /api/control-plane/v1/families/leave`

### Data-plane APIs

For mobile app data consumption:

- `GET /api/data-plane/v1/families/bootstrap`

Returns current user and family snapshot payload for iOS/Android app bootstrap.

## Local development setup (MacBook Pro friendly)

### Prerequisites

- Docker Desktop (latest)
- Flutter SDK (stable)
- Java 21 (if running backend outside Docker)

### Run backend with Docker

```bash
docker compose up --build
```

Backend will be available at:

- `http://localhost:8080`

### Run backend tests

```bash
cd backend/user-management-service
mvn test
```

### Run Flutter app

```bash
cd app
flutter pub get
flutter run -d macos    # macOS desktop
flutter run -d chrome   # Web
flutter run -d android  # Android (emulator or device)
flutter run -d iPhone   # iOS (simulator or device)
```

### Run Flutter tests

```bash
cd app
flutter test
```

## Notes

- Backend persistence is in-memory for fast local iteration and tests.
- Replace repository implementations with database-backed adapters when moving to production.
- Mobile integration can switch from sample data to backend APIs through `FamilyRepository`.
