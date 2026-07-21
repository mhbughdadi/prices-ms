Pricing Microservice — Minimized Canonical Summary

This repository includes detailed docs. To reduce duplication, use these canonical summaries when querying the project:

- AGENTS rules: AGENTS_SUMMARY.md
- Implementation checklist: IMPLEMENTATION_GUIDE_SUMMARY.md
- Repository query patterns: QUICK_REFERENCE.md

Essentials (short):
- Price calc order: Base → Sale → Event → Region → Segment → Tier → Rules → Currency → Tax
- Always filter deleted = false (soft deletes)
- Layering: Controller → BackingService → Service (business logic) → Repository
- Mapper: field-name exact match (case-sensitive)
- Build: mvn clean spring-boot:run; Tests: mvn test

For full docs, consult the original detailed files in this folder.
