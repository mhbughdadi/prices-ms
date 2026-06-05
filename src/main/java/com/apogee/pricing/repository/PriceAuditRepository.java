package com.apogee.pricing.repository;

import com.apogee.pricing.entity.PriceAuditEntity;
import com.apogee.pricing.entity.PriceAuditId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PriceAuditRepository extends JpaRepository<PriceAuditEntity, PriceAuditId> {

    /**
     * Find all audit records for a specific SKU
     */
    @Query("select pa from PriceAuditEntity pa where pa.skuId = :skuId order by pa.changedAt desc")
    List<PriceAuditEntity> findBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find all audit records for a specific SKU with pagination
     */
    @Query("select pa from PriceAuditEntity pa where pa.skuId = :skuId order by pa.changedAt desc")
    Page<PriceAuditEntity> findBySkuId(@Param("skuId") UUID skuId, Pageable pageable);

    /**
     * Find audit records for a SKU within a date range
     */
    @Query("select pa from PriceAuditEntity pa where pa.skuId = :skuId and pa.changedAt between :startDate and :endDate order by pa.changedAt desc")
    List<PriceAuditEntity> findBySkuIdAndDateRange(@Param("skuId") UUID skuId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Find audit records for a SKU within a date range with pagination
     */
    @Query("select pa from PriceAuditEntity pa where pa.skuId = :skuId and pa.changedAt between :startDate and :endDate order by pa.changedAt desc")
    Page<PriceAuditEntity> findBySkuIdAndDateRange(@Param("skuId") UUID skuId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, Pageable pageable);

    /**
     * Find audit records changed after a specific date
     */
    @Query("select pa from PriceAuditEntity pa where pa.changedAt >= :changedAfter order by pa.changedAt desc")
    List<PriceAuditEntity> findChangedAfter(@Param("changedAfter") OffsetDateTime changedAfter);

    /**
     * Find audit records changed after a specific date with pagination
     */
    @Query("select pa from PriceAuditEntity pa where pa.changedAt >= :changedAfter order by pa.changedAt desc")
    Page<PriceAuditEntity> findChangedAfter(@Param("changedAfter") OffsetDateTime changedAfter, Pageable pageable);

    /**
     * Find audit records by user who made the change
     */
    @Query("select pa from PriceAuditEntity pa where pa.changedBy = :changedBy order by pa.changedAt desc")
    List<PriceAuditEntity> findByChangedBy(@Param("changedBy") String changedBy);

    /**
     * Find audit records by user who made the change with pagination
     */
    @Query("select pa from PriceAuditEntity pa where pa.changedBy = :changedBy order by pa.changedAt desc")
    Page<PriceAuditEntity> findByChangedBy(@Param("changedBy") String changedBy, Pageable pageable);

    /**
     * Find price increases for a SKU
     */
    @Query("select pa from PriceAuditEntity pa where pa.skuId = :skuId and pa.newPrice > pa.oldPrice order by pa.changedAt desc")
    List<PriceAuditEntity> findPriceIncreases(@Param("skuId") UUID skuId);

    /**
     * Find price decreases for a SKU
     */
    @Query("select pa from PriceAuditEntity pa where pa.skuId = :skuId and pa.newPrice < pa.oldPrice order by pa.changedAt desc")
    List<PriceAuditEntity> findPriceDecreases(@Param("skuId") UUID skuId);

    /**
     * Find latest price change for a SKU
     */
    @Query("select pa from PriceAuditEntity pa where pa.skuId = :skuId order by pa.changedAt desc")
    PriceAuditEntity findLatestBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find audit records with specific change reason
     */
    @Query("select pa from PriceAuditEntity pa where pa.changeReason like concat('%', :reason, '%') order by pa.changedAt desc")
    List<PriceAuditEntity> findByChangeReasonContaining(@Param("reason") String reason);

    /**
     * Find audit records with specific change reason with pagination
     */
    @Query("select pa from PriceAuditEntity pa where pa.changeReason like concat('%', :reason, '%') order by pa.changedAt desc")
    Page<PriceAuditEntity> findByChangeReasonContaining(@Param("reason") String reason, Pageable pageable);

    /**
     * Count price changes for a SKU
     */
    @Query("select count(pa) from PriceAuditEntity pa where pa.skuId = :skuId")
    long countBySkuId(@Param("skuId") UUID skuId);

    /**
     * Count price increases for a SKU
     */
    @Query("select count(pa) from PriceAuditEntity pa where pa.skuId = :skuId and pa.newPrice > pa.oldPrice")
    long countPriceIncreases(@Param("skuId") UUID skuId);

    /**
     * Count price decreases for a SKU
     */
    @Query("select count(pa) from PriceAuditEntity pa where pa.skuId = :skuId and pa.newPrice < pa.oldPrice")
    long countPriceDecreases(@Param("skuId") UUID skuId);

    /**
     * Get highest price set for a SKU
     */
    @Query("select max(pa.newPrice) from PriceAuditEntity pa where pa.skuId = :skuId")
    java.math.BigDecimal getMaxPriceForSku(@Param("skuId") UUID skuId);

    /**
     * Get lowest price set for a SKU
     */
    @Query("select min(pa.newPrice) from PriceAuditEntity pa where pa.skuId = :skuId")
    java.math.BigDecimal getMinPriceForSku(@Param("skuId") UUID skuId);
}

