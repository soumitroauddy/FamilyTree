# What was a difficult technical problem you solved?

**Round:** System Design (or technical deep-dive adjacent to SD)  
**Level:** Senior SDE @ Amazon  
**Framing:** Use this when the interviewer wants **architecture, tradeoffs, and operability**—not only STAR storytelling.

---

## 1. Problem statement (30 seconds)

Design and operate a **stateful API** on a **container PaaS (Railway)** backed by **managed Postgres (Supabase)** with **schema migrations at boot (Flyway)**. The system must be **deployable, observable, and recoverable** when third-party network and connection semantics differ from local Docker Compose.

---

## 2. Architecture context

```text
[Flutter client] → [Railway: Spring Boot API] → [Supabase Postgres via Supavisor pooler]
                          ↓
                   [Supabase Auth JWT]
                          ↓
                   [Supabase S3-compatible storage]
```

| Layer | Local dev | Production |
|-------|-----------|------------|
| API runtime | Docker Compose | Railway container |
| Database host | `postgres:5432` (Docker network) | Supavisor pooler (public IPv4) |
| DB identity | `familytree` / static password | `postgres.<project-ref>` + secret from env |
| Migrations | Flyway at startup | Same—fail-fast if DB unreachable |

**Key design choice:** Fail-fast at startup if Flyway cannot connect. Correct for schema safety; brittle if connection config is wrong.

---

## 3. Failure modes (what made it “systems” hard)

Three distinct failures, **same user-visible symptom** (app restart loop):

| # | Symptom | Layer | Root cause |
|---|---------|-------|------------|
| 1 | `Network is unreachable` | L3/L4 + DNS | Direct Supabase host is **IPv6-only**; PaaS egress could not reach it |
| 2 | `Tenant or user not found` | Connection pooler routing | **Wrong regional pooler hostname**; pooler could not map username to tenant |
| 3 | `Found non-empty schema(s) "public" but no schema history table` | Flyway migration policy | Supabase `public` is **non-empty to Flyway** (extensions, defaults) with **no** `flyway_schema_history`; not “missing app tables” |

**Lesson:** “Flyway failed at startup” is not one failure class either. Partition: **reachability** → **pooler tenant** → **baseline / schema history** → credentials.

---

## 4. Connection options (tradeoff table)

| Approach | Pros | Cons | Verdict for Railway |
|----------|------|------|---------------------|
| Direct `db.*.supabase.co:5432` | Simple URI; full Postgres features | Often **IPv6-only**; PaaS may not route IPv6 | ❌ Avoid |
| Supavisor session pooler `:5432` | IPv4; compatible with DDL/migrations | Region-specific hostname; username `postgres.<ref>` | ✅ Used for Flyway |
| Supavisor transaction pooler `:6543` | Good for serverless / many short connections | Some migration/ prepared-statement caveats | ⚠️ Better for app traffic than Flyway |
| Railway-managed Postgres + `DATABASE_URL` | Private networking; env injection | Another datastore to operate; migration from Supabase | ✅ Valid alternative |
| Supabase dedicated IPv4 add-on | Keeps direct connection | Cost; still ops overhead | Optional |

---

## 5. Design decisions I made or reinforced

### 5.1 Environment contract

Explicit variables instead of implicit defaults:

- `SPRING_DATASOURCE_URL` — JDBC to **correct regional pooler**
- `SPRING_DATASOURCE_USERNAME` — `postgres.<project-ref>`
- `SPRING_DATASOURCE_PASSWORD` — database password (secret store / Railway vars)

Optional: `RailwayEnvironmentPostProcessor` maps `DATABASE_URL` when using Railway Postgres instead of Supabase.

### 5.2 Fail-fast migrations

Keep Flyway at startup **enabled** rather than disabling migrations to “get green.”

- **Why:** Schema drift in production is worse than a failed deploy.
- **Mitigation:** Preflight connectivity check in CI/CD or runbook before `railway up`.

### 5.3 Defaults vs environment-specific config

- **Production defaults** in `application.yml` point at pooler, not direct host.
- **Local profile** (`application-local.yml` / Compose) uses Docker hostname `postgres`.

Separation prevents “works on my machine” from leaking IPv6-only URIs into cloud deploys.

### 5.4 Flyway baseline on managed Postgres (Supabase)

Managed providers often ship a **non-empty `public` schema** before your app exists:

| What operators see | What Flyway sees |
|--------------------|------------------|
| Table Editor: no `users`, `families`, etc. | `public` has extensions / default objects → **non-empty** |
| “Database is empty, create tables first?” | Needs `flyway_schema_history` or explicit baseline |

**Config (production):**

