-- ============================================================================
-- PRICING SERVICE SCHEMA
-- PostgreSQL 16
-- Spring Boot 3.5.x + Flyway
-- UUID Strategy: gen_random_uuid()
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================================
-- ENUMS
-- ============================================================================

CREATE TYPE customer_segment_type AS ENUM
(
    'RETAIL',
    'VIP',
    'WHOLESALE',
    'EMPLOYEE',
    'DISTRIBUTOR'
);

CREATE TYPE tax_type AS ENUM
(
    'INCLUSIVE',
    'EXCLUSIVE'
);

CREATE TYPE price_event_type AS ENUM
(
    'FLASH_SALE',
    'BLACK_FRIDAY',
    'RAMADAN',
    'SEASONAL',
    'CUSTOM'
);

-- ============================================================================
-- CURRENCIES
-- ============================================================================

CREATE TABLE currencies
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_currency_code
ON currencies(code);

-- ============================================================================
-- EXCHANGE RATES
-- ============================================================================

CREATE TABLE exchange_rates
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    source_currency_id UUID NOT NULL,
    target_currency_id UUID NOT NULL,

    rate NUMERIC(18,8) NOT NULL,

    effective_date TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exchange_source
        FOREIGN KEY(source_currency_id)
        REFERENCES currencies(id),

    CONSTRAINT fk_exchange_target
        FOREIGN KEY(target_currency_id)
        REFERENCES currencies(id)
);

CREATE INDEX idx_exchange_rates_lookup
ON exchange_rates(source_currency_id, target_currency_id);

-- ============================================================================
-- CUSTOMER SEGMENTS
-- ============================================================================

CREATE TABLE customer_segments
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code customer_segment_type NOT NULL UNIQUE,

    description VARCHAR(255),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- PRICE LISTS
-- ============================================================================

CREATE TABLE price_lists
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL UNIQUE,

    name VARCHAR(200) NOT NULL,

    description VARCHAR(1000),

    currency_id UUID NOT NULL,

    priority INTEGER NOT NULL DEFAULT 100,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,

    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_pricelist_currency
        FOREIGN KEY(currency_id)
        REFERENCES currencies(id)
);

CREATE INDEX idx_pricelist_active
ON price_lists(active);

CREATE INDEX idx_pricelist_dates
ON price_lists(start_date, end_date);

-- ============================================================================
-- SKU PRICES
-- sku_id is owned by Catalog Service
-- ============================================================================

CREATE TABLE sku_prices
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    sku_id UUID NOT NULL,

    price_list_id UUID NOT NULL,

    regular_price NUMERIC(13,2) NOT NULL,

    sale_price NUMERIC(13,2),

    cost_price NUMERIC(13,2),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,

    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_sku_price_pricelist
        FOREIGN KEY(price_list_id)
        REFERENCES price_lists(id),

    CONSTRAINT chk_regular_price
        CHECK (regular_price >= 0),

    CONSTRAINT chk_sale_price
        CHECK (sale_price IS NULL OR sale_price >= 0),

    CONSTRAINT chk_cost_price
        CHECK (cost_price IS NULL OR cost_price >= 0)
);

CREATE INDEX idx_sku_prices_sku
ON sku_prices(sku_id);

CREATE INDEX idx_sku_prices_lookup
ON sku_prices(sku_id, active, start_date, end_date);

CREATE INDEX idx_sku_prices_pricelist
ON sku_prices(price_list_id);

CREATE UNIQUE INDEX idx_sku_pricelist_unique
ON sku_prices(sku_id, price_list_id)
WHERE deleted = FALSE;

-- ============================================================================
-- SEGMENT PRICES
-- ============================================================================

CREATE TABLE segment_prices
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    sku_id UUID NOT NULL,

    segment_id UUID NOT NULL,

    price NUMERIC(13,2) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_segment_price_segment
        FOREIGN KEY(segment_id)
        REFERENCES customer_segments(id),

    CONSTRAINT chk_segment_price
        CHECK(price >= 0)
);

CREATE INDEX idx_segment_prices_lookup
ON segment_prices(sku_id, segment_id);

-- ============================================================================
-- TIER PRICES
-- ============================================================================

CREATE TABLE tier_prices
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    sku_id UUID NOT NULL,

    min_quantity INTEGER NOT NULL,

    max_quantity INTEGER,

    unit_price NUMERIC(13,2) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_min_quantity
        CHECK(min_quantity > 0),

    CONSTRAINT chk_max_quantity
        CHECK(max_quantity IS NULL OR max_quantity >= min_quantity),

    CONSTRAINT chk_unit_price
        CHECK(unit_price >= 0)
);

CREATE INDEX idx_tier_prices_lookup
ON tier_prices(sku_id, min_quantity);

-- ============================================================================
-- REGION PRICES
-- ============================================================================

CREATE TABLE region_prices
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    sku_id UUID NOT NULL,

    country_code VARCHAR(2) NOT NULL,

    region_code VARCHAR(50),

    price NUMERIC(13,2) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_region_price
        CHECK(price >= 0)
);

CREATE INDEX idx_region_prices_lookup
ON region_prices(sku_id, country_code);

