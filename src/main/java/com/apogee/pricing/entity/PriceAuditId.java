package com.apogee.pricing.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class PriceAuditId implements Serializable {
    private UUID id;
    private OffsetDateTime changedAt;

    public PriceAuditId() {
    }

    public PriceAuditId(UUID id, OffsetDateTime changedAt) {
        this.id = id;
        this.changedAt = changedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceAuditId that = (PriceAuditId) o;
        return Objects.equals(id, that.id) && Objects.equals(changedAt, that.changedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, changedAt);
    }
}

