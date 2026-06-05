package com.apogee.pricing.repository;

import com.apogee.pricing.entity.TaxRuleEntity;
import com.apogee.pricing.entity.enums.TaxType;
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
public interface TaxRuleRepository extends JpaRepository<TaxRuleEntity, UUID> {

    /**
     * Find first active tax rule for a country
     */
    @Query("select tr from TaxRuleEntity tr where tr.countryCode = :countryCode and tr.active = true")
    Optional<TaxRuleEntity> findFirstActiveByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Find all tax rules for a country
     */
    @Query("select tr from TaxRuleEntity tr where tr.countryCode = :countryCode order by tr.taxName asc")
    List<TaxRuleEntity> findByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Find all active tax rules for a country
     */
    @Query("select tr from TaxRuleEntity tr where tr.countryCode = :countryCode and tr.active = true order by tr.taxName asc")
    List<TaxRuleEntity> findActiveByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Find all active tax rules for a country with pagination
     */
    @Query("select tr from TaxRuleEntity tr where tr.countryCode = :countryCode and tr.active = true order by tr.taxName asc")
    Page<TaxRuleEntity> findActiveByCountryCode(@Param("countryCode") String countryCode, Pageable pageable);

    /**
     * Find all active tax rules
     */
    @Query("select tr from TaxRuleEntity tr where tr.active = true order by tr.countryCode asc, tr.taxName asc")
    List<TaxRuleEntity> findAllActive();

    /**
     * Find all active tax rules with pagination
     */
    @Query("select tr from TaxRuleEntity tr where tr.active = true order by tr.countryCode asc, tr.taxName asc")
    Page<TaxRuleEntity> findAllActive(Pageable pageable);

    /**
     * Find tax rules by tax name
     */
    @Query("select tr from TaxRuleEntity tr where upper(tr.taxName) like upper(concat('%', :name, '%')) order by tr.countryCode asc")
    List<TaxRuleEntity> findByTaxNameContaining(@Param("name") String name);

    /**
     * Find tax rules by tax name with pagination
     */
    @Query("select tr from TaxRuleEntity tr where upper(tr.taxName) like upper(concat('%', :name, '%')) order by tr.countryCode asc")
    Page<TaxRuleEntity> findByTaxNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Find active tax rules by tax name
     */
    @Query("select tr from TaxRuleEntity tr where upper(tr.taxName) like upper(concat('%', :name, '%')) and tr.active = true order by tr.countryCode asc")
    List<TaxRuleEntity> findActiveByTaxNameContaining(@Param("name") String name);

    /**
     * Find tax rules by tax type
     */
    @Query("select tr from TaxRuleEntity tr where tr.taxMode = :taxMode order by tr.countryCode asc, tr.taxName asc")
    List<TaxRuleEntity> findByTaxType(@Param("taxMode") TaxType taxMode);

    /**
     * Find active tax rules by tax type
     */
    @Query("select tr from TaxRuleEntity tr where tr.taxMode = :taxMode and tr.active = true order by tr.countryCode asc, tr.taxName asc")
    List<TaxRuleEntity> findActiveByTaxType(@Param("taxMode") TaxType taxMode);

    /**
     * Find active tax rules by tax type with pagination
     */
    @Query("select tr from TaxRuleEntity tr where tr.taxMode = :taxMode and tr.active = true order by tr.countryCode asc, tr.taxName asc")
    Page<TaxRuleEntity> findActiveByTaxType(@Param("taxMode") TaxType taxMode, Pageable pageable);

    /**
     * Find tax rules with percentage in range
     */
    @Query("select tr from TaxRuleEntity tr where tr.percentage between :minPercentage and :maxPercentage order by tr.percentage asc")
    List<TaxRuleEntity> findByPercentageRange(@Param("minPercentage") BigDecimal minPercentage, @Param("maxPercentage") BigDecimal maxPercentage);

    /**
     * Find tax rules with percentage greater than
     */
    @Query("select tr from TaxRuleEntity tr where tr.percentage > :percentage order by tr.percentage desc")
    List<TaxRuleEntity> findByPercentageGreaterThan(@Param("percentage") BigDecimal percentage);

    /**
     * Find tax rules with percentage less than
     */
    @Query("select tr from TaxRuleEntity tr where tr.percentage < :percentage order by tr.percentage asc")
    List<TaxRuleEntity> findByPercentageLessThan(@Param("percentage") BigDecimal percentage);

    /**
     * Find highest tax rate for a country
     */
    @Query("select tr from TaxRuleEntity tr where tr.countryCode = :countryCode and tr.active = true order by tr.percentage desc")
    Optional<TaxRuleEntity> findHighestTaxRateByCountry(@Param("countryCode") String countryCode);

    /**
     * Find lowest tax rate for a country
     */
    @Query("select tr from TaxRuleEntity tr where tr.countryCode = :countryCode and tr.active = true order by tr.percentage asc")
    Optional<TaxRuleEntity> findLowestTaxRateByCountry(@Param("countryCode") String countryCode);

    /**
     * Find tax rule by country and tax name
     */
    @Query("select tr from TaxRuleEntity tr where tr.countryCode = :countryCode and tr.taxName = :taxName and tr.active = true")
    Optional<TaxRuleEntity> findByCountryAndTaxName(@Param("countryCode") String countryCode, @Param("taxName") String taxName);

    /**
     * Find tax rules created after a date
     */
    @Query("select tr from TaxRuleEntity tr where tr.createdAt >= :createdAfter order by tr.createdAt desc")
    List<TaxRuleEntity> findCreatedAfter(@Param("createdAfter") OffsetDateTime createdAfter);

    /**
     * Check if tax rule exists for country
     */
    @Query("select case when count(tr) > 0 then true else false end from TaxRuleEntity tr where tr.countryCode = :countryCode")
    boolean existsByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Check if active tax rule exists for country
     */
    @Query("select case when count(tr) > 0 then true else false end from TaxRuleEntity tr where tr.countryCode = :countryCode and tr.active = true")
    boolean existsActiveByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Count tax rules for a country
     */
    long countByCountryCode(String countryCode);

    /**
     * Count active tax rules for a country
     */
    @Query("select count(tr) from TaxRuleEntity tr where tr.countryCode = :countryCode and tr.active = true")
    long countActiveByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Count tax rules by tax type
     */
    long countByTaxMode(TaxType taxMode);

    /**
     * Get average tax percentage for a country
     */
    @Query("select avg(tr.percentage) from TaxRuleEntity tr where tr.countryCode = :countryCode and tr.active = true")
    BigDecimal getAverageTaxRateByCountry(@Param("countryCode") String countryCode);

    /**
     * Get all unique country codes
     */
    @Query("select distinct tr.countryCode from TaxRuleEntity tr where tr.active = true order by tr.countryCode asc")
    List<String> findAllCountryCodes();

    /**
     * Get all countries with at least one active tax rule
     */
    @Query("select distinct tr.countryCode from TaxRuleEntity tr where tr.active = true order by tr.countryCode asc")
    Page<String> findAllCountryCodes(Pageable pageable);
}

