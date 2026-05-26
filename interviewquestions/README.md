# Interview Questions — Prepared Answers

Prepared responses for Amazon Senior SDE interviews, based on real work from this repository.

| Question | Behavioral (HM / Bar Raiser) | System Design |
|----------|-------------------------------|---------------|
| What was a difficult technical problem you solved? | [behavioral-round.md](./difficult-technical-problem-behavioral-round.md) | [system-design-round.md](./difficult-technical-problem-system-design-round.md) |
| Leadership Principles mapping | [leadership-principles-mapping.md](./leadership-principles-mapping.md) | — |

## Source incident

Spring Boot backend deploy to Railway with Supabase PostgreSQL: crash-loop with **three** distinct Flyway/DB failures:

1. IPv6-only direct DB hostname (`Network is unreachable` on Railway)
2. Wrong Supavisor pooler region (`Tenant or user not found`)
3. Flyway baseline on Supabase’s non-empty `public` schema with no app tables (`Found non-empty schema(s) "public" but no schema history table` → `baseline-on-migrate` + `baseline-version: 0`)