```yaml
spring:
  flyway:
    baseline-on-migrate: true
    baseline-version: 0   # required: default baseline 1 would skip V1__init.sql
```

| Setting | Risk if wrong |
|---------|----------------|
| No baseline | Crash loop on first deploy to Supabase |
| `baseline-version: 1` only | Skips `V1__init.sql`; Hibernate `validate` fails later |
| `baseline-version: 0` + `baseline-on-migrate` | Baselines empty-of-app-tables schema, then applies migrations |

**Do not** pre-create app tables in the Supabase SQL editor unless you intentionally baseline at version 1 with a schema that matches `V1__init.sql`.

---

## 6. Observability and operability (what I’d add at Amazon scale)

| Gap during incident | Improvement |
|-------------------|-------------|
| Generic SQLState `08001` / `XX000` | Structured startup probe: `db_connectivity{stage=dns\|tcp\|auth}` |
| Crash loop only visible in platform logs | Alert on restart count + exit code before LB marks healthy |
| Region guessed from S3 config | Runbook: pooler host **only** from Supabase dashboard connection string |
| No pre-deploy check | CI step: `psql` or TCP + SSL handshake to pooler from CI runner |
| Flyway baseline error looks like “empty DB” | Runbook: Supabase `public` ≠ empty to Flyway; document `baseline-version: 0` |

**Health endpoint:** Spring Actuator `health` already exposed; dependency check could surface “database unreachable” vs “authentication failed” if we add a custom `DataSourceHealthIndicator` with staged checks.

---

## 7. Scaling and reliability (Senior SDE depth)

- **Pooler vs direct:** At higher scale, split **migration path** (session pooler or direct IPv4) from **request path** (transaction pooler, larger pool).
- **Connection pooling:** HikariCP in-app + Supavisor—avoid double-pooling misconfiguration (pool size vs Supavisor limits).
- **Multi-region:** If API moves regions, DB region does not automatically follow; connection strings are **regional assets**.
- **Secrets:** Password rotation requires coordinated Railway var update; pooler username format stays tied to project ref.

---

## 8. How to present in a System Design interview (outline)

1. **Clarify requirements:** HA target, RPO/RTO, migration strategy, multi-tenant or single DB.
2. **Draw boxes:** client → API → pooler → Postgres; parallel Auth and object storage.
3. **Call out the incident:** two failure modes, how you partitioned diagnosis.
4. **Tradeoffs:** fail-fast Flyway, pooler session vs transaction, managed DB vs self-hosted on Railway.
5. **Operability:** preflight, alerts, runbooks, don’t infer region from unrelated services.
6. **Close with metrics:** deploy success rate, mean time to detect, restart count before healthy.

---

## 9. 90-second spoken version (System Design)

> “The problem was operating a Spring Boot service on Railway against Supabase Postgres with Flyway at startup. Architecturally it’s a standard three-tier pattern, but the integration contract is subtle. Locally we use Docker DNS to `postgres`; in production we use Supabase’s IPv4 pooler because the direct hostname is IPv6-only. Failure one was network reachability; failure two was wrong pooler region—tenant not found—not bad passwords. Failure three was Flyway: non-empty `public` schema but no history table. Supabase’s UI showed no app tables, but Flyway still refused to migrate until we baselined with version zero so `V1__init.sql` actually runs. I’d keep fail-fast migrations, document env and Flyway contracts in repo, add staged preflight in CI, and treat managed-Postgres baseline behavior as part of the platform matrix—not something you discover only in production.”

---

## 10. Whiteboard diagram (ASCII)

```text
                    ┌─────────────────────┐
                    │   Supabase Auth     │
                    └──────────┬──────────┘
                               │ JWT
┌──────────┐    HTTPS    ┌─────▼──────────────────────────┐
│  Client  │────────────►│  Railway: Spring Boot API       │
└──────────┘             │  - Flyway @ startup (fail-fast) │
                         │  - HikariCP                     │
                         └─────┬────────────────────────────┘
                               │ JDBC (IPv4)
                               ▼
              ┌────────────────────────────────────┐
              │ aws-1-us-west-1.pooler.supabase.com │
              │ user: postgres.<project-ref>        │
              └────────────────┬───────────────────┘
                               ▼
              ┌────────────────────────────────────┐
              │      Supabase Postgres (tenant)     │
              └────────────────────────────────────┘

  ✗ Avoid: db.<project>.supabase.co (IPv6-only from Railway)
```

---

## Related

- Behavioral STAR answer: [difficult-technical-problem-behavioral-round.md](./difficult-technical-problem-behavioral-round.md)
- Leadership Principles: [leadership-principles-mapping.md](./leadership-principles-mapping.md)
