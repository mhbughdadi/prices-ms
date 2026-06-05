# AGENTS.md - Pricing Microservice Developer Guide

## Quick Start for AI Agents

This is an enterprise pricing calculation engine serving as the single source of truth for all pricing operations. The service answers: **"What is the effective price of SKU X for customer Y in region Z at time T?"**

---

## Architecture Overview

### Core Responsibility
The Pricing Microservice manages **only pricing-related data** across multiple business dimensions:
- **Owned**: Price Lists, SKU Prices, Region Prices, Segment Prices, Tier Prices, Exchange Rates, Tax Rules, Price Events, Bundle Prices, Pricing Rules, Audit History
- **NOT Owned**: Products, Inventory, Orders, Promotions, Coupons, Payments

### Effective Price Calculation Flow (Critical!)
Prices are calculated in **strict order** (see `README.md` lines 302-326):
1. Load Base Price (regular_price)
2. Apply Sale Price (if active)
3. Apply Scheduled Event Price (FLASH_SALE, BLACK_FRIDAY, RAMADAN, SEASONAL, CUSTOM)
4. Apply Region Price
5. Apply Customer Segment Price
6. Apply Tier Price (volume-based)
7. Apply Dynamic Pricing Rules (JSONB conditions)
8. Apply Currency Conversion
9. Apply Tax Rules (INCLUSIVE or EXCLUSIVE)
10. Return Effective Price

**Agents MUST respect this order when calculating or modifying prices.**

### Multi-Dimensional Pricing
Every price can vary across dimensions. Example cache keys show the pattern:
```text
price:sku:{skuId}
price:sku:{skuId}:segment:{segment}
price:sku:{skuId}:country:{country}
```
When implementing features, consider all dimensions: CustomerSegment × Region × Currency × TimeRange

---

## Key Project Patterns

### 1. Soft Delete Strategy
All entities use soft deletes via `@SQLDelete` annotation and a `deleted` boolean column:
```java
@SQLDelete(sql = "UPDATE sku_prices SET deleted = TRUE WHERE id = ?")
public class SkuPriceEntity { boolean deleted = false; }
```
**Always filter by `deleted = false` in queries** (see PriceListRepository line 30 for example).

### 2. N+1 Prevention
Use `@EntityGraph` or `fetch join` for eager loading relationships:
```java
@EntityGraph(attributePaths = {"skuPrices"})
@Query("select p from PriceListEntity p where p.id = :id and p.deleted = false")
Optional<PriceListEntity> findByIdAndNotDeletedWithSkuPrices(UUID id);
```

### 3. Batch Loading
Use `@BatchSize(size = 50)` on lazy relationships to prevent N+1:
```java
@ManyToOne(fetch = FetchType.LAZY)
@BatchSize(size = 50)
private PriceListEntity priceList;
```

### 4. Optimistic Locking
All entities have version field for concurrent modifications:
```java
@Column(name = "version", nullable = false)
private Long version = 0L;
```

### 5. Audit Trail
All entities track creator/updater and timestamps:
```java
@Column(name = "created_by") private String createdBy;
@Column(name = "created_at") private OffsetDateTime createdAt;
@Column(name = "updated_by") private String updatedBy;
@Column(name = "updated_at") private OffsetDateTime updatedAt;
```

### 6. Outbox Pattern for Events
All pricing changes publish events through `OutboxEventRepository`. Events include:
- `PriceCreated`, `PriceUpdated`, `PriceDeleted`
- `PriceListCreated`, `PriceListUpdated`
- `ExchangeRateChanged`, `TaxRuleChanged`

See schema table `outbox_events` (V1__pricing_schema.sql).

### 7. DTOs Over Entities
Always convert entities to DTOs before returning from controllers/services:
```java
private PriceListDto toDto(PriceListEntity p) {
    PriceListDto dto = new PriceListDto();
    dto.id = p.getId();
    // ... map fields ...
    return dto;
}
```

---

## Database Essentials

### UUID Primary Keys
All entities use UUID with PostgreSQL's `gen_random_uuid()`:
```java
@GeneratedValue(strategy = GenerationType.UUID)
@Column(name = "id", updatable = false, nullable = false)
private UUID id;
```

### Precision for Financial Data
All price columns use NUMERIC(13,2) for two decimal places:
```java
@Column(name = "regular_price", precision = 13, scale = 2, nullable = false)
private BigDecimal regularPrice;
```

### PostgreSQL Enums in Java
Database enums map to Java enums:
```sql
CREATE TYPE customer_segment_type AS ENUM ('RETAIL', 'VIP', 'WHOLESALE', 'EMPLOYEE', 'DISTRIBUTOR');
CREATE TYPE price_event_type AS ENUM ('FLASH_SALE', 'BLACK_FRIDAY', 'RAMADAN', 'SEASONAL', 'CUSTOM');
CREATE TYPE tax_type AS ENUM ('INCLUSIVE', 'EXCLUSIVE');
```

