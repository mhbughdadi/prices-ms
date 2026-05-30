# Pricing Microservice

> Enterprise-grade Pricing Engine for modern ecommerce platforms built with Java 21, Spring Boot, PostgreSQL, Redis, Flyway, and Docker.

---

## Overview

The Pricing Microservice is responsible for managing, calculating, and exposing product prices across multiple business dimensions while serving as the single source of truth for all pricing-related operations within the commerce ecosystem.

The service provides flexible pricing capabilities including:

* SKU Pricing
* Price Lists
* Customer Segment Pricing
* Tier Pricing
* Regional Pricing
* Multi-Currency Support
* Tax Calculation
* Scheduled Pricing
* Bundle Pricing
* Dynamic Pricing Rules
* Price Auditing
* Event-Driven Integration

The service is designed as a standalone domain within a microservices architecture and intentionally does not manage products, inventory, orders, or promotions.

---

# Business Goals

The Pricing Service answers the following business question:

> What is the effective price of SKU X for customer Y in region Z at time T?

The effective price is calculated based on:

* Base Price
* Sale Price
* Customer Segment
* Region
* Quantity
* Currency
* Tax Rules
* Active Pricing Events
* Dynamic Pricing Rules

---

# Architecture

```text
                     +----------------+
                     | Catalog Service|
                     +----------------+
                              |
                              |
                              v

+------------------------------------------------------+
|                Pricing Microservice                  |
|------------------------------------------------------|
|                                                      |
| Price Management                                     |
| Price Calculation Engine                             |
| Customer Segment Engine                              |
| Currency Engine                                      |
| Tax Engine                                           |
| Bundle Pricing Engine                                |
| Dynamic Pricing Rule Engine                          |
| Event Publisher                                      |
| Audit Module                                         |
| Cache Layer                                          |
|                                                      |
+------------------------------------------------------+
             |                       |
             |                       |
             v                       v

       PostgreSQL                Redis
```

---

# Domain Boundaries

## Owned by Pricing Service

* Price Lists
* SKU Prices
* Sale Prices
* Customer Segment Prices
* Tier Prices
* Region Prices
* Exchange Rates
* Tax Rules
* Scheduled Price Events
* Bundle Prices
* Dynamic Pricing Rules
* Price Audit History

## Not Owned by Pricing Service

* Products
* Categories
* Subcategories
* Tags
* Inventory
* Orders
* Promotions
* Coupons
* Payments

---

# Core Features

## Price Lists

Support multiple price lists such as:

* UAE Retail
* UAE Wholesale
* KSA Retail
* VIP Pricing
* Employee Pricing

## SKU Pricing

Store pricing at SKU level.

Example:

```text
SKU: IPHONE16-BLK-256

Regular Price: 4999 AED
Sale Price: 4499 AED
```

## Customer Segment Pricing

Different pricing per customer type.

Supported segments:

* Retail
* VIP
* Wholesale
* Employee
* Distributor

## Tier Pricing

Volume-based pricing.

Example:

```text
1-9 Items      = 100 AED
10-49 Items    = 90 AED
50+ Items      = 80 AED
```

## Regional Pricing

Support country and region-specific pricing.

Example:

```text
UAE = 100 AED
KSA = 110 SAR
Egypt = 1200 EGP
```

## Multi-Currency Support

Supported currencies:

* AED
* USD
* SAR
* EUR
* EGP

Features:

* Exchange Rate Management
* Currency Conversion
* Effective Price Conversion

## Tax Management

Support:

* Inclusive Tax
* Exclusive Tax

Example:

```text
UAE VAT = 5%
KSA VAT = 15%
```

## Scheduled Pricing

Activate prices automatically.

Examples:

* Black Friday
* Ramadan Sale
* Seasonal Sale
* Flash Sale

## Bundle Pricing

Example:

```text
Laptop + Mouse + Bag

Individual Price: 5600 AED

Bundle Price: 4999 AED
```

## Dynamic Pricing

Rule-based pricing engine.

Example:

```json
{
  "inventory": "< 10"
}
```

Action:

```json
{
  "increasePrice": "10%"
}
```

## Price Audit

Track every price change.

Store:

* Old Price
* New Price
* User
* Timestamp
* Reason

---

# Database Design

The service uses PostgreSQL 16 as the primary transactional database.

Core tables:

