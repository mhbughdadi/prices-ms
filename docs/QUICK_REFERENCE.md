# Quick Reference - Repository Query Methods

## Common Query Patterns Used

### 1. **Find by ID with Relations**
```java
// Eager load related entities
@Query("select entity from Entity entity left join fetch entity.relation where entity.id = :id")
Optional<Entity> findByIdWithRelations(@Param("id") UUID id);
```

### 2. **Find All Active Ordered by Priority**
```java
@Query("select entity from Entity entity where entity.active = true order by entity.priority asc")
List<Entity> findAllActive();
```

### 3. **Find by Date Range**
```java
@Query("select entity from Entity entity where entity.createdAt between :start and :end order by entity.createdAt desc")
List<Entity> findByDateRange(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);
```

### 4. **Text Search Case-Insensitive**
```java
@Query("select entity from Entity entity where upper(entity.name) like upper(concat('%', :name, '%'))")
List<Entity> findByNameContaining(@Param("name") String name);
```

### 5. **Current Date Filtering**
```java
// Find currently valid items
@Query("select entity from Entity entity where entity.startDate <= current_timestamp and entity.endDate >= current_timestamp")
List<Entity> findOngoing();

// Find future items
@Query("select entity from Entity entity where entity.startDate > current_timestamp order by entity.startDate asc")
List<Entity> findUpcoming();
```

### 6. **Aggregation Queries**
```java
@Query("select avg(entity.price) from Entity entity where entity.active = true")
BigDecimal getAveragePrice();

@Query("select count(entity) from Entity entity where entity.active = true")
long countActive();
```

### 7. **Batch Operations**
```java
@Modifying
@Transactional
@Query("update Entity entity set entity.active = false where entity.id in :ids")
void deactivateMultiple(@Param("ids") List<UUID> ids);
```

### 8. **Existence Checks**
```java
@Query("select case when count(entity) > 0 then true else false end from Entity entity where entity.code = :code")
boolean existsByCode(@Param("code") String code);
```

---

## Repository-Specific Quick Methods

### PriceListRepository
- `findAllActive()` - Get active price lists
- `findByCode(code)` - Find by unique code
- `findValidOnDate(date)` - Prices valid on date
- `findCurrencyId(currencyId)` - By currency

### SkuPriceRepository  
- `findBySkuAndPriceList()` - Specific SKU in list
- `findActiveBySkuId(skuId)` - All active prices
- `findWithDiscount()` - Prices with sale < regular
- `findOngoing()` - Currently valid prices

### TierPriceRepository
- `findBySkuAndQuantity(sku, qty)` - Tier for quantity
- `findActiveBySkuId(skuId)` - All tiers for SKU
- `hasOverlappingTiers(skuId)` - Quality check
- `getAverageUnitPriceBySku(skuId)` - Statistics

### RegionPriceRepository
- `findBySkuAndCountry(sku, country)` - Country price
- `findCountriesBySkuId(sku)` - Coverage check
- `getMaxPriceByCountry(country)` - Price range
- `getMinPriceByCountry(country)` - Price range

### SegmentPriceRepository
- `findBySkuAndSegment(sku, segment)` - Segment price
- `findActiveBySkuId(skuId)` - All for segment
- `findActiveOnDate(date)` - Valid on date
- `getAveragePriceBySegment(segment)` - Statistics

### PriceEventRepository
- `findBySkuId(skuId)` - All events for SKU
- `findOngoing()` - Currently active events
- `findUpcoming()` - Future events
- `findByEventType(type)` - By event type

### PricingRuleRepository
- `findAllActive()` - All active rules
- `findByHighestPriority()` - Ordered by priority
- `findByConditionContaining(term)` - Search JSONB
- `findByRuleNameContaining(name)` - Name search

### TaxRuleRepository
- `findFirstActiveByCountryCode(code)` - Primary rule
- `findAllCountryCodes()` - Available countries
- `getAverageTaxRateByCountry(country)` - Tax stats
- `findByTaxType(type)` - INCLUSIVE/EXCLUSIVE

### CurrencyRepository
- `findByCode(code)` - Find by ISO code
- `findAllActive()` - All active currencies
- `findByNameContaining(name)` - Name search
- `existsByCode(code)` - Existence check

### ExchangeRateRepository
- `findByCurrencyPair(source, target)` - Exchange rate
- `findLatestByCurrencyPair()` - Most recent rate
- `findCurrencyPairHistory()` - Rate history
- `deleteOutdatedRates(cutoff)` - Cleanup

### OutboxEventRepository
- `findUnprocessed()` - Pending events
- `findByAggregateId(id)` - All for aggregate
- `markAsProcessed(id)` - Single or batch
- `deleteProcessedBefore(date)` - Cleanup