### Flyway Migrations
All migrations in `src/main/resources/db/migration/V*.sql`. Auto-executed on startup.
Schema uses:
- Foreign Keys between pricing dimensions
- Check Constraints for business rules
- GIN Indexes for JSONB querying (pricing_rules table)
- Composite Indexes for common query patterns

---

## Service Layer Patterns

### Repository Layer (`service/` package)
- Thin services like `PriceListService` handle logic
- Use constructor injection (`@Service` class)
- Apply DTOs in-service, not in repositories

### Entity-DTO Conversion Location
Conversion methods (`toDto()`) are **always in service layer**, never in repositories.

### Query Optimization
Look for `@Query` annotations with `left join fetch` or `@EntityGraph` in repositories.
Example repositories: `PriceListRepository`, `SegmentPriceRepository`, `TierPriceRepository`

---

## Development & Build

### Build Command
```bash
mvn clean spring-boot:run
```

### Start Infrastructure (Docker)
```bash
docker compose up -d
```
This starts PostgreSQL 16 and Redis 7 (configured in docker-compose.yml).

### Test Execution
```bash
mvn test
```
Test location: `src/test/java/com/apogee/pricing/`

### Key Dependencies
- **Spring Boot 3.3.5** (Java 21 compatible)
- **Hibernate 6** with Spring Data JPA
- **PostgreSQL 16** driver
- **Flyway Core** for migrations
- **Lombok** for @Getter/@Setter (configured in annotation processor)
- **Log4j2** (not SLF4j) with Logstash integration
- **Springdoc OpenAPI 2.5.0** for Swagger
- **Custom libs**: `com.apogee.common:common-utils@0.0.1`, `com.apogee.spring.common:spring-common@0.0.3`

### Configuration Files
- **Main config**: `src/main/resources/application.properties` (properties loaded at runtime from env variables)
- **Dev config**: `src/main/resources/application-dev.properties`
- **Logging**: `src/main/resources/log4j2.xml` (Console + Logstash Socket)
- **i18n**: `errors_ar.properties`, `errors_en.properties`

---

## API Conventions

### Endpoint Pattern
All endpoints use `/api/v1/{resource}` structure (see controller at line 17).
Example: `GET /api/v1/pricelists/{id}` returns `Optional<PriceListDto>`

### Response Handling
- Use `ResponseEntity<T>` with proper HttpStatus (200, 404, etc.)
- Missing resources → HTTP 404 (NOT_FOUND)
- Return DTOs, never raw entities

### Request Correlation
All requests tracked via `CorrelationFilter` which:
- Generates/extracts `X-REQUEST-ID` header
- Puts `REQUEST_ID` and `SERVICE_ID` in SLF4j MDC for tracing
- Required for service mesh integration

---

## Common Tasks & Implementation Locations

| Task | Location | Example |
|------|----------|---------|
| Add new price dimension | `entity/` (new Entity + @Entity table), `repository/`, `service/` | `RegionPriceEntity`, `RegionPriceRepository` |
| Fix N+1 query | `repository/` - add `@EntityGraph` or fetch join | `PriceListRepository.findByIdAndNotDeletedWithSkuPrices()` |
| Add audit trail | `entity/` - add created_by/updated_by fields | `SkuPriceEntity` line 54-58 |
| Implement price calculation | Service layer (not entity layer) | Extend `PriceListService` with calculation logic |
| Add cache key pattern | Document in README Performance section | `price:sku:{skuId}:segment:{segment}` |
| New API endpoint | `controller/` - add @GetMapping/@PostMapping | `PricingController` |

---

## Critical Integration Points

### 1. Cache Invalidation
When prices change, invalidate Redis keys following pattern:
```
price:sku:{skuId}*  // wildcard for all price variants
```

### 2. Event Publishing
Use `OutboxEventRepository` for price changes so consumer services (Order, Billing, Catalog) stay in sync.

### 3. Currency Conversion
Always use `ExchangeRateRepository` effective_date logic for rates—exchange rates are time-sensitive.

### 4. Lazy Relationships
Default is LAZY on ManyToOne/OneToMany. Don't forget `@EntityGraph` or risk N+1 in production.

### 5. Deleted Record Handling
Soft deletes mean `deleted = true` records still exist in DB. Always filter in queries.

---

## Non-Functional Requirements

- **Availability**: 99.99%
- **Single SKU Lookup**: < 50ms (via cache)
- **Bulk Lookup**: < 200ms (via cache + batch queries)
- **Scale**: 5,000,000+ SKUs, 100,000+ Products, 50,000+ req/min

Use Redis heavily for effective price caching given these targets.

---

## Future Context

See `README.md` line 591-602 for enhancement roadmap (AI-based pricing, demand forecasting, multi-tenant support, GraphQL, price approval workflow).

Currently there is **NO facade/orchestration layer**—pricing calculation logic should be added to service layer methods as features expand.