```text
currencies
exchange_rates
customer_segments
price_lists
sku_prices
segment_prices
tier_prices
region_prices
tax_rules
price_events
price_bundles
bundle_items
pricing_rules
price_audit
outbox_events
```

Features:

* UUID Primary Keys
* Foreign Keys
* Check Constraints
* Optimistic Locking
* Soft Deletes
* JSONB Support
* GIN Indexes
* Table Partitioning
* Outbox Pattern

---

# Effective Price Calculation Flow

The Pricing Engine calculates prices in the following order:

```text
1. Load Base Price

2. Apply Active Sale Price

3. Apply Scheduled Event Price

4. Apply Region Price

5. Apply Customer Segment Price

6. Apply Tier Price

7. Apply Dynamic Pricing Rules

8. Apply Currency Conversion

9. Apply Tax Rules

10. Return Effective Price
```

---

# API Overview

## Pricing APIs

### Get Effective Price

```http
GET /api/v1/prices/skus/{skuId}
```

### Bulk Price Lookup

```http
POST /api/v1/prices/bulk
```

### Create Price

```http
POST /api/v1/prices
```

### Update Price

```http
PUT /api/v1/prices/{id}
```

### Delete Price

```http
DELETE /api/v1/prices/{id}
```

---

## Price Lists

```http
POST /api/v1/price-lists

GET /api/v1/price-lists/{id}

PUT /api/v1/price-lists/{id}

DELETE /api/v1/price-lists/{id}
```

---

## Tier Pricing

```http
POST /api/v1/tier-prices

PUT /api/v1/tier-prices/{id}

DELETE /api/v1/tier-prices/{id}
```

---

## Currency Management

```http
GET /api/v1/currencies

POST /api/v1/currencies

PUT /api/v1/currencies/{id}
```

---

## Exchange Rates

```http
GET /api/v1/exchange-rates

POST /api/v1/exchange-rates
```

---

## Tax Rules

```http
GET /api/v1/taxes

POST /api/v1/taxes

PUT /api/v1/taxes/{id}
```

---

## Price History

```http
GET /api/v1/prices/{skuId}/history
```

---

# Event-Driven Architecture

The service supports asynchronous integration through the Outbox Pattern.

Published events include:

```text
PriceCreated
PriceUpdated
PriceDeleted
PriceListCreated
PriceListUpdated
ExchangeRateChanged
TaxRuleChanged
```

Future integrations:

* Kafka
* RabbitMQ
* AWS SNS/SQS

---

# Performance Strategy

## PostgreSQL

Used for:

* Transactional Consistency
* Relational Queries
* Audit Storage

## Redis

Used for:

* Effective Price Cache
* Region Price Cache
* Customer Segment Price Cache

Example keys:

```text
price:sku:{skuId}

price:sku:{skuId}:segment:{segment}

price:sku:{skuId}:country:{country}
```

---

# Technology Stack

## Backend

* Java 21
* Spring Boot 3.5.x
* Spring Data JPA
* Hibernate 6

## Database

* PostgreSQL 16

## Cache

* Redis 7

## Migrations

* Flyway

## API Documentation

* OpenAPI
* Swagger

## Containerization

* Docker
* Docker Compose

## Monitoring

* Micrometer
* Prometheus
* Grafana

## Logging

* Log4j2
* ELK Stack

## Tracing

* OpenTelemetry

---

# Local Development

## Start Infrastructure

```bash
docker compose up -d
```

## Run Application

```bash
mvn clean spring-boot:run
```

## Execute Migrations

Flyway migrations are automatically executed during application startup.

---

# Non-Functional Requirements

## Availability

```text
99.99%
```

## Response Time

```text
Single SKU Lookup < 50 ms

Bulk Price Lookup < 200 ms
```

## Scalability

```text
100,000+ Products

5,000,000+ SKUs

50,000+ Requests / Minute
```

## Security

* JWT Authentication
* OAuth2 Resource Server
* Role-Based Access Control
* Audit Logging

---

# Future Enhancements

* AI-Based Dynamic Pricing
* Demand Forecasting
* Competitor Price Monitoring
* Marketplace Seller Pricing
* Real-Time Pricing Events
* GraphQL Support
* Multi-Tenant Support
* Price Approval Workflow
* Price Simulation Engine
* Pricing Analytics Dashboard

---

# License

This project is intended for enterprise ecommerce platforms and serves as the central pricing domain within a scalable microservices architecture.
