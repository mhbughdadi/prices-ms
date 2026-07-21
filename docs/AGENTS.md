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
10. Return Effective Price and old price before discounts

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
import static com.apogee.common.mapper.ObjectMapper.transform;
import static com.apogee.common.mapper.ObjectMapper.transformCollection;

Category savedCategory = this.categoryService.addCategory(transform(categoryDto, Category.class));
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
Conversion methods (`transform()`) are **always in service layer**, never in repositories.

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


# AGENTS.md - AI Agent Guidance for Products Microservice

## Quick Start for AI Agents

This is a **Spring Boot 3.3.5 + Java 21 microservice** managing products, categories, tags, and SKUs via REST APIs. Agents should understand the layered architecture and the specific patterns used here before making changes.

---

## 1. Architecture & Layers (Data Flow)

### Layer Structure: Controller → Backing Service (Facade) → Service → Repository → Database

```
HTTP Request
    ↓
@RestController (thin orchestration layer)
    ↓
BackingService (adapts input/output DTOs, orchestrates services)
    ↓
@Service @Transactional (business logic + transactions)
    ↓
JPA Repository (SQL queries via Spring Data)
    ↓
MySQL Database (via application.properties datasource)
```

**Key files:**
- Controllers: `src/main/java/com/apogee/product/controller/` (HTTP entry points)
- Backing Services: `src/main/java/com/apogee/product/backingservice/` (API orchestration)
- Services: `src/main/java/com/apogee/product/services/` + `impl/` (business logic)
- Repositories: `src/main/java/com/apogee/product/repositories/` (Spring Data JPA)

**Example flow (get product by ID):**
1. `ProductController.getProductById(Long id)` → calls `productsBackingService`
2. `ProductsBackingService.getProductById(id)` → calls `productService.findProductById(id)`
3. `ProductServiceImpl.findProductById(id)` → queries `ProductRepository`, transforms entity→model
4. Response transformed to `FindProductResponseDto` and returned as `ResponseEntity`

### DTO Layers
- **Input DTOs**: `dtos/inputs/` (e.g., `ProductDto`) — map HTTP request body to domain objects
- **Output DTOs**: `dtos/output/` (e.g., `ProductOutputDto`) — map domain objects to JSON response
- **Models**: `models/` (e.g., `Product`) — intermediate domain representation
- **Entities**: `entities/` (e.g., `ProductEntity`) — JPA entities (DB rows)

---

## 2. Critical Project-Specific Conventions

### 2.1 Constants in Non-Instantiable Classes

All error messages, logging keys, and constants are stored in `ProductsConstant` (cannot be instantiated):

```java
// ✅ DO THIS: ProductsConstant.java
package com.apogee.product.constants;

public final class ProductsConstant {
    private ProductsConstant() {}  // Prevents instantiation
    
    public static final String ERROR_RECORD_NOT_FOUND = "record.not.found";
    public static final String ERROR_PRODUCT_IMAGE_NOT_FOUND = "errors.product.image.not.found";
    public static final String REQUEST_ID = "requestId";
    public static final String X_REQUEST_ID = "X-Request-Id";
}
```

**Usage in services:**
```java
import static com.apogee.product.constants.ProductsConstant.ERROR_RECORD_NOT_FOUND;

throw new RecordNotFoundException(ERROR_RECORD_NOT_FOUND, productId);
```

### 2.2 Exception Handling Pattern

Only **two custom exceptions** are used; throw with i18n keys from `errors_en.properties` / `errors_ar.properties`:

#### RecordNotFoundException
```java
// Not found errors — includes record ID for logging
public RecordNotFoundException(String message, Long recordId) { /*...*/ }
public RecordNotFoundException(String message, Long... recordIds) { /*...*/ }

// Usage:
if (!productExists) {
    throw new RecordNotFoundException(ERROR_RECORD_NOT_FOUND, productId);
}
```

#### DBException
```java
// Database operation errors
public DBException(String message, Class<?> entityClass, Long... recordIds) { /*...*/ }

// Usage:
try {
    tagRepository.save(tag);
} catch (Exception e) {
    throw new DBException("Tag save failed", TagEntity.class, tagId, e);
}
```

Both exceptions are caught by `GlobalExceptionHandler` which converts them to localized `FailureResponse` (English + Arabic).

### 2.3 Mapper Utility (Custom Field-by-Name Mapping)

