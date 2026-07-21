IMPLEMENTATION_GUIDE.md — Token-efficient summary

Purpose: One-page implementation checklist and templates for fast agent action.

File structure (predictable): controller/, facade/, services/, services/impl/, repositories/, entities/, models/, dtos/, exceptions/, constants/, configs/, filters/

Essentials:
- Constants: centralize all messages and keys
- Mapper: use custom ObjectMapper.transform/transformCollection; ensure identical field names
- Exceptions: only ResourceNotFoundException and DatabaseException
- Services: annotate @Service and @Transactional on implementation classes
- Facade/BackingService: convert DTOs ↔ models, no business logic
- Controllers: thin, with OpenAPI annotations and MDC requestId

Quick checklist for new endpoint:
1) Entity + Repository + Migration
2) Model + DTOs (input/output)
3) Service interface + ServiceImpl + tests (JUnit5+Mockito)
4) Facade (DTO ↔ model) + Controller (OpenAPI)
5) Add constants + translations + update docs

Dev commands: mvn clean package -DskipTests; mvn test; docker build -t service:latest .

Last updated: 2026-07-21
