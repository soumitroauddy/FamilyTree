# Leadership Principles — Difficult Technical Problem (Railway / Supabase Deploy)

Maps the [behavioral answer](./difficult-technical-problem-behavioral-round.md) to Amazon Leadership Principles. Use **one sentence per LP** in interviews; lead with **Dive Deep**, **Ownership**, and **Deliver Results**.

---

## Primary LPs (lead with these)

### Dive Deep

I went past the generic “DB connection failed” message and traced it through DNS (IPv6-only direct host), TCP routing on Railway, Supavisor tenant routing, and regional pooler hostnames until each failure mode had a distinct, testable explanation.

### Ownership

I owned the full deploy failure end-to-end—from Railway runtime logs through Supabase connection semantics—and didn’t treat it as “someone else’s infra” until I had a verified fix in both live env vars and repo defaults.

### Deliver Results

The service was crash-looping in production; I restored a healthy deploy path by fixing the datasource configuration, validating connectivity with `psql`, and redeploying so Flyway could complete and the API could serve traffic.

---

## Supporting LPs

### Learn and Be Curious

When the error changed from `Network is unreachable` to `Tenant or user not found`, I treated it as a new hypothesis rather than assuming the first fix was “close enough,” and learned Supabase pooler hostnames are not uniform across regions. A third error—Flyway “non-empty schema” with no app tables in the UI—taught me that **managed Postgres empty state ≠ Flyway empty state**, and that `baseline-version` must match whether migrations still need to run.

### Insist on the Highest Standards

I refused to paper over the issue with retries, disabled migrations, or vague “DB down” handling—I required evidence at each layer (DNS, TCP, pooler auth) before calling the incident resolved.

### Bias for Action

As soon as logs showed Flyway failing at startup, I reproduced and tested connection paths locally and against candidate pooler endpoints instead of waiting on a full redeploy cycle for every guess.

### Invent and Simplify

I replaced a fragile assumption (“use `aws-0-us-east-1` because S3 is in `us-east-1`”) with a simple, repeatable contract: pooler URI + `postgres.<project-ref>` username, documented in config and env vars.

### Are Right, A Lot

My initial IPv6/pooler diagnosis was directionally right; when auth still failed, I updated the model (wrong pooler region) based on systematic endpoint testing rather than defending the first theory.

### Earn Trust

I separated “network unreachable” from “tenant not found” with clear evidence so teammates could trust the fix wasn’t a lucky config change, and I aligned runtime config with source-controlled defaults to reduce surprise on the next deploy.

### Have Backbone; Disagree and Commit

I pushed back on treating this as an application bug or credential issue without proof; once we identified platform-specific connection requirements, I committed to the pooler-based approach even though it added operational nuance.

---

## LP cheat sheet (one phrase each)

| LP | Phrase from the story |
|----|------------------------|
| **Ownership** | Railway + Supabase + Spring/Flyway + git defaults |
| **Dive Deep** | DNS → IPv6 → pooler → tenant → region → Flyway baseline |
| **Deliver Results** | Crash loop → healthy startup / migrations |
| **Learn and Be Curious** | Each new error = new model (3 failure classes) |
| **Highest Standards** | No Flyway-off / blind retries |
| **Bias for Action** | Test pooler endpoints while deploys failed |
| **Invent and Simplify** | Explicit env contract vs guessing region |
| **Are Right, A Lot** | Revised theory when data contradicted it |
| **Earn Trust** | Evidence-based + config in repo |
| **Backbone** | Challenged “bad password/app bug” without proof |

---

## 30-second LP wrap (closing line)

> “This story hits **Ownership** and **Dive Deep** hardest—I didn’t stop at the first error. **Deliver Results** is production unblocked. **Learn and Be Curious** shows up three times: IPv6/pooler, wrong pooler region vs S3, then Flyway baseline when Supabase looked empty in the UI but wasn’t empty to the migrator.”

---

## If interviewer names one LP

| They say | You emphasize |
|----------|----------------|
| **“Dive Deep”** | Three failure modes; DNS AAAA; pooler tenant; Flyway baseline vs Table Editor |
| **“Ownership”** | End-to-end; env vars + application.yml; preflight with `psql` |
| **“Deliver Results”** | Before: restart loop; after: Flyway succeeds, service healthy |
| **“Customer obsession”** | Users couldn’t use new backend until deploy worked; minimized downtime of *new* rollout |
| **“Frugality”** | Used existing Supabase pooler (no paid IPv4 add-on) |

---

## Related documents

- [Behavioral round answer](./difficult-technical-problem-behavioral-round.md)
- [System design round answer](./difficult-technical-problem-system-design-round.md)
