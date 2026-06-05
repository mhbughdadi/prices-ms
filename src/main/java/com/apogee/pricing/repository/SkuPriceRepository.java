package com.apogee.pricing.repository;

import com.apogee.pricing.entity.PriceListEntity;
import com.apogee.pricing.entity.SkuPriceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkuPriceRepository extends JpaRepository<SkuPriceEntity, UUID> {

    /**
     * Find price for SKU in a price list
     */
    @Query("select sp from SkuPriceEntity sp where sp.skuId = :skuId and sp.priceList.id = :priceListId and sp.deleted = false")
    Optional<SkuPriceEntity> findBySkuAndPriceList(@Param("skuId") UUID skuId, @Param("priceListId") UUID priceListId);

    /**
     * Find all prices for a SKU
     */
    @Query("select sp from SkuPriceEntity sp where sp.skuId = :skuId and sp.deleted = false order by sp.priceList.priority asc")
    List<SkuPriceEntity> findBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find all prices for a SKU with pagination
     */
    @Query("select sp from SkuPriceEntity sp where sp.skuId = :skuId and sp.deleted = false order by sp.priceList.priority asc")
    Page<SkuPriceEntity> findBySkuId(@Param("skuId") UUID skuId, Pageable pageable);

    /**
     * Find active prices for a SKU
     */
    @Query("select sp from SkuPriceEntity sp where sp.skuId = :skuId and sp.active = true and sp.deleted = false order by sp.priceList.priority asc")
    List<SkuPriceEntity> findActiveBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find all prices for a price list
     */
    List<SkuPriceEntity> findByPriceList(PriceListEntity priceList);

    /**
     * Find all prices for a price list with pagination
     */
    Page<SkuPriceEntity> findByPriceList(PriceListEntity priceList, Pageable pageable);

    /**
     * Find prices in a price range
     */
    @Query("select sp from SkuPriceEntity sp where sp.regularPrice between :minPrice and :maxPrice and sp.deleted = false order by sp.regularPrice asc")
    List<SkuPriceEntity> findByRegularPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find prices in a price range with pagination
     */
    @Query("select sp from SkuPriceEntity sp where sp.regularPrice between :minPrice and :maxPrice and sp.deleted = false order by sp.regularPrice asc")
    Page<SkuPriceEntity> findByRegularPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    /**
     * Find prices with sale price available
     */
    @Query("select sp from SkuPriceEntity sp where sp.salePrice is not null and sp.deleted = false order by sp.salePrice asc")
    List<SkuPriceEntity> findWithSalePrice();

    /**
     * Find prices with sale price available and pagination
     */
    @Query("select sp from SkuPriceEntity sp where sp.salePrice is not null and sp.deleted = false order by sp.salePrice asc")
    Page<SkuPriceEntity> findWithSalePrice(Pageable pageable);

    /**
     * Find prices with sale price in a range
     */
    @Query("select sp from SkuPriceEntity sp where sp.salePrice is not null and sp.salePrice between :minPrice and :maxPrice and sp.deleted = false order by sp.salePrice asc")
    List<SkuPriceEntity> findBySalePriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find prices with cost price available
     */
    @Query("select sp from SkuPriceEntity sp where sp.costPrice is not null and sp.deleted = false order by sp.costPrice asc")
    List<SkuPriceEntity> findWithCostPrice();

    /**
     * Find prices valid on a specific date
     */
    @Query("select sp from SkuPriceEntity sp where sp.active = true and sp.deleted = false and (sp.startDate is null or sp.startDate <= :date) and (sp.endDate is null or sp.endDate >= :date)")
    List<SkuPriceEntity> findValidOnDate(@Param("date") OffsetDateTime date);

    /**
     * Find prices within a date range
     */
    @Query("select sp from SkuPriceEntity sp where sp.deleted = false and (sp.startDate is null or sp.startDate <= :endDate) and (sp.endDate is null or sp.endDate >= :startDate) order by sp.startDate asc")
    List<SkuPriceEntity> findByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Find upcoming prices (start date in future)
     */
    @Query("select sp from SkuPriceEntity sp where sp.startDate > current_timestamp and sp.deleted = false order by sp.startDate asc")
    List<SkuPriceEntity> findUpcoming();

    /**
     * Find ongoing prices (current date is within valid range)
     */
    @Query("select sp from SkuPriceEntity sp where sp.active = true and sp.deleted = false and (sp.startDate is null or sp.startDate <= current_timestamp) and (sp.endDate is null or sp.endDate >= current_timestamp) order by sp.priceList.priority asc")
    List<SkuPriceEntity> findOngoing();

    /**
     * Find expired prices
     */
    @Query("select sp from SkuPriceEntity sp where sp.endDate < current_timestamp and sp.deleted = false order by sp.endDate desc")
    List<SkuPriceEntity> findExpired();

    /**
     * Find prices modified after a specific date
     */
    @Query("select sp from SkuPriceEntity sp where sp.updatedAt >= :updatedAfter and sp.deleted = false order by sp.updatedAt desc")
    List<SkuPriceEntity> findModifiedAfter(@Param("updatedAfter") OffsetDateTime updatedAfter);

    /**
     * Find highest regular price for SKU
     */
    @Query("select sp from SkuPriceEntity sp where sp.skuId = :skuId and sp.deleted = false order by sp.regularPrice desc")
    Optional<SkuPriceEntity> findHighestRegularPriceBySku(@Param("skuId") UUID skuId);

    /**
     * Find lowest regular price for SKU
     */
    @Query("select sp from SkuPriceEntity sp where sp.skuId = :skuId and sp.deleted = false order by sp.regularPrice asc")
    Optional<SkuPriceEntity> findLowestRegularPriceBySku(@Param("skuId") UUID skuId);

    /**
     * Find prices with discount (sale price lower than regular price)
     */
    @Query("select sp from SkuPriceEntity sp where sp.salePrice is not null and sp.salePrice < sp.regularPrice and sp.deleted = false order by sp.skuId asc")
    List<SkuPriceEntity> findWithDiscount();

    /**
     * Find prices for a price list with eager load of associated data
     */
    @Query("select sp from SkuPriceEntity sp left join fetch sp.priceList pl where pl.id = :priceListId and sp.deleted = false")
    List<SkuPriceEntity> findByPriceListIdWithRelations(@Param("priceListId") UUID priceListId);

    /**
     * Count prices for a SKU
     */
    @Query("select count(sp) from SkuPriceEntity sp where sp.skuId = :skuId and sp.deleted = false")
    long countBySkuId(@Param("skuId") UUID skuId);

    /**
     * Count active prices for a SKU
     */
    @Query("select count(sp) from SkuPriceEntity sp where sp.skuId = :skuId and sp.active = true and sp.deleted = false")
    long countActiveBySkuId(@Param("skuId") UUID skuId);

    /**
     * Count prices in a price list
     */
    long countByPriceList(PriceListEntity priceList);

    /**
     * Check if price exists for SKU in price list
     */
    @Query("select case when count(sp) > 0 then true else false end from SkuPriceEntity sp where sp.skuId = :skuId and sp.priceList.id = :priceListId and sp.deleted = false")
    boolean existsForSkuInPriceList(@Param("skuId") UUID skuId, @Param("priceListId") UUID priceListId);

    /**
     * Get average regular price for a SKU
     */
    @Query("select avg(sp.regularPrice) from SkuPriceEntity sp where sp.skuId = :skuId and sp.deleted = false")
    BigDecimal getAverageRegularPrice(@Param("skuId") UUID skuId);

    /**
     * Get average sale price for SKUs with sale price
     */
    @Query("select avg(sp.salePrice) from SkuPriceEntity sp where sp.salePrice is not null and sp.deleted = false")
    BigDecimal getAverageSalePrice();

    /**
     * Get maximum regular price for a price list
     */
    @Query("select max(sp.regularPrice) from SkuPriceEntity sp where sp.priceList.id = :priceListId and sp.deleted = false")
    BigDecimal getMaxRegularPriceInPriceList(@Param("priceListId") UUID priceListId);

    /**
     * Get minimum regular price for a price list
     */
    @Query("select min(sp.regularPrice) from SkuPriceEntity sp where sp.priceList.id = :priceListId and sp.deleted = false")
    BigDecimal getMinRegularPriceInPriceList(@Param("priceListId") UUID priceListId);

    /**
     * Find prices modified by a specific user
     */
    @Query("select sp from SkuPriceEntity sp where sp.updatedBy = :updatedBy and sp.deleted = false order by sp.updatedAt desc")
    List<SkuPriceEntity> findModifiedBy(@Param("updatedBy") String updatedBy);

    /**
     * Find prices created by a specific user
     */
    @Query("select sp from SkuPriceEntity sp where sp.createdBy = :createdBy and sp.deleted = false order by sp.createdAt desc")
    List<SkuPriceEntity> findCreatedBy(@Param("createdBy") String createdBy);
}

