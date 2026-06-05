package com.apogee.pricing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "price_bundles")
@Getter
@Setter
public class PriceBundleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "bundle_code", length = 100, nullable = false, unique = true)
    private String bundleCode;

    @Column(name = "bundle_name", length = 255, nullable = false)
    private String bundleName;

    @Column(name = "bundle_price", precision = 13, scale = 2, nullable = false)
    private BigDecimal bundlePrice;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "bundle", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<BundleItemEntity> items = new ArrayList<>();
}

