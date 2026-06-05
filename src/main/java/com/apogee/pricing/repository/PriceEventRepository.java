package com.apogee.pricing.repository;

import com.apogee.pricing.entity.PriceEventEntity;
import com.apogee.pricing.entity.enums.PriceEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PriceEventRepository extends JpaRepository<PriceEventEntity, UUID> {

    /**
     * Find events by SKU ID
     */
    @Query("select pe from PriceEventEntity pe where pe.skuId = :skuId order by pe.startDate desc")
    List<PriceEventEntity> findBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find events by SKU ID with pagination
     */
    @Query("select pe from PriceEventEntity pe where pe.skuId = :skuId order by pe.startDate desc")
    Page<PriceEventEntity> findBySkuId(@Param("skuId") UUID skuId, Pageable pageable);

    /**
     * Find active events for a SKU
     */
    @Query("select pe from PriceEventEntity pe where pe.skuId = :skuId and pe.active = true order by pe.startDate desc")
    List<PriceEventEntity> findActiveBySkuId(@Param("skuId") UUID skuId);

    /**
     * Find active events for a SKU with pagination
     */
    @Query("select pe from PriceEventEntity pe where pe.skuId = :skuId and pe.active = true order by pe.startDate desc")
    Page<PriceEventEntity> findActiveBySkuId(@Param("skuId") UUID skuId, Pageable pageable);

    /**
     * Find events by event type
     */
    @Query("select pe from PriceEventEntity pe where pe.eventType = :eventType order by pe.startDate desc")
    List<PriceEventEntity> findByEventType(@Param("eventType") PriceEventType eventType);

    /**
     * Find events by event type with pagination
     */
    @Query("select pe from PriceEventEntity pe where pe.eventType = :eventType order by pe.startDate desc")
    Page<PriceEventEntity> findByEventType(@Param("eventType") PriceEventType eventType, Pageable pageable);

    /**
     * Find active events by event type
     */
    @Query("select pe from PriceEventEntity pe where pe.eventType = :eventType and pe.active = true order by pe.startDate desc")
    List<PriceEventEntity> findActiveByEventType(@Param("eventType") PriceEventType eventType);

    /**
     * Find events within a date range
     */
    @Query("select pe from PriceEventEntity pe where pe.startDate <= :endDate and pe.endDate >= :startDate order by pe.startDate asc")
    List<PriceEventEntity> findByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Find events within a date range with pagination
     */
    @Query("select pe from PriceEventEntity pe where pe.startDate <= :endDate and pe.endDate >= :startDate order by pe.startDate asc")
    Page<PriceEventEntity> findByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, Pageable pageable);

    /**
     * Find active events within a date range
     */
    @Query("select pe from PriceEventEntity pe where pe.active = true and pe.startDate <= :endDate and pe.endDate >= :startDate order by pe.startDate asc")
    List<PriceEventEntity> findActiveByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Find ongoing events (current date is within event duration)
     */
    @Query("select pe from PriceEventEntity pe where pe.active = true and pe.startDate <= current_timestamp and pe.endDate >= current_timestamp order by pe.priority desc")
    List<PriceEventEntity> findOngoing();

    /**
     * Find upcoming events
     */
    @Query("select pe from PriceEventEntity pe where pe.active = true and pe.startDate > current_timestamp order by pe.startDate asc")
    List<PriceEventEntity> findUpcoming();

    /**
     * Find upcoming events with pagination
     */
    @Query("select pe from PriceEventEntity pe where pe.active = true and pe.startDate > current_timestamp order by pe.startDate asc")
    Page<PriceEventEntity> findUpcoming(Pageable pageable);

    /**
     * Find past events
     */
    @Query("select pe from PriceEventEntity pe where pe.endDate < current_timestamp order by pe.endDate desc")
    List<PriceEventEntity> findPast();

    /**
     * Find events by name containing
     */
    @Query("select pe from PriceEventEntity pe where upper(pe.eventName) like upper(concat('%', :name, '%')) order by pe.startDate desc")
    List<PriceEventEntity> findByEventNameContaining(@Param("name") String name);

    /**
     * Find active events by name
     */
    @Query("select pe from PriceEventEntity pe where upper(pe.eventName) like upper(concat('%', :name, '%')) and pe.active = true order by pe.startDate desc")
    List<PriceEventEntity> findActiveByEventNameContaining(@Param("name") String name);

    /**
     * Find highest priority events
     */
    @Query("select pe from PriceEventEntity pe where pe.active = true order by pe.priority desc, pe.startDate desc")
    List<PriceEventEntity> findByHighestPriority();

    /**
     * Find events for SKU within date range
     */
    @Query("select pe from PriceEventEntity pe where pe.skuId = :skuId and pe.startDate <= :endDate and pe.endDate >= :startDate order by pe.startDate asc")
    List<PriceEventEntity> findBySkuIdAndDateRange(@Param("skuId") UUID skuId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Find events with specific future price
     */
    @Query("select pe from PriceEventEntity pe where pe.futurePrice = :price order by pe.startDate asc")
    List<PriceEventEntity> findByFuturePrice(@Param("price") BigDecimal price);

    /**
     * Find events with future price in range
     */
    @Query("select pe from PriceEventEntity pe where pe.futurePrice between :minPrice and :maxPrice order by pe.futurePrice asc")
    List<PriceEventEntity> findByFuturePriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Count active events
     */
    @Query("select count(pe) from PriceEventEntity pe where pe.active = true")
    long countActive();

    /**
     * Count events for SKU
     */
    long countBySkuId(UUID skuId);

    /**
     * Count events by event type
     */
    long countByEventType(PriceEventType eventType);

    /**
     * Get average future price
     */
    @Query("select avg(pe.futurePrice) from PriceEventEntity pe where pe.active = true")
    BigDecimal getAverageFuturePrice();
}

