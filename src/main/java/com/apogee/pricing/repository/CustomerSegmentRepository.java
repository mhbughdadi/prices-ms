package com.apogee.pricing.repository;

import com.apogee.pricing.entity.CustomerSegmentEntity;
import com.apogee.pricing.entity.enums.CustomerSegmentType;
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
public interface CustomerSegmentRepository extends JpaRepository<CustomerSegmentEntity, UUID> {

    /**
     * Find segment by code
     */
    Optional<CustomerSegmentEntity> findByCode(CustomerSegmentType code);

    /**
     * Find all active segments
     */
    @Query("select cs from CustomerSegmentEntity cs where cs.active = true order by cs.code asc")
    List<CustomerSegmentEntity> findAllActive();

    /**
     * Find all active segments with pagination
     */
    @Query("select cs from CustomerSegmentEntity cs where cs.active = true order by cs.code asc")
    Page<CustomerSegmentEntity> findAllActive(Pageable pageable);

    /**
     * Find segments by description containing text
     */
    @Query("select cs from CustomerSegmentEntity cs where upper(cs.description) like upper(concat('%', :description, '%')) order by cs.code asc")
    List<CustomerSegmentEntity> findByDescriptionContaining(@Param("description") String description);

    /**
     * Find segments by description containing text with pagination
     */
    @Query("select cs from CustomerSegmentEntity cs where upper(cs.description) like upper(concat('%', :description, '%')) order by cs.code asc")
    Page<CustomerSegmentEntity> findByDescriptionContaining(@Param("description") String description, Pageable pageable);

    /**
     * Find active segments by description
     */
    @Query("select cs from CustomerSegmentEntity cs where upper(cs.description) like upper(concat('%', :description, '%')) and cs.active = true order by cs.code asc")
    List<CustomerSegmentEntity> findActiveByDescriptionContaining(@Param("description") String description);

    /**
     * Check if segment code exists
     */
    boolean existsByCode(CustomerSegmentType code);

    /**
     * Check if active segment exists by code
     */
    @Query("select case when count(cs) > 0 then true else false end from CustomerSegmentEntity cs where cs.code = :code and cs.active = true")
    boolean existsActiveByCode(@Param("code") CustomerSegmentType code);

    /**
     * Count active segments
     */
    long countByActive(boolean active);

    /**
     * Find segment by code with segment prices eagerly loaded
     */
    @Query("select cs from CustomerSegmentEntity cs left join fetch cs.segmentPrices sp where cs.code = :code")
    Optional<CustomerSegmentEntity> findByCodeWithPrices(@Param("code") CustomerSegmentType code);

    /**
     * Get count of segment prices for a segment
     */
    @Query("select count(sp) from SegmentPriceEntity sp where sp.segment.id = :segmentId")
    long countSegmentPrices(@Param("segmentId") UUID segmentId);
}

