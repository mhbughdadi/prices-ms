package com.apogee.pricing.repository;

import com.apogee.pricing.entity.CustomerSegmentEntity;
import com.apogee.pricing.entity.SegmentPriceEntity;
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
public interface SegmentPriceRepository extends JpaRepository<SegmentPriceEntity, UUID> {

    /**
     * Find price for SKU and customer segment
     */
    @Query("select sp from SegmentPriceEntity sp where sp.skuId = :skuId and sp.segment.id = :segmentId and sp.active = true")
    Optional<SegmentPriceEntity> findBySkuAndSegment(@Param("skuId") UUID skuId, @Param("segmentId") UUID segmentId);

    /**
     * Find all prices for a SKU across all segments
     */
    @Query("select sp from SegmentPriceEntity sp where sp.skuId = :skuId order by sp.segment.code asc")
    List<SegmentPriceEntity> findBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find all active prices for a SKU
     */
    @Query("select sp from SegmentPriceEntity sp where sp.skuId = :skuId and sp.active = true order by sp.segment.code asc")
    List<SegmentPriceEntity> findActiveBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find all active prices for a SKU with pagination
     */
    @Query("select sp from SegmentPriceEntity sp where sp.skuId = :skuId and sp.active = true order by sp.segment.code asc")
    Page<SegmentPriceEntity> findActiveBySkuId(@Param("skuId") UUID skuId, Pageable pageable);

    /**
     * Find all prices for a segment
     */
    List<SegmentPriceEntity> findBySegment(CustomerSegmentEntity segment);

    /**
     * Find all prices for a segment with pagination
     */
    Page<SegmentPriceEntity> findBySegment(CustomerSegmentEntity segment, Pageable pageable);

    /**
     * Find all prices for a segment by segment ID
     */
    @Query("select sp from SegmentPriceEntity sp where sp.segment.id = :segmentId order by sp.skuId asc")
    List<SegmentPriceEntity> findBySegmentId(@Param("segmentId") UUID segmentId);

    /**
     * Find all active prices for a segment
     */
    @Query("select sp from SegmentPriceEntity sp where sp.segment.id = :segmentId and sp.active = true order by sp.skuId asc")
    List<SegmentPriceEntity> findActiveBySegmentId(@Param("segmentId") UUID segmentId);

    /**
     * Find all active prices for a segment with pagination
     */
    @Query("select sp from SegmentPriceEntity sp where sp.segment.id = :segmentId and sp.active = true order by sp.skuId asc")
    Page<SegmentPriceEntity> findActiveBySegmentId(@Param("segmentId") UUID segmentId, Pageable pageable);

    /**
     * Find prices in a price range
     */
    @Query("select sp from SegmentPriceEntity sp where sp.price between :minPrice and :maxPrice order by sp.price asc")
    List<SegmentPriceEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find prices in a price range with pagination
     */
    @Query("select sp from SegmentPriceEntity sp where sp.price between :minPrice and :maxPrice order by sp.price asc")
    Page<SegmentPriceEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    /**
     * Find prices greater than a specific amount
     */
    @Query("select sp from SegmentPriceEntity sp where sp.price > :price order by sp.price desc")
    List<SegmentPriceEntity> findByPriceGreaterThan(@Param("price") BigDecimal price);

    /**
     * Find prices less than a specific amount
     */
    @Query("select sp from SegmentPriceEntity sp where sp.price < :price order by sp.price asc")
    List<SegmentPriceEntity> findByPriceLessThan(@Param("price") BigDecimal price);

    /**
     * Find prices active on a specific date
     */
    @Query("select sp from SegmentPriceEntity sp where sp.active = true and (sp.startDate is null or sp.startDate <= :date) and (sp.endDate is null or sp.endDate >= :date)")
    List<SegmentPriceEntity> findActiveOnDate(@Param("date") OffsetDateTime date);

    /**
     * Find prices within a date range
     */
    @Query("select sp from SegmentPriceEntity sp where (sp.startDate is null or sp.startDate <= :endDate) and (sp.endDate is null or sp.endDate >= :startDate) order by sp.startDate asc")
    List<SegmentPriceEntity> findByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Find upcoming prices (start date in future)
     */
    @Query("select sp from SegmentPriceEntity sp where sp.startDate > current_timestamp order by sp.startDate asc")
    List<SegmentPriceEntity> findUpcoming();

    /**
     * Find expired prices (end date in past)
     */
    @Query("select sp from SegmentPriceEntity sp where sp.endDate < current_timestamp order by sp.endDate desc")
    List<SegmentPriceEntity> findExpired();

    /**
     * Find prices created after a specific date
     */
    @Query("select sp from SegmentPriceEntity sp where sp.createdAt >= :createdAfter order by sp.createdAt desc")
    List<SegmentPriceEntity> findCreatedAfter(@Param("createdAfter") OffsetDateTime createdAfter);

    /**
     * Check if price exists for SKU and segment
     */
    @Query("select case when count(sp) > 0 then true else false end from SegmentPriceEntity sp where sp.skuId = :skuId and sp.segment.id = :segmentId")
    boolean existsForSkuAndSegment(@Param("skuId") UUID skuId, @Param("segmentId") UUID segmentId);

    /**
     * Count prices for a SKU
     */
    long countBySkuId(UUID skuId);

    /**
     * Count prices for a segment
     */
    long countBySegment(CustomerSegmentEntity segment);

    /**
     * Count active prices for a segment
     */
    @Query("select count(sp) from SegmentPriceEntity sp where sp.segment.id = :segmentId and sp.active = true")
    long countActiveBySegmentId(@Param("segmentId") UUID segmentId);

    /**
     * Get average price for a segment
     */
    @Query("select avg(sp.price) from SegmentPriceEntity sp where sp.segment.id = :segmentId and sp.active = true")
    BigDecimal getAveragePriceBySegment(@Param("segmentId") UUID segmentId);

    /**
     * Get maximum price for a segment
     */
    @Query("select max(sp.price) from SegmentPriceEntity sp where sp.segment.id = :segmentId and sp.active = true")
    BigDecimal getMaxPriceBySegment(@Param("segmentId") UUID segmentId);

    /**
     * Get minimum price for a segment
     */
    @Query("select min(sp.price) from SegmentPriceEntity sp where sp.segment.id = :segmentId and sp.active = true")
    BigDecimal getMinPriceBySegment(@Param("segmentId") UUID segmentId);

    /**
     * Find all segments with prices for a SKU
     */
    @Query("select sp.segment from SegmentPriceEntity sp where sp.skuId = :skuId and sp.active = true order by sp.segment.code asc")
    List<CustomerSegmentEntity> findSegmentsBySkuId(@Param("skuId") UUID skuId);
}

