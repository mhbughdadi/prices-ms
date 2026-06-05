package com.apogee.pricing.repository;

import com.apogee.pricing.entity.CurrencyEntity;
import com.apogee.pricing.entity.PriceListEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceListRepository extends JpaRepository<PriceListEntity, UUID> {

    /**
     * Find price list by code
     */
    Optional<PriceListEntity> findByCode(String code);

    /**
     * Find price list by code case-insensitive
     */
    @Query("select p from PriceListEntity p where upper(p.code) = upper(:code) and p.deleted = false")
    Optional<PriceListEntity> findByCodeIgnoreCase(@Param("code") String code);

    /**
     * Find price list with SKU prices eagerly loaded
     */
    @EntityGraph(attributePaths = {"skuPrices"})
    @Query("select p from PriceListEntity p where p.id = :id and p.deleted = false")
    Optional<PriceListEntity> findByIdAndNotDeletedWithSkuPrices(@Param("id") UUID id);

    /**
     * Find price list with SKU prices using fetch join
     */
    @Query("select p from PriceListEntity p left join fetch p.skuPrices sp where p.id = :id and p.deleted = false")
    Optional<PriceListEntity> findByIdWithSkuPricesFetch(@Param("id") UUID id);

    /**
     * Find all active price lists
     */
    @Query("select p from PriceListEntity p where p.active = true and p.deleted = false order by p.priority asc, p.name asc")
    List<PriceListEntity> findAllActive();

    /**
     * Find all active price lists with pagination
     */
    @Query("select p from PriceListEntity p where p.active = true and p.deleted = false order by p.priority asc, p.name asc")
    Page<PriceListEntity> findAllActive(Pageable pageable);

    /**
     * Find price lists by name containing
     */
    @Query("select p from PriceListEntity p where upper(p.name) like upper(concat('%', :name, '%')) and p.deleted = false order by p.priority asc")
    List<PriceListEntity> findByNameContaining(@Param("name") String name);

    /**
     * Find price lists by name containing with pagination
     */
    @Query("select p from PriceListEntity p where upper(p.name) like upper(concat('%', :name, '%')) and p.deleted = false order by p.priority asc")
    Page<PriceListEntity> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Find active price lists by name
     */
    @Query("select p from PriceListEntity p where upper(p.name) like upper(concat('%', :name, '%')) and p.active = true and p.deleted = false order by p.priority asc")
    List<PriceListEntity> findActiveByNameContaining(@Param("name") String name);

    /**
     * Find price lists by currency
     */
    List<PriceListEntity> findByCurrency(CurrencyEntity currency);

    /**
     * Find active price lists by currency
     */
    @Query("select p from PriceListEntity p where p.currency.id = :currencyId and p.active = true and p.deleted = false order by p.priority asc")
    List<PriceListEntity> findActiveByCurrencyId(@Param("currencyId") UUID currencyId);

    /**
     * Find active price lists by currency with pagination
     */
    @Query("select p from PriceListEntity p where p.currency.id = :currencyId and p.active = true and p.deleted = false order by p.priority asc")
    Page<PriceListEntity> findActiveByCurrencyId(@Param("currencyId") UUID currencyId, Pageable pageable);

    /**
     * Find price lists by priority
     */
    @Query("select p from PriceListEntity p where p.priority = :priority and p.deleted = false order by p.name asc")
    List<PriceListEntity> findByPriority(@Param("priority") Integer priority);

    /**
     * Find price lists by priority range
     */
    @Query("select p from PriceListEntity p where p.priority between :minPriority and :maxPriority and p.deleted = false order by p.priority asc")
    List<PriceListEntity> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);

    /**
     * Find price lists valid on a specific date
     */
    @Query("select p from PriceListEntity p where p.active = true and p.deleted = false and (p.startDate is null or p.startDate <= :date) and (p.endDate is null or p.endDate >= :date)")
    List<PriceListEntity> findValidOnDate(@Param("date") OffsetDateTime date);

    /**
     * Find price lists within a date range
     */
    @Query("select p from PriceListEntity p where p.deleted = false and (p.startDate is null or p.startDate <= :endDate) and (p.endDate is null or p.endDate >= :startDate) order by p.priority asc")
    List<PriceListEntity> findByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Find upcoming price lists
     */
    @Query("select p from PriceListEntity p where p.startDate > current_timestamp and p.deleted = false order by p.startDate asc")
    List<PriceListEntity> findUpcoming();

    /**
     * Find ongoing price lists
     */
    @Query("select p from PriceListEntity p where p.active = true and p.deleted = false and (p.startDate is null or p.startDate <= current_timestamp) and (p.endDate is null or p.endDate >= current_timestamp) order by p.priority asc")
    List<PriceListEntity> findOngoing();

    /**
     * Find expired price lists
     */
    @Query("select p from PriceListEntity p where p.endDate < current_timestamp and p.deleted = false order by p.endDate desc")
    List<PriceListEntity> findExpired();

    /**
     * Find price lists modified after a specific date
     */
    @Query("select p from PriceListEntity p where p.updatedAt >= :updatedAfter and p.deleted = false order by p.updatedAt desc")
    List<PriceListEntity> findModifiedAfter(@Param("updatedAfter") OffsetDateTime updatedAfter);

    /**
     * Find price lists created by a specific user
     */
    @Query("select p from PriceListEntity p where p.createdBy = :createdBy and p.deleted = false order by p.createdAt desc")
    List<PriceListEntity> findCreatedBy(@Param("createdBy") String createdBy);

    /**
     * Check if price list code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if active price list exists by code
     */
    @Query("select case when count(p) > 0 then true else false end from PriceListEntity p where p.code = :code and p.active = true and p.deleted = false")
    boolean existsActiveByCode(@Param("code") String code);

    /**
     * Count active price lists
     */
    @Query("select count(p) from PriceListEntity p where p.active = true and p.deleted = false")
    long countActive();

    /**
     * Count price lists for a currency
     */
    long countByCurrency(CurrencyEntity currency);

    /**
     * Count SKU prices in a price list
     */
    @Query("select count(sp) from SkuPriceEntity sp where sp.priceList.id = :priceListId and sp.deleted = false")
    long countSkuPrices(@Param("priceListId") UUID priceListId);

    /**
     * Get detailed price list with SKU prices and currency data
     */
    @Query("select p from PriceListEntity p left join fetch p.currency left join fetch p.skuPrices sp where p.id = :id and p.deleted = false")
    Optional<PriceListEntity> findByIdWithFullData(@Param("id") UUID id);

    /**
     * Find highest priority active price list
     */
    @Query("select p from PriceListEntity p where p.active = true and p.deleted = false order by p.priority asc")
    Optional<PriceListEntity> findHighestPriority();

    /**
     * Find price lists by priority range with pagination
     */
    @Query("select p from PriceListEntity p where p.priority between :minPriority and :maxPriority and p.deleted = false order by p.priority asc")
    Page<PriceListEntity> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority, Pageable pageable);
}

