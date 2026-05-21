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

**Hardening**

- Documented in `application.yml` why direct `db.*` hosts fail on Railway-like platforms.
- Left an explicit env contract: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

### Result

- Eliminated deploy crash-loops; startup progressed past Flyway and the service became healthy.
- Cut time-to-diagnosis on the second failure by separating **network**, **pooler tenant routing**, and **region** instead of treating every error as “database down.”
- Reduced recurrence risk by fixing both **runtime config** and **repo defaults**, plus a repeatable preflight (`psql` against the pooler).

---

## 60-second version

> We had a production deploy where Spring Boot built fine but crashed on startup during Flyway migrations. First it was `Network is unreachable` because Supabase’s direct DB hostname was IPv6-only and Railway couldn’t route to it. I switched to Supabase’s IPv4 pooler and the `postgres.<project-ref>` username format. Then we hit `Tenant or user not found`—which looked like auth but was the wrong pooler region. I’d assumed `us-east-1` from S3 config, but the database was on `aws-1-us-west-1`. I tested regional pooler endpoints, confirmed connectivity with `psql`, updated Railway env vars and application defaults, and redeployed successfully. The hard part wasn’t Spring—it was diagnosing two different failure modes behind the same symptom.

---

## Follow-up questions (Behavioral)

| Question | Answer |
|----------|--------|
| **What would you do differently?** | Add a deploy preflight that checks DNS (A vs AAAA) and pooler connectivity; document “copy pooler URI from Supabase dashboard—do not infer region from other services.” |
| **How did you prioritize?** | Unblock production first (correct pooler + env vars), then prevent regression (yaml defaults + comments). |
| **Did you work alone?** | Yes for diagnosis and fix; I would have pulled in a teammate earlier for a second pair of eyes on the second error mode. |
| **Customer impact?** | New deploys were blocked; no partial traffic on a broken instance because the process exited before binding the port. |
| **Strongest Leadership Principles?** | **Dive Deep**, **Ownership**, **Deliver Results** — see [leadership-principles-mapping.md](./leadership-principles-mapping.md). |

---

## What not to say

- Do not read out real passwords, JWT secrets, or project-specific credentials in an interview.
- Do not claim you “fixed Spring Boot”—the framework was fine; the integration contract was wrong.
- Do not skip the second failure; it shows you updated your mental model when evidence changed.