Uses a **custom `Mapper` utility** (not Dozer/ModelMapper) that:
- Maps fields by **name only** (case-sensitive)
- Requires **no-arg constructor** on destination class
- Supports **nested collection mapping** and **callback functions**
- Is **cached internally** for performance

```java
import static com.apogee.common.mapper.ObjectMapper.transform;
import static com.apogee.common.mapper.ObjectMapper.transformCollection;

// Simple 1-to-1 mapping
ProductOutputDto dto = transform(productEntity, ProductOutputDto.class);

// Collection mapping
List<ProductOutputDto> dtos = transformCollection(productEntities, ProductOutputDto.class);

// Mapping with callback for custom logic
List<Product> products = transformCollection(
    productEntities, 
    Product.class, 
    this::enrichProductWithTags  // callback method
);

// Private callback method
private Product enrichProductWithTags(ProductEntity entity, Product model) {
    model.setId(entity.getId());
    model.setTags(transformCollection(entity.getTags(), Tag.class)); // nested
    return model;
}
```

**⚠️ CRITICAL:** Mapper silently ignores unmatched fields. When adding a new field:
1. Add it to **both** the source class AND destination class (same field name)
2. Add tests to verify the field is mapped

### 2.4 Service → BackingService Pattern

Services are **purely business logic** (no HTTP awareness). BackingServices handle **DTO conversions**:

```java
// ❌ WRONG: Service with DTO conversion
@Service
public class ProductServiceImpl {
    public ProductOutputDto getProduct(Long id) { /*...*/ }  // Wrong layer!
}

// ✅ CORRECT: Service returns domain model
@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    public Product findProductById(Long id) throws RecordNotFoundException { /*...*/ }
}

// ✅ CORRECT: BackingService adapts DTOs
@Service
public class ProductsBackingService {
    @Autowired private ProductService productService;
    
    public FindProductResponseDto getProductById(Long id) throws MapperException, RecordNotFoundException {
        Product product = productService.findProductById(id);  // Get domain model
        return new FindProductResponseDto()
            .setProduct(transform(product, ProductOutputDto.class));  // Adapt to DTO
    }
}
```

### 2.5 Logging via Filter (RequestId Correlation)

Logging is handled by `CorrelationFilter` which:
- Generates a unique `requestId` (UUID) per HTTP request
- Stores it in SLF4J MDC (Mapped Diagnostic Context)
- Adds it to response header (`X-Request-Id`)

```java
// CorrelationFilter automatically sets MDC and response header
// In logs, requestId appears via `%X{REQUEST_ID}` in log4j2.xml

// Controllers can access requestId if needed:
@GetMapping("/products/{productId}")
public ResponseEntity<Response> getProduct(
    @PathVariable Long productId,
    HttpServletRequest request  // Can extract if needed
) { /*...*/ }
```

### 2.6 OpenAPI / Swagger Annotations

Controllers use **OpenAPI 3.0 annotations** (not Swagger 2). All error responses reference shared schemas:

```java
@GetMapping("/products/{productId}")
@Operation(summary = "Get product by ID", description = "Retrieves a specific product")
@ApiResponse(
    responseCode = "200", 
    description = "Success", 
    content = @Content(schema = @Schema(implementation = FindProductResponseDto.class))
)
@ApiResponse(responseCode = "404", ref = "#/components/schemas/FailureResponse")  // Shared error schema
@ApiResponse(responseCode = "500", ref = "#/components/schemas/FailureResponse")
public ResponseEntity<Response> getProduct(@PathVariable Long productId) throws MapperException, RecordNotFoundException {
    FindProductResponseDto response = productsBackingService.getProductById(productId);
    return new ResponseEntity<>(response, HttpStatus.OK);
}
```

Shared error schemas are pre-configured in `OpenApiConfigurations.java` (injected via `components.addSchemas(...)`).

---

## 3. File Structure & Naming Conventions

