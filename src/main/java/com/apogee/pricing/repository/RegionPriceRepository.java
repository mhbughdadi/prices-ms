package com.apogee.pricing.repository;

import com.apogee.pricing.entity.RegionPriceEntity;
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
public interface RegionPriceRepository extends JpaRepository<RegionPriceEntity, UUID> {

    /**
     * Find price for SKU in a specific country
     */
    @Query("select rp from RegionPriceEntity rp where rp.skuId = :skuId and rp.countryCode = :countryCode and rp.active = true")
    Optional<RegionPriceEntity> findBySkuAndCountry(@Param("skuId") UUID skuId, @Param("countryCode") String countryCode);

    /**
     * Find price for SKU in a specific country and region
     */
    @Query("select rp from RegionPriceEntity rp where rp.skuId = :skuId and rp.countryCode = :countryCode and rp.regionCode = :regionCode and rp.active = true")
    Optional<RegionPriceEntity> findBySkuCountryAndRegion(@Param("skuId") UUID skuId, @Param("countryCode") String countryCode, @Param("regionCode") String regionCode);

    /**
     * Find all prices for a SKU
     */
    @Query("select rp from RegionPriceEntity rp where rp.skuId = :skuId order by rp.countryCode asc, rp.regionCode asc")
    List<RegionPriceEntity> findBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find all active prices for a SKU
     */
    @Query("select rp from RegionPriceEntity rp where rp.skuId = :skuId and rp.active = true order by rp.countryCode asc, rp.regionCode asc")
    List<RegionPriceEntity> findActiveBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find all active prices for a SKU with pagination
     */
    @Query("select rp from RegionPriceEntity rp where rp.skuId = :skuId and rp.active = true order by rp.countryCode asc, rp.regionCode asc")
    Page<RegionPriceEntity> findActiveBySkuId(@Param("skuId") UUID skuId, Pageable pageable);

    /**
     * Find all prices for a country
     */
    @Query("select rp from RegionPriceEntity rp where rp.countryCode = :countryCode order by rp.skuId asc")
    List<RegionPriceEntity> findByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Find all active prices for a country
     */
    @Query("select rp from RegionPriceEntity rp where rp.countryCode = :countryCode and rp.active = true order by rp.skuId asc")
    List<RegionPriceEntity> findActiveByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Find all active prices for a country with pagination
     */
    @Query("select rp from RegionPriceEntity rp where rp.countryCode = :countryCode and rp.active = true order by rp.skuId asc")
    Page<RegionPriceEntity> findActiveByCountryCode(@Param("countryCode") String countryCode, Pageable pageable);

    /**
     * Find prices for a region
     */
    @Query("select rp from RegionPriceEntity rp where rp.regionCode = :regionCode order by rp.skuId asc")
    List<RegionPriceEntity> findByRegionCode(@Param("regionCode") String regionCode);

    /**
     * Find active prices for a region
     */
    @Query("select rp from RegionPriceEntity rp where rp.regionCode = :regionCode and rp.active = true order by rp.skuId asc")
    List<RegionPriceEntity> findActiveByRegionCode(@Param("regionCode") String regionCode);

    /**
     * Find prices in a price range
     */
    @Query("select rp from RegionPriceEntity rp where rp.price between :minPrice and :maxPrice order by rp.price asc")
    List<RegionPriceEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find prices in a price range with pagination
     */
    @Query("select rp from RegionPriceEntity rp where rp.price between :minPrice and :maxPrice order by rp.price asc")
    Page<RegionPriceEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    /**
     * Find prices greater than a specific amount
     */
    @Query("select rp from RegionPriceEntity rp where rp.price > :price order by rp.price desc")
    List<RegionPriceEntity> findByPriceGreaterThan(@Param("price") BigDecimal price);

    /**
     * Find prices less than a specific amount
     */
    @Query("select rp from RegionPriceEntity rp where rp.price < :price order by rp.price asc")
    List<RegionPriceEntity> findByPriceLessThan(@Param("price") BigDecimal price);

    /**
     * Find highest price for a country
     */
    @Query("select rp from RegionPriceEntity rp where rp.countryCode = :countryCode and rp.active = true order by rp.price desc")
    Optional<RegionPriceEntity> findHighestPriceByCountry(@Param("countryCode") String countryCode);

    /**
     * Find lowest price for a country
     */
    @Query("select rp from RegionPriceEntity rp where rp.countryCode = :countryCode and rp.active = true order by rp.price asc")
    Optional<RegionPriceEntity> findLowestPriceByCountry(@Param("countryCode") String countryCode);

    /**
     * Check if price exists for SKU and country
     */
    @Query("select case when count(rp) > 0 then true else false end from RegionPriceEntity rp where rp.skuId = :skuId and rp.countryCode = :countryCode")
    boolean existsForSkuAndCountry(@Param("skuId") UUID skuId, @Param("countryCode") String countryCode);

    /**
     * Count prices for a SKU
     */
    long countBySkuId(UUID skuId);

    /**
     * Count prices for a country
     */
    long countByCountryCode(String countryCode);

    /**
     * Count active prices for a country
     */
    @Query("select count(rp) from RegionPriceEntity rp where rp.countryCode = :countryCode and rp.active = true")
    long countActiveByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Get average price for a country
     */
    @Query("select avg(rp.price) from RegionPriceEntity rp where rp.countryCode = :countryCode and rp.active = true")
    BigDecimal getAveragePriceByCountry(@Param("countryCode") String countryCode);

    /**
     * Get maximum price for a country
     */
    @Query("select max(rp.price) from RegionPriceEntity rp where rp.countryCode = :countryCode and rp.active = true")
    BigDecimal getMaxPriceByCountry(@Param("countryCode") String countryCode);

    /**
     * Get minimum price for a country
     */
    @Query("select min(rp.price) from RegionPriceEntity rp where rp.countryCode = :countryCode and rp.active = true")
    BigDecimal getMinPriceByCountry(@Param("countryCode") String countryCode);

    /**
     * Find all countries with prices for a SKU
     */
    @Query("select distinct rp.countryCode from RegionPriceEntity rp where rp.skuId = :skuId and rp.active = true")
    List<String> findCountriesBySkuId(@Param("skuId") UUID skuId);
}

