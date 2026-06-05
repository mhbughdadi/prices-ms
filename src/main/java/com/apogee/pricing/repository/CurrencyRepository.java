package com.apogee.pricing.repository;

import com.apogee.pricing.entity.CurrencyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyEntity, UUID> {

    /**
     * Find currency by ISO code
     */
    Optional<CurrencyEntity> findByCode(String code);

    /**
     * Find currency by code case-insensitive
     */
    @Query("select c from CurrencyEntity c where upper(c.code) = upper(:code)")
    Optional<CurrencyEntity> findByCodeIgnoreCase(@Param("code") String code);

    /**
     * Find all active currencies
     */
    @Query("select c from CurrencyEntity c where c.active = true order by c.name asc")
    List<CurrencyEntity> findAllActive();

    /**
     * Find all active currencies with pagination
     */
    @Query("select c from CurrencyEntity c where c.active = true order by c.name asc")
    Page<CurrencyEntity> findAllActive(Pageable pageable);

    /**
     * Find currencies by partial name match
     */
    @Query("select c from CurrencyEntity c where upper(c.name) like upper(concat('%', :name, '%')) order by c.name asc")
    List<CurrencyEntity> findByNameContaining(@Param("name") String name);

    /**
     * Find currencies by partial name match with pagination
     */
    @Query("select c from CurrencyEntity c where upper(c.name) like upper(concat('%', :name, '%')) order by c.name asc")
    Page<CurrencyEntity> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Find active currencies by name
     */
    @Query("select c from CurrencyEntity c where upper(c.name) like upper(concat('%', :name, '%')) and c.active = true order by c.name asc")
    List<CurrencyEntity> findActiveByNameContaining(@Param("name") String name);

    /**
     * Find if a currency code exists
     */
    boolean existsByCode(String code);

    /**
     * Find if an active currency exists by code
     */
    @Query("select case when count(c) > 0 then true else false end from CurrencyEntity c where c.code = :code and c.active = true")
    boolean existsActiveByCode(@Param("code") String code);

    /**
     * Count total currencies
     */
    long countByActive(boolean active);
}