### PriceAuditRepository  
- `findBySkuId(skuId)` - All changes for SKU
- `findPriceIncreases(skuId)` - Only increases
- `findPriceDecreases(skuId)` - Only decreases
- `findLatestBySkuId(skuId)` - Most recent change

### BundleItemRepository
- `findByBundleId(bundleId)` - Items in bundle
- `findBySkuId(skuId)` - Bundles with SKU
- `existsInBundle(bundle, sku)` - Membership check
- `countByBundleId(bundleId)` - Item count

### PriceBundleRepository
- `findByBundleCode(code)` - Find by code
- `findAllActive()` - Active bundles
- `findByPriceRange(min, max)` - Price filter
- `getTotalBundlePrice()` - Sum statistics

### CustomerSegmentRepository
- `findByCode(code)` - By enum code
- `findAllActive()` - All active segments
- `findByCodeWithPrices()` - Eager load prices
- `countSegmentPrices(segmentId)` - Price count

---

## Common Spring Data JPA Patterns

### Pagination
```java
Page<Entity> page = repository.findAllActive(PageRequest.of(0, 20));
List<Entity> content = page.getContent();
long total = page.getTotalElements();
```

### Sorting
```java
List<Entity> sorted = repository.findAll(Sort.by("priority").ascending());
List<Entity> multiSort = repository.findAll(
    Sort.by("priority", "name").ascending()
);
```

### Optional Handling
```java
Optional<Entity> opt = repository.findById(id);
Entity entity = opt.orElse(null);
Entity entity = opt.orElseThrow(() -> new NotFoundException("Not found"));
```

### List Results
```java
List<Entity> results = repository.findAllActive();
if (results.isEmpty()) {
    // handle empty
}
```

### Count
```java
long activeCount = repository.countByActive(true);
boolean exists = repository.existsByCode("TEST");
```

---

## Transactional Considerations

All batch delete/update operations are marked with:
```java
@Modifying
@Transactional
```

This ensures:
- ✅ Changes are flushed to database
- ✅ Transaction context is maintained
- ✅ Proper exception handling
- ✅ Rollback on failure

---

## Performance Tips

1. **Use Pagination**: Always page large result sets
   ```java
   Page<Entity> page = repo.findAll(PageRequest.of(0, 100));
   ```

2. **Eager Load Relations**: Use fetch joins for known relations
   ```java
   findByIdWithRelations(id)  // Instead of findById(id)
   ```

3. **Filter Early**: Use repository queries, not in-memory filtering
   ```java
   List<Entity> filtered = repo.findActiveBySkuId(skuId);  // Good
   // NOT: findBySkuId(skuId).stream().filter(e -> e.isActive())  // Bad
   ```

4. **Use Indexes**: Queries respect database indexes
   - `sku_id`
   - `country_code`
   - `active, start_date, end_date`
   - etc.

5. **Aggregation in DB**: Use repository aggregation methods
   ```java
   BigDecimal avg = repo.getAveragePrice();  // Good - in database
   // NOT: list.stream().mapToDouble(Entity::getPrice).average()  // Bad - in memory
   ```

---

## Common Issues & Solutions

### Issue: N+1 Query Problem
**Solution**: Use `@EntityGraph` or fetch join
```java
@Query("select entity from Entity entity left join fetch entity.children")
List<Entity> findWithChildren();
```

### Issue: Lazy Loading Exception
**Solution**: Use `@Transactional` or eager load
```java
@Transactional
public void processEntity(UUID id) {
    Entity entity = repo.findByIdWithRelations(id);
    // Access relations without lazy loading issues
}
```

### Issue: Too Many Results
**Solution**: Add pagination or filtering
```java
Page<Entity> page = repo.findAllActive(PageRequest.of(0, 100));
```

### Issue: Case-Sensitive Search Not Working
**Solution**: Use `upper()` in query
```java
@Query("select e from Entity e where upper(e.name) = upper(:name)")
Optional<Entity> findByNameIgnoreCase(@Param("name") String name);
```

---

## Repository Dependency Injection

```java
// In Service class
@Service
public class MyService {
    
    @Autowired
    private PriceListRepository priceListRepository;
    
    @Autowired
    private SkuPriceRepository skuPriceRepository;
    
    public void myMethod() {
        List<PriceList> lists = priceListRepository.findAllActive();
    }
}
```

---

## Testing Repositories

```java
@SpringBootTest
public class PriceListRepositoryTest {
    
    @Autowired
    private PriceListRepository repository;
    
    @Test
    void testFindAllActive() {
        List<PriceList> active = repository.findAllActive();
        assertFalse(active.isEmpty());
    }
}
```

---

Updated: June 6, 2026  
All Repositories: ✅ Production Ready  
Entity Implementation: CurrencyEntity added ✅

