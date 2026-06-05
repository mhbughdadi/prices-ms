package com.apogee.pricing.repository;

import com.apogee.pricing.entity.PriceBundleEntity;
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
public interface PriceBundleRepository extends JpaRepository<PriceBundleEntity, UUID> {

    /**
     * Find bundle by code
     */
    Optional<PriceBundleEntity> findByBundleCode(String bundleCode);

    /**
     * Find bundle by code case-insensitive
     */
    @Query("select pb from PriceBundleEntity pb where upper(pb.bundleCode) = upper(:bundleCode)")
    Optional<PriceBundleEntity> findByBundleCodeIgnoreCase(@Param("bundleCode") String bundleCode);

    /**
     * Find all active bundles
     */
    @Query("select pb from PriceBundleEntity pb where pb.active = true order by pb.bundleName asc")
    List<PriceBundleEntity> findAllActive();

    /**
     * Find all active bundles with pagination
     */
    @Query("select pb from PriceBundleEntity pb where pb.active = true order by pb.bundleName asc")
    Page<PriceBundleEntity> findAllActive(Pageable pageable);

    /**
     * Find bundles by name containing
     */
    @Query("select pb from PriceBundleEntity pb where upper(pb.bundleName) like upper(concat('%', :name, '%')) order by pb.bundleName asc")
    List<PriceBundleEntity> findByBundleNameContaining(@Param("name") String name);

    /**
     * Find bundles by name containing with pagination
     */
    @Query("select pb from PriceBundleEntity pb where upper(pb.bundleName) like upper(concat('%', :name, '%')) order by pb.bundleName asc")
    Page<PriceBundleEntity> findByBundleNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Find active bundles by name
     */
    @Query("select pb from PriceBundleEntity pb where upper(pb.bundleName) like upper(concat('%', :name, '%')) and pb.active = true order by pb.bundleName asc")
    List<PriceBundleEntity> findActiveByBundleNameContaining(@Param("name") String name);

    /**
     * Find bundles by code containing
     */
    @Query("select pb from PriceBundleEntity pb where upper(pb.bundleCode) like upper(concat('%', :code, '%')) order by pb.bundleCode asc")
    List<PriceBundleEntity> findByBundleCodeContaining(@Param("code") String code);

    /**
     * Find bundles with price in range
     */
    @Query("select pb from PriceBundleEntity pb where pb.bundlePrice between :minPrice and :maxPrice order by pb.bundlePrice asc")
    List<PriceBundleEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find bundles with price in range with pagination
     */
    @Query("select pb from PriceBundleEntity pb where pb.bundlePrice between :minPrice and :maxPrice order by pb.bundlePrice asc")
    Page<PriceBundleEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    /**
     * Find bundles with price greater than
     */
    @Query("select pb from PriceBundleEntity pb where pb.bundlePrice > :price order by pb.bundlePrice asc")
    List<PriceBundleEntity> findByPriceGreaterThan(@Param("price") BigDecimal price);

    /**
     * Find bundles with price less than
     */
    @Query("select pb from PriceBundleEntity pb where pb.bundlePrice < :price order by pb.bundlePrice asc")
    List<PriceBundleEntity> findByPriceLessThan(@Param("price") BigDecimal price);

    /**
     * Find bundle with items eagerly loaded
     */
    @Query("select pb from PriceBundleEntity pb left join fetch pb.items where pb.id = :id")
    Optional<PriceBundleEntity> findByIdWithItems(@Param("id") UUID id);

    /**
     * Check if bundle code exists
     */
    boolean existsByBundleCode(String bundleCode);

    /**
     * Check if active bundle exists by code
     */
    @Query("select case when count(pb) > 0 then true else false end from PriceBundleEntity pb where pb.bundleCode = :bundleCode and pb.active = true")
    boolean existsActiveByBundleCode(@Param("bundleCode") String bundleCode);

    /**
     * Count active bundles
     */
    long countByActive(boolean active);

    /**
     * Get total bundle price (sum of all bundle prices)
     */
    @Query("select sum(pb.bundlePrice) from PriceBundleEntity pb where pb.active = true")
    BigDecimal getTotalBundlePrice();

    /**
     * Get average bundle price
     */
    @Query("select avg(pb.bundlePrice) from PriceBundleEntity pb where pb.active = true")
    BigDecimal getAverageBundlePrice();

    /**
     * Get maximum bundle price
     */
    @Query("select max(pb.bundlePrice) from PriceBundleEntity pb where pb.active = true")
    BigDecimal getMaxBundlePrice();

    /**
     * Get minimum bundle price
     */
    @Query("select min(pb.bundlePrice) from PriceBundleEntity pb where pb.active = true")
    BigDecimal getMinBundlePrice();

    /**
     * Count items in a bundle
     */
    @Query("select count(bi) from BundleItemEntity bi where bi.bundle.id = :bundleId")
    long countBundleItems(@Param("bundleId") UUID bundleId);
}

