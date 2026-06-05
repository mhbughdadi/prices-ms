package com.apogee.pricing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pricing_rules")
@Getter
@Setter
public class PricingRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "rule_name", length = 255, nullable = false)
    private String ruleName;

    // store JSONB as string; columnDefinition ensures proper type in DB
    @Column(name = "condition_json", columnDefinition = "jsonb", nullable = false)
    private String conditionJson;

    @Column(name = "action_json", columnDefinition = "jsonb", nullable = false)
    private String actionJson;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}

