package com.apogee.pricing.repository;

import com.apogee.pricing.entity.BundleItemEntity;
import com.apogee.pricing.entity.PriceBundleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BundleItemRepository extends JpaRepository<BundleItemEntity, UUID> {

    /**
     * Find all items in a bundle
     */
    List<BundleItemEntity> findByBundle(PriceBundleEntity bundle);

    /**
     * Find all items for a bundle with pagination
     */
    Page<BundleItemEntity> findByBundle(PriceBundleEntity bundle, Pageable pageable);

    /**
     * Find all items in a bundle by bundle ID
     */
    @Query("select bi from BundleItemEntity bi where bi.bundle.id = :bundleId")
    List<BundleItemEntity> findByBundleId(@Param("bundleId") UUID bundleId);

    /**
     * Find all items in a bundle by bundle ID with pagination
     */
    @Query("select bi from BundleItemEntity bi where bi.bundle.id = :bundleId")
    Page<BundleItemEntity> findByBundleId(@Param("bundleId") UUID bundleId, Pageable pageable);

    /**
     * Count items in a bundle
     */
    long countByBundle(PriceBundleEntity bundle);

    /**
     * Count items in a bundle by bundle ID
     */
    @Query("select count(bi) from BundleItemEntity bi where bi.bundle.id = :bundleId")
    long countByBundleId(@Param("bundleId") UUID bundleId);

    /**
     * Find items by SKU ID
     */
    List<BundleItemEntity> findBySkuId(UUID skuId);

    /**
     * Find items by SKU ID with pagination
     */
    Page<BundleItemEntity> findBySkuId(UUID skuId, Pageable pageable);

    /**
     * Find items in a bundle by SKU ID
     */
    @Query("select bi from BundleItemEntity bi where bi.bundle.id = :bundleId and bi.skuId = :skuId")
    List<BundleItemEntity> findByBundleIdAndSkuId(@Param("bundleId") UUID bundleId, @Param("skuId") UUID skuId);

    /**
     * Check if a SKU exists in a bundle
     */
    @Query("select case when count(bi) > 0 then true else false end from BundleItemEntity bi where bi.bundle.id = :bundleId and bi.skuId = :skuId")
    boolean existsInBundle(@Param("bundleId") UUID bundleId, @Param("skuId") UUID skuId);

    /**
     * Delete all items in a bundle
     */
    void deleteByBundle(PriceBundleEntity bundle);

    /**
     * Delete all items in a bundle by bundle ID
     */
    @Query("delete from BundleItemEntity bi where bi.bundle.id = :bundleId")
    void deleteByBundleId(@Param("bundleId") UUID bundleId);
}

