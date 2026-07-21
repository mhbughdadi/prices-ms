AGENTS.md — Token-efficient summary

Purpose: Short, copyable reference for AI agents working on pricing and products microservices.

Key rules (pricing):
- Price calc order (must follow): Base → Sale → Event → Region → Segment → Tier → Rules → Currency → Tax → Return effective + old price
- Cache keys: price:sku:{skuId}[:segment:{segment}][:country:{country}]
- Soft deletes: always filter deleted = false
- Outbox events for all price changes

Key rules (products):
- Layering: Controller → BackingService/Facade → Service (@Transactional) → Repository → DB
- DTOs only in Facade; Services return domain models
- Mapper: field-name exact match (case-sensitive)
- Exceptions: use only RecordNotFoundException and DBException

Dev shortcuts:
- Build: mvn clean spring-boot:run
- Tests: mvn test
- Docker: docker compose up -d

Token-saving tips:
- Centralize strings in constants
- Keep DTOs flat
- Use consistent naming patterns

Last updated: 2026-07-21
