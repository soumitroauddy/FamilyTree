# What was a difficult technical problem you solved?

**Round:** Behavioral (Hiring Manager / Bar Raiser)  
**Level:** Senior SDE @ Amazon  
**Time:** ~3–4 minutes spoken; use the 60-second version if they ask for brevity

---

## Full answer (STAR)

### Situation

I was deploying a **Spring Boot 3.5 / Java 21** backend to **Railway** for a family-tree product. The service runs **Flyway** migrations at startup, uses **PostgreSQL on Supabase**, and integrates **Supabase Auth** plus **S3-compatible object storage**. The Docker image built and deployed successfully, but the service **never became healthy**—it crashed in a restart loop before accepting traffic.

### Task

My goal was to get a **reliable production deploy**: the API had to pass health checks, complete schema migrations, and stay up under Railway’s runtime—not just “build green.”

### Action

I treated it as a **connectivity → auth → configuration** problem instead of guessing from stack traces.

**First failure — network**

- Logs showed `java.net.SocketException: Network is unreachable` during Hikari/Flyway startup.
- The app defaulted to Supabase’s **direct** host (`db.<project>.supabase.co`).
- DNS showed that hostname is **IPv6-only** (AAAA record, no IPv4 A record).
- Railway’s container network could not route to that address, so TCP never completed—this was not a password or application bug.

**Fix attempt 1 — move to the pooler**

- Switched to Supabase’s **Supavisor connection pooler** (IPv4-capable), which is the recommended pattern for PaaS hosts with inconsistent IPv6 routing.
- Set the pooler username to `postgres.<project-ref>` (required for tenant routing on shared poolers).
- Updated Railway environment variables and application defaults so we would not silently regress to the direct host.

**Second failure — wrong pooler region**

- After the network issue was fixed, startup failed with `FATAL: Tenant or user not found`.
- That is a **pooler routing** error, not a generic auth failure: the pooler did not recognize the tenant for the hostname we used.
- I had assumed `aws-0-us-east-1` because object storage was configured for `us-east-1`; **database region and S3 region are independent**.
- I systematically tested regional pooler endpoints and confirmed the project’s actual pooler: **`aws-1-us-west-1.pooler.supabase.com`** on port 5432 (session mode, suitable for Flyway DDL).
- Verified connectivity with `psql` before redeploying.

**Third failure — Flyway baseline (misleading “empty” database)**

- After connectivity and pooler region were correct, startup failed with: `Found non-empty schema(s) "public" but no schema history table. Use baseline() or set baselineOnMigrate to true`.
- Supabase’s **Table Editor showed no app tables**—easy to assume the database was empty and that tables had to be created manually first. That was wrong.
- The DB **was reachable** (Hikari connected); this was a **migration policy** failure, not connectivity or missing DDL.
- Flyway treats `public` as non-empty when Supabase has default objects (extensions, etc.) even if the product has zero `users` / `families` tables.
- **Wrong fix:** `baseline-version: 1` alone would mark `V1__init.sql` as already applied and **skip** creating app tables.
- **Correct fix:** `baseline-on-migrate: true` with `baseline-version: 0` so Flyway creates `flyway_schema_history`, baselines, then still runs `V1__init.sql`.
- Documented the rationale in `application.yml` so the next deploy wouldn’t regress.

**Hardening**

- Documented in `application.yml` why direct `db.*` hosts fail on Railway-like platforms.
- Left an explicit env contract: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
- Added Flyway baseline settings for managed Postgres (Supabase) where `public` is never truly empty to the migrator.

### Result

- Eliminated deploy crash-loops; startup progressed past Flyway and the service became healthy.
- Cut time-to-diagnosis by separating **network**, **pooler tenant routing**, **region**, and **Flyway baseline semantics** instead of treating every error as “database down” or “create tables manually.”
- Reduced recurrence risk by fixing **runtime config**, **repo defaults**, and **migration config** (not one-off SQL in the Supabase dashboard).

---

## 60-second version (connectivity focus)

> We had a production deploy where Spring Boot built fine but crashed on startup during Flyway migrations. First it was `Network is unreachable` because Supabase’s direct DB hostname was IPv6-only and Railway couldn’t route to it. I switched to Supabase’s IPv4 pooler and the `postgres.<project-ref>` username format. Then we hit `Tenant or user not found`—which looked like auth but was the wrong pooler region. I’d assumed `us-east-1` from S3 config, but the database was on `aws-1-us-west-1`. I tested regional pooler endpoints, confirmed connectivity with `psql`, updated Railway env vars and application defaults, and redeployed successfully. The hard part wasn’t Spring—it was diagnosing two different failure modes behind the same symptom.

## 90-second version (full incident, including Flyway baseline)

> Same deploy: after fixing IPv6 and the pooler region, we hit a third crash loop. Flyway said `Found non-empty schema(s) "public" but no schema history table`. Supabase’s UI showed no app tables, so it felt like we needed to create schema manually—but Flyway was complaining that `public` wasn’t empty *to the migrator* because of Supabase defaults like extensions. Connection was fine; this was migration policy. I set `baseline-on-migrate` with `baseline-version: 0` so we still run `V1__init.sql`—not version 1, which would have skipped table creation. Redeploy succeeded. The lesson is three failure classes behind one symptom: network, pooler routing, and Flyway baseline semantics on managed Postgres.

---

## Follow-up questions (Behavioral)

| Question | Answer |
|----------|--------|
| **What would you do differently?** | Add a deploy preflight: DNS (A vs AAAA), pooler `psql`, and a note that Supabase `public` is non-empty to Flyway even with zero app tables; document baseline settings in the runbook. |
| **Did you need tables in Supabase first?** | No—Flyway creates them. Manual DDL without `flyway_schema_history` causes the same error; Supabase defaults can trigger it even with an “empty” Table Editor. |
| **How did you prioritize?** | Unblock production first (correct pooler + env vars), then prevent regression (yaml defaults + comments). |
| **Did you work alone?** | Yes for diagnosis and fix; I would have pulled in a teammate earlier for a second pair of eyes on the second error mode. |
| **Customer impact?** | New deploys were blocked; no partial traffic on a broken instance because the process exited before binding the port. |
| **Strongest Leadership Principles?** | **Dive Deep**, **Ownership**, **Deliver Results** — see [leadership-principles-mapping.md](./leadership-principles-mapping.md). |

---

## What not to say

- Do not read out real passwords, JWT secrets, or project-specific credentials in an interview.
- Do not claim you “fixed Spring Boot”—the framework was fine; the integration contract was wrong.
- Do not skip the second or third failure; each one shows you updated your mental model when evidence changed.
- Do not claim “Supabase had no database objects”—Flyway’s “non-empty” ≠ “no tables in the UI.”