```
src/main/java/com/apogee/product/
├── controller/               # @RestController endpoints
│   ├── ProductController.java
│   ├── CategoryController.java
│   └── ...
├── backingservice/          # API orchestration layer (no HTTP deps)
│   ├── ProductsBackingService.java
│   └── ...
├── services/                # Service interfaces
│   ├── ProductService.java
│   ├── CategoryService.java
│   └── impl/               # Service implementations (@Transactional, @Service)
│       ├── ProductServiceImpl.java
│       └── ...
├── repositories/            # Spring Data JPA repos
│   ├── ProductRepository.java
│   └── ...
├── entities/               # JPA entities (@Entity, @Table)
│   ├── ProductEntity.java
│   └── ...
├── models/                 # Domain models (no annotations)
│   ├── Product.java
│   └── ...
├── dtos/
│   ├── inputs/            # Request DTOs
│   │   ├── ProductDto.java
│   │   └── ...
│   └── output/            # Response DTOs
│       ├── ProductOutputDto.java
│       └── ...
├── constants/             # Non-instantiable constant classes
│   └── ProductsConstant.java
├── exceptions/            # Custom exceptions
│   ├── RecordNotFoundException.java
│   ├── DBException.java
│   └── ...
├── configs/              # Spring @Configuration beans
│   ├── OpenApiConfigurations.java
│   └── ...
├── filters/              # Servlet filters
│   └── CorrelationFilter.java
└── ProductApplication.java  # @SpringBootApplication entry point
```

---

## 4. Build & Run Commands

**Local development:**
```bash
# Clean build (fastest for full rebuild)
 mvn clean package -DskipTests

# Run locally with Spring profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run tests
mvn test

# Build Docker image
docker build -t products-ms:latest .
```

**Docker runtime environment variables:**
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/products_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password
```

Default port: **9090** (or 9091 for dev profile with SQL logging).

---

## 5. Key Dependencies & Integration Points

| Dependency | Purpose | Usage |
|------------|---------|-------|
| **Spring Boot 3.3.5** | REST framework, DI, auto-config | `@RestController`, `@Service`, `@Autowired` |
| **Spring Data JPA** | Persistence abstraction | `@Repository` interfaces extending `JpaRepository` |
| **MySQL Connector/J** | JDBC driver | Configured in `application.properties` |
| **Log4j2** | Structured logging | `log4j2.xml` configuration (sends to Logstash) |
| **Lombok** | Reduce boilerplate | `@Getter`, `@Setter`, `@Transactional` |
| **OpenAPI 3.0** | API documentation | `@Operation`, `@ApiResponse`, `@Schema` |
| **Custom Mapper** | DTO↔Model↔Entity mapping | `transform()`, `transformCollection()` from `ObjectMapper` |
| **Custom exceptions** | Error handling | `RecordNotFoundException`, `DBException` |

---

## 6. Error Handling Flow

```
Service throws RecordNotFoundException(ERROR_RECORD_NOT_FOUND, productId)
    ↓
GlobalExceptionHandler catches it
    ↓
Extracts message key "record.not.found"
    ↓
Looks up ResourceBundle (errors_en.properties / errors_ar.properties)
    ↓
Returns FailureResponse with localized messages (English + Arabic)
    ↓
ResponseEntity with 404 status code
```

**Add error messages:**
1. Add constant to `ProductsConstant.java`
2. Add key=value to `src/main/resources/errors_en.properties`
3. Add key=value to `src/main/resources/errors_ar.properties`
4. Use in service: `throw new RecordNotFoundException(MY_ERROR_KEY, recordId)`

---

## 7. Testing Patterns

**Unit test structure (JUnit 5 + Mockito, no Spring context):**

```java
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl service;

    @Test
    void findCategoryById_returnsCategoryWhenExists() throws MapperException, RecordNotFoundException {
        // Arrange
        Long categoryId = 1L;
        CategoryEntity entity = new CategoryEntity();
        entity.setId(categoryId);
        entity.setName("Electronics");
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(entity));

        // Act
        Category result = service.findCategoryByID(categoryId);

        // Assert
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    void findCategoryById_throwsNotFoundWhenMissing() {
        // Arrange
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, 
            () -> service.findCategoryByID(categoryId),
            ERROR_RECORD_NOT_FOUND);
    }
}
```

---

## 8. Common Agent Tasks & Patterns

### Add a New REST Endpoint

1. **Define input/output DTOs** (`dtos/inputs/NewResourceDto.java`, `dtos/output/NewResourceResponseDto.java`)
2. **Add Controller method** with `@GetMapping`, `@PostMapping`, etc., + OpenAPI annotations
3. **Add BackingService method** to orchestrate (DTO → model conversion)
4. **Add Service method** with `@Transactional` (business logic, throws exceptions)
5. **Add Repository method** if needed (Spring Data JPA custom queries)
6. **Add error constants** to `ProductsConstant` and localization files
7. **Test**: Unit test the service (no Spring context), API integration test optional

### Add a New Field to an Entity

1. Add to `*Entity.java` (JPA entity) — add `@Column` if needed
2. Add to `*Dto.java` (input) and `*OutputDto.java` (output) — same field **name**
3. Add to `*Model.java` (domain model) — same field **name**
4. **Create/update test** to verify mapping with `Mapper.transform()`
5. **Migration**: If persisting, add liquibase/flyway changeset

### Map Related Entities (e.g., Products → Tags)

Use **callback function** with `transformCollection`:

```java
// In ProductServiceImpl
private Product enrichProductWithTags(ProductEntity entity, Product model) {
    model.setId(entity.getId());
    model.setTags(transformCollection(entity.getTags(), Tag.class, this::mapTag));
    return model;
}

