# Interview Questions — Prepared Answers

Prepared responses for Amazon Senior SDE interviews, based on real work from this repository.

| Question | Behavioral (HM / Bar Raiser) | System Design |
|----------|-------------------------------|---------------|
| What was a difficult technical problem you solved? | [behavioral-round.md](./difficult-technical-problem-behavioral-round.md) | [system-design-round.md](./difficult-technical-problem-system-design-round.md) |
| Leadership Principles mapping | [leadership-principles-mapping.md](./leadership-principles-mapping.md) | — |

## Source incident

Spring Boot backend deploy to Railway with Supabase PostgreSQL: crash-loop due to IPv6-only direct DB hostname, then wrong Supavisor pooler region (`aws-0-us-east-1` vs actual `aws-1-us-west-1`).