-- ============================================================================
-- TAX RULES
-- ============================================================================

CREATE TABLE tax_rules
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    country_code VARCHAR(2) NOT NULL,

    tax_name VARCHAR(100) NOT NULL,

    percentage NUMERIC(5,2) NOT NULL,

    tax_mode tax_type NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_tax_percentage
        CHECK(percentage >= 0 AND percentage <= 100)
);

CREATE INDEX idx_tax_rules_country
ON tax_rules(country_code);

-- ============================================================================
-- PRICE EVENTS
-- ============================================================================

CREATE TABLE price_events
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    sku_id UUID NOT NULL,

    event_name VARCHAR(255) NOT NULL,

    event_type price_event_type NOT NULL,

    future_price NUMERIC(13,2) NOT NULL,

    priority INTEGER NOT NULL DEFAULT 100,

    start_date TIMESTAMPTZ NOT NULL,

    end_date TIMESTAMPTZ NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_future_price
        CHECK(future_price >= 0),

    CONSTRAINT chk_event_dates
        CHECK(end_date > start_date)
);

CREATE INDEX idx_price_events_lookup
ON price_events(sku_id, start_date, end_date);

-- ============================================================================
-- PRICE BUNDLES
-- ============================================================================

CREATE TABLE price_bundles
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    bundle_code VARCHAR(100) NOT NULL UNIQUE,

    bundle_name VARCHAR(255) NOT NULL,

    bundle_price NUMERIC(13,2) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_bundle_price
        CHECK(bundle_price >= 0)
);

-- ============================================================================
-- BUNDLE ITEMS
-- ============================================================================

CREATE TABLE bundle_items
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    bundle_id UUID NOT NULL,

    sku_id UUID NOT NULL,

    quantity INTEGER NOT NULL,

    CONSTRAINT fk_bundle_item_bundle
        FOREIGN KEY(bundle_id)
        REFERENCES price_bundles(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_bundle_quantity
        CHECK(quantity > 0)
);

CREATE INDEX idx_bundle_items_bundle
ON bundle_items(bundle_id);

-- ============================================================================
-- DYNAMIC PRICING RULES
-- ============================================================================

CREATE TABLE pricing_rules
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    rule_name VARCHAR(255) NOT NULL,

    condition_json JSONB NOT NULL,

    action_json JSONB NOT NULL,

    priority INTEGER NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pricing_rules_json
ON pricing_rules
USING GIN(condition_json);

CREATE INDEX idx_pricing_rules_action_json
ON pricing_rules
USING GIN(action_json);

-- ============================================================================
-- PRICE AUDIT (PARTITIONED)
-- ============================================================================

CREATE TABLE price_audit
(
    id UUID NOT NULL DEFAULT gen_random_uuid(),

    sku_id UUID NOT NULL,

    old_price NUMERIC(13,2),

    new_price NUMERIC(13,2),

    changed_by VARCHAR(100),

    change_reason VARCHAR(1000),

    changed_at TIMESTAMPTZ NOT NULL,

    PRIMARY KEY(id, changed_at)
)
PARTITION BY RANGE(changed_at);

CREATE TABLE price_audit_2026
PARTITION OF price_audit
FOR VALUES FROM ('2026-01-01')
TO ('2027-01-01');

-- ============================================================================
-- OUTBOX EVENTS
-- For Kafka / RabbitMQ Event Publishing
-- ============================================================================

CREATE TABLE outbox_events
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    aggregate_type VARCHAR(100) NOT NULL,

    aggregate_id UUID NOT NULL,

    event_type VARCHAR(100) NOT NULL,

    payload JSONB NOT NULL,

    processed BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_processed
ON outbox_events(processed);

CREATE INDEX idx_outbox_payload
ON outbox_events
USING GIN(payload);

-- ============================================================================
-- SEED DATA
-- ============================================================================

INSERT INTO currencies(code,name,symbol)
VALUES
('AED','UAE Dirham','AED'),
('USD','US Dollar','$'),
('SAR','Saudi Riyal','SAR'),
('EUR','Euro','€'),
('EGP','Egyptian Pound','EGP');

INSERT INTO customer_segments(code,description)
VALUES
('RETAIL','Retail Customers'),
('VIP','VIP Customers'),
('WHOLESALE','Wholesale Customers'),
('EMPLOYEE','Employee Pricing'),
('DISTRIBUTOR','Distributor Pricing');

INSERT INTO tax_rules
(
    country_code,
    tax_name,
    percentage,
    tax_mode
)
VALUES
('AE','VAT',5,'EXCLUSIVE'),
('SA','VAT',15,'EXCLUSIVE');

-- ============================================================================
-- RECOMMENDED FUTURE PARTITIONS
-- ============================================================================

-- CREATE TABLE price_audit_2027
-- PARTITION OF price_audit
-- FOR VALUES FROM ('2027-01-01')
-- TO ('2028-01-01');

-- CREATE TABLE price_audit_2028
-- PARTITION OF price_audit
-- FOR VALUES FROM ('2028-01-01')
-- TO ('2029-01-01');