private Tag mapTag(TagEntity entity, Tag model) {
    model.setId(entity.getId());
    model.setName(entity.getName());
    return model;
}

// When calling service:
List<Product> products = transformCollection(
    productEntities, 
    Product.class, 
    this::enrichProductWithTags
);
```

---

## 9. Debugging & Troubleshooting Tips

| Issue | Cause | Solution |
|-------|-------|----------|
| Mapper silently returns null/empty fields | Field name mismatch (case-sensitive) | Check both source & destination have **exact** field names |
| `RecordNotFoundException` not localized | Error key not in `errors_en.properties` | Add the key to both `.properties` files |
| OpenAPI docs show wrong schema | Schema not added to `OpenApiConfigurations` | Add to `components.addSchemas()` |
| Controller method logged incorrectly | `@PathVariable` / `@RequestBody` removed | Keep parameter annotations for `CorrelationFilter` logging |
| Tests fail: entity not found | Mocking incomplete | Ensure `when(repo.findById(...)).thenReturn(Optional.of(...))` |
| Request hangs | Database connection issue | Check `application-dev.properties` datasource URL |

---

## 10. Token-Saving Tips for New Projects

1. **Copy the file structure exactly** — reuse `constants/`, `services/impl/`, `backingservice/`, `dtos/` layout
2. **Use the same `Mapper` utility** — don't introduce new mapping libraries
3. **Pre-configure OpenAPI** in `configs/OpenApiConfigurations.java` once, reference shared error schemas
4. **Centralize constants** — all strings in `*Constant.java` (prevents duplication in agents' searches)
5. **Minimal backing services** — only adapt DTOs, delegate to services (reduces agent confusion on where to put logic)
6. **Test templates** — copy `CategoryServiceImplTest` pattern for all new services
7. **Use exceptions consistently** — only `RecordNotFoundException` + `DBException` for all errors
8. **Avoid custom filters** — use existing `CorrelationFilter` pattern
9. **Document deviations EARLY** — any deviation from layers wastes agent tokens investigating "why"
10. **Lombok annotations everywhere** — `@Getter`, `@Setter`, `@Transactional` reduce code agents need to read

---

## 11. Quick Reference: What Agents Should Know

| Question | Answer | Key File |
|----------|--------|----------|
| Where do REST endpoints go? | `controller/` — keep thin, delegate to BackingService | `ProductController.java` |
| Where does DTO→model conversion happen? | `backingservice/` — this is the semantic adapter layer | `ProductsBackingService.java` |
| Where does business logic go? | `services/impl/` — mark with `@Transactional`, throw exceptions | `ProductServiceImpl.java` |
| How do I map objects? | Use `Mapper.transform()` / `transformCollection()` with callbacks | `README.md` line 104+ |
| How do I throw errors? | Use constants from `ProductsConstant`, throw `RecordNotFoundException` / `DBException` | `constants/ProductsConstant.java` |
| How do I test services? | JUnit 5 + Mockito, no Spring context, mock repositories | `CategoryServiceImplTest.java` |
| How do I document APIs? | OpenAPI `@Operation`, `@ApiResponse`, `@Schema` annotations | `CategoryController.java` line 81+ |
| How do I correlate logs? | Use `CorrelationFilter` — it sets requestId in MDC automatically | `CorrelationFilter.java` |
| What's the port? | 9090 (9091 for dev profile with SQL logging) | `application-dev.properties` |
| How do I run locally? | `./mvnw spring-boot:run` or `java -jar target/products-ms-*.jar` | `README.md` line 76+ |

---

**Last Updated:** June 8, 2026  
**For:** GitHub Copilot / AI Agents working on products-ms microservice  
**Source Files Analyzed:** 15+ (controllers, services, DTOs, entities, configs, utilities)


