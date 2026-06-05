package com.apogee.pricing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "bundle_items", indexes = {
        @Index(name = "idx_bundle_items_bundle", columnList = "bundle_id")
})
@Getter
@Setter
public class BundleItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private PriceBundleEntity bundle;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}

