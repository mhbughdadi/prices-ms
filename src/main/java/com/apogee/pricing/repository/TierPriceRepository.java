package com.apogee.pricing.repository;

import com.apogee.pricing.entity.TierPriceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TierPriceRepository extends JpaRepository<TierPriceEntity, UUID> {

    /**
     * Find tier price for SKU at specific quantity
     */
    @Query("select tp from TierPriceEntity tp where tp.skuId = :skuId and tp.minQuantity <= :quantity and (tp.maxQuantity is null or tp.maxQuantity >= :quantity) and tp.active = true")
    Optional<TierPriceEntity> findBySkuAndQuantity(@Param("skuId") UUID skuId, @Param("quantity") Integer quantity);

    /**
     * Find all tier prices for a SKU
     */
    @Query("select tp from TierPriceEntity tp where tp.skuId = :skuId order by tp.minQuantity asc")
    List<TierPriceEntity> findBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find all active tier prices for a SKU
     */
    @Query("select tp from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true order by tp.minQuantity asc")
    List<TierPriceEntity> findActiveBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find all active tier prices for a SKU with pagination
     */
    @Query("select tp from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true order by tp.minQuantity asc")
    Page<TierPriceEntity> findActiveBySkuId(@Param("skuId") UUID skuId, Pageable pageable);

    /**
     * Find tier prices starting at a minimum quantity
     */
    @Query("select tp from TierPrice tp where tp.skuId = :skuId and tp.minQuantity >= :minQuantity order by tp.minQuantity asc")
    List<TierPriceEntity> findBySkuAndMinQuantityGreaterThanOrEqual(@Param("skuId") UUID skuId, @Param("minQuantity") Integer minQuantity);

    /**
     * Find tier prices for quantity range
     */
    @Query("select tp from TierPriceEntity tp where tp.minQuantity <= :maxQuantity and (tp.maxQuantity is null or tp.maxQuantity >= :minQuantity) order by tp.minQuantity asc")
    List<TierPriceEntity> findByQuantityRange(@Param("minQuantity") Integer minQuantity, @Param("maxQuantity") Integer maxQuantity);

    /**
     * Find tier prices in a price range
     */
    @Query("select tp from TierPriceEntity tp where tp.unitPrice between :minPrice and :maxPrice order by tp.unitPrice asc")
    List<TierPriceEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find tier prices in a price range with pagination
     */
    @Query("select tp from TierPriceEntity tp where tp.unitPrice between :minPrice and :maxPrice order by tp.unitPrice asc")
    Page<TierPriceEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    /**
     * Find tier prices with unit price greater than
     */
    @Query("select tp from TierPriceEntity tp where tp.unitPrice > :price order by tp.unitPrice desc")
    List<TierPriceEntity> findByUnitPriceGreaterThan(@Param("price") BigDecimal price);

    /**
     * Find tier prices with unit price less than
     */
    @Query("select tp from TierPriceEntity tp where tp.unitPrice < :price order by tp.unitPrice asc")
    List<TierPriceEntity> findByUnitPriceLessThan(@Param("price") BigDecimal price);

    /**
     * Find tier with all tiers for SKU with pagination
     */
    @Query("select tp from TierPriceEntity tp where tp.active = true order by tp.minQuantity asc")
    Page<TierPriceEntity> findAllActive(Pageable pageable);

    /**
     * Find highest price tier for SKU
     */
    @Query("select tp from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true order by tp.unitPrice desc")
    Optional<TierPriceEntity> findHighestPriceTierBySku(@Param("skuId") UUID skuId);

    /**
     * Find lowest price tier for SKU
     */
    @Query("select tp from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true order by tp.unitPrice asc")
    Optional<TierPriceEntity> findLowestPriceTierBySku(@Param("skuId") UUID skuId);

    /**
     * Find largest tier by quantity for SKU
     */
    @Query("select tp from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true order by tp.minQuantity desc")
    Optional<TierPriceEntity> findLargestTierBySku(@Param("skuId") UUID skuId);

    /**
     * Find tier for specific min quantity
     */
    @Query("select tp from TierPriceEntity tp where tp.skuId = :skuId and tp.minQuantity = :minQuantity and tp.active = true")
    Optional<TierPriceEntity> findBySkuAndMinQuantity(@Param("skuId") UUID skuId, @Param("minQuantity") Integer minQuantity);

    /**
     * Check if tier price exists for SKU
     */
    @Query("select case when count(tp) > 0 then true else false end from TierPriceEntity tp where tp.skuId = :skuId")
    boolean existsBySkuId(@Param("skuId") UUID skuId);

    /**
     * Check if active tier price exists for SKU
     */
    @Query("select case when count(tp) > 0 then true else false end from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true")
    boolean existsActiveBySkuId(@Param("skuId") UUID skuId);

    /**
     * Check if tier price exists at specific quantity
     */
    @Query("select case when count(tp) > 0 then true else false end from TierPriceEntity tp where tp.skuId = :skuId and tp.minQuantity = :minQuantity")
    boolean existsAtQuantity(@Param("skuId") UUID skuId, @Param("minQuantity") Integer minQuantity);

    /**
     * Count tier prices for a SKU
     */
    long countBySkuId(UUID skuId);

    /**
     * Count active tier prices for a SKU
     */
    @Query("select count(tp) from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true")
    long countActiveBySkuId(@Param("skuId") UUID skuId);

    /**
     * Get average unit price for a SKU
     */
    @Query("select avg(tp.unitPrice) from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true")
    BigDecimal getAverageUnitPriceBySku(@Param("skuId") UUID skuId);

    /**
     * Get maximum unit price for a SKU
     */
    @Query("select max(tp.unitPrice) from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true")
    BigDecimal getMaxUnitPriceBySku(@Param("skuId") UUID skuId);

    /**
     * Get minimum unit price for a SKU
     */
    @Query("select min(tp.unitPrice) from TierPriceEntity tp where tp.skuId = :skuId and tp.active = true")
    BigDecimal getMinUnitPriceBySku(@Param("skuId") UUID skuId);

    /**
     * Check for overlapping tiers in a SKU (gap or overlap in quantity ranges)
     */
    @Query("select case when count(tp1) > 0 then true else false end from TierPriceEntity tp1, TierPriceEntity tp2 where tp1.skuId = :skuId and tp2.skuId = :skuId and tp1.id != tp2.id and tp1.minQuantity <= tp2.minQuantity and (tp1.maxQuantity is null or tp1.maxQuantity >= tp2.minQuantity)")
    boolean hasOverlappingTiers(@Param("skuId") UUID skuId);
}

