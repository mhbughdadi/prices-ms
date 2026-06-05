package com.apogee.pricing.repository;

import com.apogee.pricing.entity.OutboxEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    /**
     * Find all unprocessed events
     */
    @Query("select oe from OutboxEventEntity oe where oe.processed = false order by oe.createdAt asc")
    List<OutboxEventEntity> findUnprocessed();

    /**
     * Find all unprocessed events with pagination
     */
    @Query("select oe from OutboxEventEntity oe where oe.processed = false order by oe.createdAt asc")
    Page<OutboxEventEntity> findUnprocessed(Pageable pageable);

    /**
     * Find unprocessed events by aggregate type
     */
    @Query("select oe from OutboxEventEntity oe where oe.processed = false and oe.aggregateType = :aggregateType order by oe.createdAt asc")
    List<OutboxEventEntity> findUnprocessedByAggregateType(@Param("aggregateType") String aggregateType);

    /**
     * Find unprocessed events by event type
     */
    @Query("select oe from OutboxEventEntity oe where oe.processed = false and oe.eventType = :eventType order by oe.createdAt asc")
    List<OutboxEventEntity> findUnprocessedByEventType(@Param("eventType") String eventType);

    /**
     * Find events by aggregate ID
     */
    List<OutboxEventEntity> findByAggregateId(UUID aggregateId);

    /**
     * Find events by aggregate ID with pagination
     */
    Page<OutboxEventEntity> findByAggregateId(UUID aggregateId, Pageable pageable);

    /**
     * Find events by aggregate type
     */
    @Query("select oe from OutboxEventEntity oe where oe.aggregateType = :aggregateType order by oe.createdAt desc")
    List<OutboxEventEntity> findByAggregateType(@Param("aggregateType") String aggregateType);

    /**
     * Find events by aggregate type with pagination
     */
    @Query("select oe from OutboxEventEntity oe where oe.aggregateType = :aggregateType order by oe.createdAt desc")
    Page<OutboxEventEntity> findByAggregateType(@Param("aggregateType") String aggregateType, Pageable pageable);

    /**
     * Find unprocessed events for a specific aggregate
     */
    @Query("select oe from OutboxEventEntity oe where oe.aggregateId = :aggregateId and oe.processed = false order by oe.createdAt asc")
    List<OutboxEventEntity> findUnprocessedByAggregateId(@Param("aggregateId") UUID aggregateId);

    /**
     * Find events by event type
     */
    @Query("select oe from OutboxEventEntity oe where oe.eventType = :eventType order by oe.createdAt desc")
    List<OutboxEventEntity> findByEventType(@Param("eventType") String eventType);

    /**
     * Find events by event type with pagination
     */
    @Query("select oe from OutboxEventEntity oe where oe.eventType = :eventType order by oe.createdAt desc")
    Page<OutboxEventEntity> findByEventType(@Param("eventType") String eventType, Pageable pageable);

    /**
     * Find events created after a specific date
     */
    @Query("select oe from OutboxEventEntity oe where oe.createdAt >= :createdAfter order by oe.createdAt desc")
    List<OutboxEventEntity> findCreatedAfter(@Param("createdAfter") OffsetDateTime createdAfter);

    /**
     * Find events created after a specific date with pagination
     */
    @Query("select oe from OutboxEventEntity oe where oe.createdAt >= :createdAfter order by oe.createdAt desc")
    Page<OutboxEventEntity> findCreatedAfter(@Param("createdAfter") OffsetDateTime createdAfter, Pageable pageable);

    /**
     * Mark events as processed
     */
    @Modifying
    @Transactional
    @Query("update OutboxEventEntity oe set oe.processed = true where oe.id = :id")
    void markAsProcessed(@Param("id") UUID id);

    /**
     * Mark multiple events as processed
     */
    @Modifying
    @Transactional
    @Query("update OutboxEventEntity oe set oe.processed = true where oe.id in :ids")
    void markAsProcessed(@Param("ids") List<UUID> ids);

    /**
     * Mark all unprocessed events as processed
     */
    @Modifying
    @Transactional
    @Query("update OutboxEventEntity oe set oe.processed = true where oe.processed = false")
    void markAllAsProcessed();

    /**
     * Count unprocessed events
     */
    @Query("select count(oe) from OutboxEventEntity oe where oe.processed = false")
    long countUnprocessed();

    /**
     * Count events by aggregate type
     */
    @Query("select count(oe) from OutboxEventEntity oe where oe.aggregateType = :aggregateType")
    long countByAggregateType(@Param("aggregateType") String aggregateType);

    /**
     * Delete processed events older than a cutoff date
     */
    @Modifying
    @Transactional
    @Query("delete from OutboxEventEntity oe where oe.processed = true and oe.createdAt < :cutoffDate")
    void deleteProcessedBefore(@Param("cutoffDate") OffsetDateTime cutoffDate);
}

