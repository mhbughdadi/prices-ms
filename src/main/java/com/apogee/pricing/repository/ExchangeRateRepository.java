package com.apogee.pricing.repository;

import com.apogee.pricing.entity.CurrencyEntity;
import com.apogee.pricing.entity.ExchangeRateEntity;
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
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, UUID> {

    /**
     * Find exchange rate between two currencies
     */
    @Query("select er from ExchangeRateEntity er where er.sourceCurrency.id = :sourceCurrencyId and er.targetCurrency.id = :targetCurrencyId")
    Optional<ExchangeRateEntity> findByCurrencyPair(@Param("sourceCurrencyId") UUID sourceCurrencyId, @Param("targetCurrencyId") UUID targetCurrencyId);

    /**
     * Find latest exchange rate between two currencies
     */
    @Query("select er from ExchangeRateEntity er where er.sourceCurrency.id = :sourceCurrencyId and er.targetCurrency.id = :targetCurrencyId order by er.effectiveDate desc")
    Optional<ExchangeRateEntity> findLatestByCurrencyPair(@Param("sourceCurrencyId") UUID sourceCurrencyId, @Param("targetCurrencyId") UUID targetCurrencyId);

    /**
     * Find exchange rates from a source currency
     */
    List<ExchangeRateEntity> findBySourceCurrency(CurrencyEntity sourceCurrency);

    /**
     * Find exchange rates from a source currency with pagination
     */
    Page<ExchangeRateEntity> findBySourceCurrency(CurrencyEntity sourceCurrency, Pageable pageable);

    /**
     * Find exchange rates to a target currency
     */
    List<ExchangeRateEntity> findByTargetCurrency(CurrencyEntity targetCurrency);

    /**
     * Find exchange rates to a target currency with pagination
     */
    Page<ExchangeRateEntity> findByTargetCurrency(CurrencyEntity targetCurrency, Pageable pageable);

    /**
     * Find exchange rates effective on or after a given date
     */
    @Query("select er from ExchangeRateEntity er where er.effectiveDate >= :effectiveDate order by er.effectiveDate desc")
    List<ExchangeRateEntity> findEffectiveAfter(@Param("effectiveDate") OffsetDateTime effectiveDate);

    /**
     * Find exchange rates effective on or after a given date with pagination
     */
    @Query("select er from ExchangeRateEntity er where er.effectiveDate >= :effectiveDate order by er.effectiveDate desc")
    Page<ExchangeRateEntity> findEffectiveAfter(@Param("effectiveDate") OffsetDateTime effectiveDate, Pageable pageable);

    /**
     * Find exchange rates for a date range
     */
    @Query("select er from ExchangeRateEntity er where er.effectiveDate between :startDate and :endDate order by er.effectiveDate desc")
    List<ExchangeRateEntity> findByEffectiveDateBetween(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Find exchange rates for a date range with pagination
     */
    @Query("select er from ExchangeRateEntity er where er.effectiveDate between :startDate and :endDate order by er.effectiveDate desc")
    Page<ExchangeRateEntity> findByEffectiveDateBetween(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, Pageable pageable);

    /**
     * Find all exchange rates for a given pair sorted by effective date
     */
    @Query("select er from ExchangeRateEntity er where er.sourceCurrency.id = :sourceCurrencyId and er.targetCurrency.id = :targetCurrencyId order by er.effectiveDate desc")
    List<ExchangeRateEntity> findCurrencyPairHistory(@Param("sourceCurrencyId") UUID sourceCurrencyId, @Param("targetCurrencyId") UUID targetCurrencyId);

    /**
     * Find all exchange rates for a given pair sorted by effective date with pagination
     */
    @Query("select er from ExchangeRateEntity er where er.sourceCurrency.id = :sourceCurrencyId and er.targetCurrency.id = :targetCurrencyId order by er.effectiveDate desc")
    Page<ExchangeRateEntity> findCurrencyPairHistory(@Param("sourceCurrencyId") UUID sourceCurrencyId, @Param("targetCurrencyId") UUID targetCurrencyId, Pageable pageable);

    /**
     * Check if exchange rate exists for a currency pair
     */
    @Query("select case when count(er) > 0 then true else false end from ExchangeRateEntity er where er.sourceCurrency.id = :sourceCurrencyId and er.targetCurrency.id = :targetCurrencyId")
    boolean existsByCurrencyPair(@Param("sourceCurrencyId") UUID sourceCurrencyId, @Param("targetCurrencyId") UUID targetCurrencyId);

    /**
     * Delete outdated exchange rates
     */
    @Query("delete from ExchangeRateEntity er where er.effectiveDate < :cutoffDate")
    void deleteOutdatedRates(@Param("cutoffDate") OffsetDateTime cutoffDate);

    /**
     * Count exchange rates for a source currency
     */
    long countBySourceCurrency(CurrencyEntity sourceCurrency);

    /**
     * Count exchange rates for a target currency
     */
    long countByTargetCurrency(CurrencyEntity targetCurrency);
}

