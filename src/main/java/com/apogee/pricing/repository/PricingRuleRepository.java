package com.apogee.pricing.repository;

import com.apogee.pricing.entity.PricingRuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, UUID> {

    /**
     * Find rule by name
     */
    Optional<PricingRuleEntity> findByRuleName(String ruleName);

    /**
     * Find rule by name case-insensitive
     */
    @Query("select pr from PricingRuleEntity pr where upper(pr.ruleName) = upper(:ruleName)")
    Optional<PricingRuleEntity> findByRuleNameIgnoreCase(@Param("ruleName") String ruleName);

    /**
     * Find all active rules
     */
    @Query("select pr from PricingRuleEntity pr where pr.active = true order by pr.priority asc")
    List<PricingRuleEntity> findAllActive();

    /**
     * Find all active rules with pagination
     */
    @Query("select pr from PricingRuleEntity pr where pr.active = true order by pr.priority asc")
    Page<PricingRuleEntity> findAllActive(Pageable pageable);

    /**
     * Find rules by name containing
     */
    @Query("select pr from PricingRuleEntity pr where upper(pr.ruleName) like upper(concat('%', :name, '%')) order by pr.priority asc")
    List<PricingRuleEntity> findByRuleNameContaining(@Param("name") String name);

    /**
     * Find rules by name containing with pagination
     */
    @Query("select pr from PricingRuleEntity pr where upper(pr.ruleName) like upper(concat('%', :name, '%')) order by pr.priority asc")
    Page<PricingRuleEntity> findByRuleNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Find active rules by name
     */
    @Query("select pr from PricingRuleEntity pr where upper(pr.ruleName) like upper(concat('%', :name, '%')) and pr.active = true order by pr.priority asc")
    List<PricingRuleEntity> findActiveByRuleNameContaining(@Param("name") String name);

    /**
     * Find rules by priority
     */
    @Query("select pr from PricingRuleEntity pr where pr.priority = :priority order by pr.ruleName asc")
    List<PricingRuleEntity> findByPriority(@Param("priority") Integer priority);

    /**
     * Find rules by priority range
     */
    @Query("select pr from PricingRuleEntity pr where pr.priority between :minPriority and :maxPriority order by pr.priority asc")
    List<PricingRuleEntity> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);

    /**
     * Find highest priority active rules
     */
    @Query("select pr from PricingRuleEntity pr where pr.active = true order by pr.priority asc, pr.ruleName asc")
    List<PricingRuleEntity> findByHighestPriority();

    /**
     * Find highest priority active rules with limit
     */
    @Query("select pr from PricingRuleEntity pr where pr.active = true order by pr.priority asc")
    List<PricingRuleEntity> findTopByHighestPriority(@Param("limit") int limit);

    /**
     * Find rules created after a specific date
     */
    @Query("select pr from PricingRuleEntity pr where pr.createdAt >= :createdAfter order by pr.createdAt desc")
    List<PricingRuleEntity> findCreatedAfter(@Param("createdAfter") OffsetDateTime createdAfter);

    /**
     * Find rules created within a date range
     */
    @Query("select pr from PricingRuleEntity pr where pr.createdAt between :startDate and :endDate order by pr.createdAt desc")
    List<PricingRuleEntity> findCreatedBetween(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Find rules by condition JSON content
     */
    @Query("select pr from PricingRuleEntity pr where pr.conditionJson like concat('%', :searchTerm, '%') order by pr.priority asc")
    List<PricingRuleEntity> findByConditionContaining(@Param("searchTerm") String searchTerm);

    /**
     * Find rules by action JSON content
     */
    @Query("select pr from PricingRuleEntity pr where pr.actionJson like concat('%', :searchTerm, '%') order by pr.priority asc")
    List<PricingRuleEntity> findByActionContaining(@Param("searchTerm") String searchTerm);

    /**
     * Check if rule name exists
     */
    boolean existsByRuleName(String ruleName);

    /**
     * Check if active rule exists by name
     */
    @Query("select case when count(pr) > 0 then true else false end from PricingRuleEntity pr where pr.ruleName = :ruleName and pr.active = true")
    boolean existsActiveByRuleName(@Param("ruleName") String ruleName);

    /**
     * Count active rules
     */
    long countByActive(boolean active);

    /**
     * Count rules by priority
     */
    long countByPriority(Integer priority);

    /**
     * Find all rules sorted by priority
     */
    @Query("select pr from PricingRuleEntity pr order by pr.priority asc")
    List<PricingRuleEntity> findAllByPriority();

    /**
     * Find all rules sorted by priority with pagination
     */
    @Query("select pr from PricingRuleEntity pr order by pr.priority asc")
    Page<PricingRuleEntity> findAllByPriority(Pageable pageable);

    /**
     * Delete inactive rules created before a date
     */
    @Query("delete from PricingRuleEntity pr where pr.active = false and pr.createdAt < :cutoffDate")
    void deleteInactiveOlderThan(@Param("cutoffDate") OffsetDateTime cutoffDate);
}

