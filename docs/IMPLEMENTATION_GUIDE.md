# Implementation Guide: Spring Boot Microservice Architecture

## Complete scaffolding instructions for building controllers, facades (backing services), services, and utilities with minimal token usage.

---

## 1. File Structure Template

Copy this exact structure for all new microservices:

```
src/main/java/com/yourcompany/yourmodule/
├── controller/                          # HTTP layer
│   ├── YourResourceController.java
│   └── [Other controllers]
├── facade/                              # API orchestration (backup services renamed)
│   ├── YourResourceFacade.java
│   └── [Other facades]
├── services/                            # Business logic contracts
│   ├── YourResourceService.java
│   └── [Other service interfaces]
├── services/impl/                       # @Transactional implementations
│   ├── YourResourceServiceImpl.java
│   └── [Other implementations]
├── repositories/                        # Spring Data JPA
│   ├── YourResourceRepository.java
│   └── [Other repositories]
├── entities/                            # JPA @Entity classes
│   ├── YourResourceEntity.java
│   └── [Other entities]
├── models/                              # Domain models (no annotations)
│   ├── YourResource.java
│   └── [Other models]
├── dtos/
│   ├── inputs/                          # @RequestBody DTOs
│   │   ├── YourResourceDto.java
│   │   └── YourResourceInputDto.java
│   └── outputs/                         # @ResponseBody DTOs
│       ├── YourResourceOutputDto.java
│       ├── YourResourceResponseDto.java
│       └── AllYourResourcesResponseDto.java
├── exceptions/                          # Custom exceptions
│   ├── YourModuleException.java
│   ├── ResourceNotFoundException.java
│   ├── DatabaseException.java
│   └── [Others]
├── constants/                           # Non-instantiable constant classes
│   ├── YourModuleConstants.java
│   ├── ErrorMessages.java
│   ├── ApiMessages.java
│   └── LoggingKeys.java
├── enums/                               # Enumerations
│   ├── ResourceStatus.java
│   └── [Others]
├── configs/                             # @Configuration beans
│   ├── OpenApiConfig.java
│   ├── OpenApiConstants.java
│   └── [Others]
├── filters/                             # Servlet filters
│   └── CorrelationIdFilter.java
├── utilities/                           # Helper methods & utils
│   ├── ValidationUtil.java
│   └── [Others]
└── YourModuleApplication.java           # @SpringBootApplication
```

---

## 2. Non-Instantiable Constants Class Template

**Purpose:** Centralize all strings to reduce agent token usage (don't search for strings scattered in 10 files).

```java
package com.yourcompany.yourmodule.constants;

/**
 * Centralized constants for YourModule microservice.
 * Non-instantiable class holding all string literals.
 */
public final class YourModuleConstants {

    // Constructor is private to prevent instantiation
    private YourModuleConstants() {
        throw new AssertionError("Cannot instantiate YourModuleConstants");
    }

    // ====== ERROR MESSAGES (i18n keys) ======
    public static final String ERROR_RESOURCE_NOT_FOUND = "error.resource.not.found";
    public static final String ERROR_RESOURCE_ALREADY_EXISTS = "error.resource.already.exists";
    public static final String ERROR_INVALID_INPUT = "error.invalid.input";
    public static final String ERROR_DATABASE_FAILURE = "error.database.failure";
    public static final String ERROR_UNAUTHORIZED = "error.unauthorized";
    public static final String ERROR_FORBIDDEN = "error.forbidden";
    
    // ====== SUCCESS MESSAGES ======
    public static final String SUCCESS_RESOURCE_CREATED = "success.resource.created";
    public static final String SUCCESS_RESOURCE_UPDATED = "success.resource.updated";
    public static final String SUCCESS_RESOURCE_DELETED = "success.resource.deleted";

    // ====== LOGGING KEYS (for MDC, request tracking) ======
    public static final String REQUEST_ID = "requestId";
    public static final String X_REQUEST_ID = "X-Request-Id";
    public static final String USER_ID = "userId";
    public static final String CORRELATION_ID = "correlationId";
    
    // ====== API RESPONSE KEYS ======
    public static final String RESPONSE_STATUS = "status";
    public static final String RESPONSE_MESSAGE = "message";
    public static final String RESPONSE_DATA = "data";
    public static final String RESPONSE_TIMESTAMP = "timestamp";
    public static final String RESPONSE_ERROR_CODE = "errorCode";
    
    // ====== DATABASE/ENTITY FIELD NAMES ======
    public static final String FIELD_ID = "id";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_UPDATED_AT = "updatedAt";
    public static final String FIELD_CREATED_BY = "createdBy";
    public static final String FIELD_UPDATED_BY = "updatedBy";
    
    // ====== VALIDATION MESSAGES ======
    public static final String VALIDATION_REQUIRED_FIELD = "validation.required.field";
    public static final String VALIDATION_INVALID_FORMAT = "validation.invalid.format";
    public static final String VALIDATION_MIN_LENGTH = "validation.min.length";
    public static final String VALIDATION_MAX_LENGTH = "validation.max.length";
    
    // ====== STATUS/ENUM VALUES ======
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_DELETED = "DELETED";
}
```

**Usage in services:**
```java
import static com.yourcompany.yourmodule.constants.YourModuleConstants.*;

// In service:
throw new ResourceNotFoundException(ERROR_RESOURCE_NOT_FOUND, resourceId);
```

---

## 3. Exception Classes Template

Create two main exceptions + specific subclasses:

### RecordNotFoundException.java
```java
package com.yourcompany.yourmodule.exceptions;

import lombok.Getter;
import java.io.Serial;

/**
 * Thrown when a record is not found in the database.
 * Includes the record ID for logging and error tracking.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long recordId;
    private final Long[] recordIds;

    /**
     * Single record not found.
     *
     * @param message the error message (i18n key)
     * @param recordId the ID of the missing record
     */
    public ResourceNotFoundException(String message, Long recordId) {
        super(message, null);
        this.recordId = recordId;
        this.recordIds = null;
    }

    /**
     * Multiple records not found.
     *
     * @param message the error message (i18n key)
     * @param recordIds the IDs of the missing records
     */
    public ResourceNotFoundException(String message, Long... recordIds) {
        super(message, null);
        this.recordId = null;
        this.recordIds = recordIds;
    }

    /**
     * Constructor with cause for wrapping other exceptions.
     *
     * @param message the error message (i18n key)
     * @param cause the underlying exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.recordId = null;
        this.recordIds = null;
    }
}
```

### DatabaseException.java
```java
package com.yourcompany.yourmodule.exceptions;

import lombok.Getter;
import java.io.Serial;
import java.io.Serializable;

/**
 * Thrown when a database operation fails.
 * Includes entity class and record IDs for context.
 */
@Getter
public class DatabaseException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long[] recordIds;
    private final Class<?> entityClass;

    /**
     * Database operation failed with entity context.
     *
     * @param message the error message (i18n key)
     * @param entityClass the JPA entity class affected
     * @param recordIds the IDs of affected records
     */
    public DatabaseException(String message, Class<?> entityClass, Long... recordIds) {
        super(message);
        this.recordIds = recordIds;
        this.entityClass = entityClass;
    }

    /**
     * Constructor with cause for wrapping other exceptions.
     *
     * @param message the error message (i18n key)
     * @param cause the underlying exception
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.recordIds = null;
        this.entityClass = null;
    }

    /**
     * Constructor with cause only.
     *
     * @param cause the underlying exception
     */
    public DatabaseException(Throwable cause) {
        super(cause);
        this.recordIds = null;
        this.entityClass = null;
    }
}
```

---

## 4. Entity Class Template

```java
package com.yourcompany.yourmodule.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * JPA entity for YourResource table.
 * Maps to 'your_resources' table in database.
 */
@Entity
@Table(name = "your_resources")
@Getter
@Setter
public class YourResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    private String status;  // Use enum if possible

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Relations (if needed)
    // @OneToMany, @ManyToOne, etc.

    @PrePersist
    public void onCreate() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = new Date();
    }
}
```

---

## 5. Domain Model Template

```java
package com.yourcompany.yourmodule.models;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * Domain model for YourResource.
 * NO JPA annotations, pure domain logic.
 * Used to pass data between services and facades.
 */
@Getter
@Setter
public class YourResource {

    private Long id;
    private String name;
    private String description;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private boolean active;

    // Business methods (no DB operations)
    public boolean isNew() {
        return this.id == null;
    }

    public void activateResource() {
        this.active = true;
    }

    public void deactivateResource() {
        this.active = false;
    }
}
```

---

## 6. DTOs Template

### Input DTO (for @RequestBody)
```java
package com.yourcompany.yourmodule.dtos.inputs;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

/**
 * Input DTO for creating/updating YourResource.
 * Includes validation annotations for Spring validation.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YourResourceInputDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "error.validation.name.required")
    @Size(min = 1, max = 255, message = "error.validation.name.size")
    private String name;

    @Size(max = 1000, message = "error.validation.description.size")
    private String description;

    @NotNull(message = "error.validation.status.required")
    private String status;

    private boolean active = true;
}
```

### Output DTO (for response body)
```java
package com.yourcompany.yourmodule.dtos.outputs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Output DTO for returning YourResource in API responses.
 * Includes only fields that should be exposed to clients.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YourResourceOutputDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private String status;
    private boolean active;
    private Date createdAt;
    private Date updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
```

### Wrapper Response DTOs
```java
package com.yourcompany.yourmodule.dtos.outputs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Single resource response wrapper.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YourResourceResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private YourResourceOutputDto resource;
    private String message;
    private long timestamp = System.currentTimeMillis();
}

/**
 * Multiple resources response wrapper.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllYourResourcesResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<YourResourceOutputDto> resources;
    private int totalCount;
    private String message;
    private long timestamp = System.currentTimeMillis();
}

/**
 * Error response wrapper.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String errorCode;
    private String message;
    private String messageAr;  // For i18n
    private long timestamp = System.currentTimeMillis();
}
```

---

## 7. Repository Template

```java
package com.yourcompany.yourmodule.repositories;

import com.yourcompany.yourmodule.entities.YourResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for YourResourceEntity.
 * Automatically generates SQL queries based on method names.
 */
@Repository
public interface YourResourceRepository extends JpaRepository<YourResourceEntity, Long> {

    /**
     * Find all active resources.
     */
    List<YourResourceEntity> findByActiveTrue();

    /**
     * Find by name (may have duplicates).
     */
    Optional<YourResourceEntity> findByName(String name);

    /**
     * Custom query example.
     */
    @Query("SELECT r FROM YourResourceEntity r WHERE r.status = :status AND r.active = true")
    List<YourResourceEntity> findActiveByStatus(@Param("status") String status);

    /**
     * Exists check (more efficient than findById).
     */
    boolean existsById(Long id);
}
```

---

## 8. Service Interface Template

```java
package com.yourcompany.yourmodule.services;

import com.yourcompany.yourmodule.models.YourResource;
import com.yourcompany.yourmodule.exceptions.ResourceNotFoundException;
import com.yourcompany.yourmodule.exceptions.DatabaseException;
import java.util.List;

/**
 * Service contract for YourResource business logic.
 * No implementation details — defines the abstraction.
 */
public interface YourResourceService {

    /**
     * Retrieve all active resources.
     *
     * @return list of resources
     * @throws DatabaseException if DB operation fails
     */
    List<YourResource> findAllActive() throws DatabaseException;

    /**
     * Retrieve a specific resource by ID.
     *
     * @param id the resource ID
     * @return the resource
     * @throws ResourceNotFoundException if not found
     * @throws DatabaseException if DB operation fails
     */
    YourResource findById(Long id) throws ResourceNotFoundException, DatabaseException;

    /**
     * Create a new resource.
     *
     * @param resource the resource to create
     * @return the created resource (with ID assigned)
     * @throws DatabaseException if creation fails
     */
    YourResource create(YourResource resource) throws DatabaseException;

    /**
     * Update an existing resource.
     *
     * @param resource the resource to update (must have ID)
     * @return the updated resource
     * @throws ResourceNotFoundException if not found
     * @throws DatabaseException if update fails
     */
    YourResource update(YourResource resource) throws ResourceNotFoundException, DatabaseException;

    /**
     * Delete a resource by ID.
     *
     * @param id the resource ID
     * @throws ResourceNotFoundException if not found
     * @throws DatabaseException if deletion fails
     */
    void delete(Long id) throws ResourceNotFoundException, DatabaseException;
}
```

---

## 9. Service Implementation Template

```java
package com.yourcompany.yourmodule.services.impl;

import com.yourcompany.yourmodule.entities.YourResourceEntity;
import com.yourcompany.yourmodule.models.YourResource;
import com.yourcompany.yourmodule.repositories.YourResourceRepository;
import com.yourcompany.yourmodule.services.YourResourceService;
import com.yourcompany.yourmodule.exceptions.ResourceNotFoundException;
import com.yourcompany.yourmodule.exceptions.DatabaseException;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.yourcompany.yourmodule.constants.YourModuleConstants.*;
import static com.apogee.common.mapper.ObjectMapper.transform;
import static com.apogee.common.mapper.ObjectMapper.transformCollection;

/**
 * Service implementation with @Transactional for automatic transaction management.
 * THIS is where business logic lives. NO HTTP awareness.
 */
@Service
@Transactional
@Log4j2
public class YourResourceServiceImpl implements YourResourceService {

    @Autowired
    private YourResourceRepository repository;

    @Override
    public List<YourResource> findAllActive() throws DatabaseException {
        try {
            log.info("Fetching all active resources");
            List<YourResourceEntity> entities = repository.findByActiveTrue();
            
            if (entities.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Use transformCollection for entity → model conversion
            List<YourResource> resources = transformCollection(
                entities,
                YourResource.class,
                this::enrichResource  // Optional callback for custom logic
            );
            
            log.info("Successfully fetched {} active resources", resources.size());
            return resources;
            
        } catch (Exception e) {
            log.error("Error fetching active resources", e);
            throw new DatabaseException(ERROR_DATABASE_FAILURE, e);
        }
    }

    @Override
    public YourResource findById(Long id) throws ResourceNotFoundException, DatabaseException {
        try {
            log.info("Fetching resource with ID: {}", id);
            Optional<YourResourceEntity> entity = repository.findById(id);
            
            if (entity.isEmpty()) {
                log.warn("Resource not found with ID: {}", id);
                throw new ResourceNotFoundException(ERROR_RESOURCE_NOT_FOUND, id);
            }
            
            YourResource resource = transform(entity.get(), YourResource.class, this::enrichResource);
            log.info("Successfully fetched resource with ID: {}", id);
            return resource;
            
        } catch (ResourceNotFoundException e) {
            throw e;  // Re-throw domain exceptions
        } catch (Exception e) {
            log.error("Error fetching resource with ID: {}", id, e);
            throw new DatabaseException(ERROR_DATABASE_FAILURE, YourResourceEntity.class, id);
        }
    }

    @Override
    public YourResource create(YourResource resource) throws DatabaseException {
        try {
            log.info("Creating new resource: {}", resource.getName());
            
            YourResourceEntity entity = transform(resource, YourResourceEntity.class);
            YourResourceEntity saved = repository.save(entity);
            
            YourResource result = transform(saved, YourResource.class, this::enrichResource);
            log.info("Successfully created resource with ID: {}", result.getId());
            return result;
            
        } catch (Exception e) {
            log.error("Error creating resource", e);
            throw new DatabaseException(ERROR_DATABASE_FAILURE, YourResourceEntity.class);
        }
    }

    @Override
    public YourResource update(YourResource resource) throws ResourceNotFoundException, DatabaseException {
        try {
            log.info("Updating resource with ID: {}", resource.getId());
            
            if (!repository.existsById(resource.getId())) {
                log.warn("Resource not found for update with ID: {}", resource.getId());
                throw new ResourceNotFoundException(ERROR_RESOURCE_NOT_FOUND, resource.getId());
            }
            
            YourResourceEntity entity = transform(resource, YourResourceEntity.class);
            YourResourceEntity updated = repository.save(entity);
            
            YourResource result = transform(updated, YourResource.class, this::enrichResource);
            log.info("Successfully updated resource with ID: {}", result.getId());
            return result;
            
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating resource with ID: {}", resource.getId(), e);
            throw new DatabaseException(ERROR_DATABASE_FAILURE, YourResourceEntity.class, resource.getId());
        }
    }

    @Override
    public void delete(Long id) throws ResourceNotFoundException, DatabaseException {
        try {
            log.info("Deleting resource with ID: {}", id);
            
            if (!repository.existsById(id)) {
                log.warn("Resource not found for deletion with ID: {}", id);
                throw new ResourceNotFoundException(ERROR_RESOURCE_NOT_FOUND, id);
            }
            
            repository.deleteById(id);
            log.info("Successfully deleted resource with ID: {}", id);
            
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting resource with ID: {}", id, e);
            throw new DatabaseException(ERROR_DATABASE_FAILURE, YourResourceEntity.class, id);
        }
    }

    /**
     * Callback method for custom mapping logic.
     * Called by transformCollection/transform when converting entity → model.
     */
    private YourResource enrichResource(YourResourceEntity entity, YourResource model) {
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setStatus(entity.getStatus());
        model.setActive(entity.isActive());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedBy(entity.getUpdatedBy());
        return model;
    }
}
```

---

## 10. Facade (Backing Service) Template

```java
package com.yourcompany.yourmodule.facade;

import com.yourcompany.yourmodule.dtos.inputs.YourResourceInputDto;
import com.yourcompany.yourmodule.dtos.outputs.YourResourceResponseDto;
import com.yourcompany.yourmodule.dtos.outputs.AllYourResourcesResponseDto;
import com.yourcompany.yourmodule.dtos.outputs.YourResourceOutputDto;
import com.yourcompany.yourmodule.models.YourResource;
import com.yourcompany.yourmodule.services.YourResourceService;
import com.yourcompany.yourmodule.exceptions.ResourceNotFoundException;
import com.yourcompany.yourmodule.exceptions.DatabaseException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.apogee.common.mapper.ObjectMapper.transform;
import static com.apogee.common.mapper.ObjectMapper.transformCollection;

/**
 * Facade (Backing Service) for YourResource API.
 * ONLY responsibility: adapt input DTOs → models, call service, adapt models → output DTOs.
 * NO business logic here — that's in the Service layer.
 * NO database access — use Service for that.
 */
@Service
@Log4j2
public class YourResourceFacade {

    @Autowired
    private YourResourceService service;

    /**
     * Get all active resources.
     * Transforms service response to output DTO.
     */
    public AllYourResourcesResponseDto getAllActive() throws DatabaseException {
        log.info("Facade: Getting all active resources");
        
        List<YourResource> models = service.findAllActive();
        List<YourResourceOutputDto> dtos = transformCollection(models, YourResourceOutputDto.class);
        
        AllYourResourcesResponseDto response = new AllYourResourcesResponseDto();
        response.setResources(dtos);
        response.setTotalCount(dtos.size());
        response.setMessage("success.resources.retrieved");
        
        return response;
    }

    /**
     * Get a single resource by ID.
     */
    public YourResourceResponseDto getById(Long id) throws ResourceNotFoundException, DatabaseException {
        log.info("Facade: Getting resource by ID: {}", id);
        
        YourResource model = service.findById(id);
        YourResourceOutputDto dto = transform(model, YourResourceOutputDto.class);
        
        YourResourceResponseDto response = new YourResourceResponseDto();
        response.setResource(dto);
        response.setMessage("success.resource.retrieved");
        
        return response;
    }

    /**
     * Create a new resource.
     * Adapts input DTO → model, calls service, returns output DTO.
     */
    public YourResourceResponseDto create(YourResourceInputDto inputDto) throws DatabaseException {
        log.info("Facade: Creating resource");
        
        // Convert input DTO to model
        YourResource model = transform(inputDto, YourResource.class);
        
        // Call service (business logic happens here)
        YourResource created = service.create(model);
        
        // Convert result model to output DTO
        YourResourceOutputDto outputDto = transform(created, YourResourceOutputDto.class);
        
        YourResourceResponseDto response = new YourResourceResponseDto();
        response.setResource(outputDto);
        response.setMessage("success.resource.created");
        
        return response;
    }

    /**
     * Update an existing resource.
     */
    public YourResourceResponseDto update(Long id, YourResourceInputDto inputDto) 
            throws ResourceNotFoundException, DatabaseException {
        log.info("Facade: Updating resource with ID: {}", id);
        
        YourResource model = transform(inputDto, YourResource.class);
        model.setId(id);  // Ensure ID is set
        
        YourResource updated = service.update(model);
        YourResourceOutputDto outputDto = transform(updated, YourResourceOutputDto.class);
        
        YourResourceResponseDto response = new YourResourceResponseDto();
        response.setResource(outputDto);
        response.setMessage("success.resource.updated");
        
        return response;
    }

    /**
     * Delete a resource.
     */
    public void delete(Long id) throws ResourceNotFoundException, DatabaseException {
        log.info("Facade: Deleting resource with ID: {}", id);
        service.delete(id);
    }
}
```

---

## 11. Controller Template

```java
package com.yourcompany.yourmodule.controller;

import com.yourcompany.yourmodule.dtos.inputs.YourResourceInputDto;
import com.yourcompany.yourmodule.dtos.outputs.YourResourceResponseDto;
import com.yourcompany.yourmodule.dtos.outputs.AllYourResourcesResponseDto;
import com.yourcompany.yourmodule.facade.YourResourceFacade;
import com.yourcompany.yourmodule.exceptions.ResourceNotFoundException;
import com.yourcompany.yourmodule.exceptions.DatabaseException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for YourResource API.
 * THIN layer: only HTTP handling, delegation to Facade, response assembly.
 * NO business logic — that's in Facade/Service.
 * NO database access — that's in Service.
 */
@RestController
@RequestMapping("/api/v1/resources")
@Tag(name = "YourResources", description = "API for managing your resources")
@Log4j2
public class YourResourceController {

    @Autowired
    private YourResourceFacade facade;

    /**
     * GET /api/v1/resources
     * Retrieve all active resources.
     */
    @GetMapping
    @Operation(
        summary = "Get all resources",
        description = "Retrieves all active resources in the system"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved resources",
        content = @Content(schema = @Schema(implementation = AllYourResourcesResponseDto.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<AllYourResourcesResponseDto> getAll() throws DatabaseException {
        log.info("GET /api/v1/resources - Fetching all resources");
        AllYourResourcesResponseDto response = facade.getAllActive();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * GET /api/v1/resources/{id}
     * Retrieve a specific resource by ID.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get resource by ID",
        description = "Retrieves a specific resource by its ID"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved resource",
        content = @Content(schema = @Schema(implementation = YourResourceResponseDto.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Resource not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<YourResourceResponseDto> getById(@PathVariable Long id) 
            throws ResourceNotFoundException, DatabaseException {
        log.info("GET /api/v1/resources/{} - Fetching resource", id);
        YourResourceResponseDto response = facade.getById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * POST /api/v1/resources
     * Create a new resource.
     */
    @PostMapping
    @Operation(
        summary = "Create resource",
        description = "Creates a new resource with the provided data"
    )
    @ApiResponse(
        responseCode = "201",
        description = "Resource created successfully",
        content = @Content(schema = @Schema(implementation = YourResourceResponseDto.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<YourResourceResponseDto> create(
            @Valid @RequestBody YourResourceInputDto inputDto) throws DatabaseException {
        log.info("POST /api/v1/resources - Creating new resource");
        YourResourceResponseDto response = facade.create(inputDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/resources/{id}
     * Update an existing resource.
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update resource",
        description = "Updates an existing resource by its ID"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Resource updated successfully",
        content = @Content(schema = @Schema(implementation = YourResourceResponseDto.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Resource not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<YourResourceResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody YourResourceInputDto inputDto)
            throws ResourceNotFoundException, DatabaseException {
        log.info("PUT /api/v1/resources/{} - Updating resource", id);
        YourResourceResponseDto response = facade.update(id, inputDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * DELETE /api/v1/resources/{id}
     * Delete a resource.
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete resource",
        description = "Deletes a resource by its ID"
    )
    @ApiResponse(
        responseCode = "204",
        description = "Resource deleted successfully"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Resource not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Void> delete(@PathVariable Long id)
            throws ResourceNotFoundException, DatabaseException {
        log.info("DELETE /api/v1/resources/{} - Deleting resource", id);
        facade.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
```

---

## 12. Test Template (JUnit 5 + Mockito)

```java
package com.yourcompany.yourmodule.services.impl;

import com.yourcompany.yourmodule.entities.YourResourceEntity;
import com.yourcompany.yourmodule.models.YourResource;
import com.yourcompany.yourmodule.repositories.YourResourceRepository;
import com.yourcompany.yourmodule.exceptions.ResourceNotFoundException;
import com.yourcompany.yourmodule.exceptions.DatabaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.yourcompany.yourmodule.constants.YourModuleConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for YourResourceServiceImpl.
 * NO Spring context — fast, isolated tests.
 * Uses Mockito to mock dependencies.
 */
@ExtendWith(MockitoExtension.class)
class YourResourceServiceImplTest {

    @Mock
    private YourResourceRepository repository;

    @InjectMocks
    private YourResourceServiceImpl service;

    @Test
    void findById_returnsResourceWhenExists() throws Exception {
        // Arrange
        Long resourceId = 1L;
        YourResourceEntity entity = new YourResourceEntity();
        entity.setId(resourceId);
        entity.setName("Test Resource");
        entity.setDescription("Test");
        entity.setStatus("ACTIVE");
        entity.setActive(true);

        when(repository.findById(resourceId)).thenReturn(Optional.of(entity));

        // Act
        YourResource result = service.findById(resourceId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(resourceId);
        assertThat(result.getName()).isEqualTo("Test Resource");
        assertThat(result.isActive()).isTrue();

        // Verify repository was called exactly once
        verify(repository, times(1)).findById(resourceId);
    }

    @Test
    void findById_throwsNotFoundWhenMissing() {
        // Arrange
        Long resourceId = 999L;
        when(repository.findById(resourceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.findById(resourceId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage(ERROR_RESOURCE_NOT_FOUND);

        verify(repository, times(1)).findById(resourceId);
    }

    @Test
    void create_savesAndReturnsResourceWithId() throws Exception {
        // Arrange
        YourResource inputModel = new YourResource();
        inputModel.setName("New Resource");
        inputModel.setDescription("New");
        inputModel.setStatus("ACTIVE");
        inputModel.setActive(true);

        YourResourceEntity savedEntity = new YourResourceEntity();
        savedEntity.setId(1L);
        savedEntity.setName("New Resource");
        savedEntity.setDescription("New");
        savedEntity.setStatus("ACTIVE");
        savedEntity.setActive(true);

        when(repository.save(any(YourResourceEntity.class))).thenReturn(savedEntity);

        // Act
        YourResource result = service.create(inputModel);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("New Resource");

        verify(repository, times(1)).save(any(YourResourceEntity.class));
    }

    @Test
    void update_updatesExistingResource() throws Exception {
        // Arrange
        Long resourceId = 1L;
        YourResource updateModel = new YourResource();
        updateModel.setId(resourceId);
        updateModel.setName("Updated Name");
        updateModel.setStatus("ACTIVE");

        when(repository.existsById(resourceId)).thenReturn(true);

        YourResourceEntity updatedEntity = new YourResourceEntity();
        updatedEntity.setId(resourceId);
        updatedEntity.setName("Updated Name");
        updatedEntity.setStatus("ACTIVE");

        when(repository.save(any(YourResourceEntity.class))).thenReturn(updatedEntity);

        // Act
        YourResource result = service.update(updateModel);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");

        verify(repository, times(1)).existsById(resourceId);
        verify(repository, times(1)).save(any(YourResourceEntity.class));
    }

    @Test
    void update_throwsNotFoundWhenIdDoesntExist() {
        // Arrange
        Long resourceId = 999L;
        YourResource updateModel = new YourResource();
        updateModel.setId(resourceId);

        when(repository.existsById(resourceId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> service.update(updateModel))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage(ERROR_RESOURCE_NOT_FOUND);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_deletesExistingResource() throws Exception {
        // Arrange
        Long resourceId = 1L;
        when(repository.existsById(resourceId)).thenReturn(true);

        // Act
        service.delete(resourceId);

        // Assert
        verify(repository, times(1)).existsById(resourceId);
        verify(repository, times(1)).deleteById(resourceId);
    }

    @Test
    void delete_throwsNotFoundWhenIdDoesntExist() {
        // Arrange
        Long resourceId = 999L;
        when(repository.existsById(resourceId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> service.delete(resourceId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage(ERROR_RESOURCE_NOT_FOUND);

        verify(repository, never()).deleteById(any());
    }

    @Test
    void findAllActive_returnsEmptyListWhenNone() throws Exception {
        // Arrange
        when(repository.findByActiveTrue()).thenReturn(Collections.emptyList());

        // Act
        List<YourResource> result = service.findAllActive();

        // Assert
        assertThat(result).isEmpty();
        verify(repository, times(1)).findByActiveTrue();
    }

    @Test
    void findAllActive_returnsAllActiveResources() throws Exception {
        // Arrange
        YourResourceEntity entity1 = new YourResourceEntity();
        entity1.setId(1L);
        entity1.setName("Resource 1");
        entity1.setActive(true);

        YourResourceEntity entity2 = new YourResourceEntity();
        entity2.setId(2L);
        entity2.setName("Resource 2");
        entity2.setActive(true);

        when(repository.findByActiveTrue()).thenReturn(List.of(entity1, entity2));

        // Act
        List<YourResource> result = service.findAllActive();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Resource 1");
        assertThat(result.get(1).getName()).isEqualTo("Resource 2");

        verify(repository, times(1)).findByActiveTrue();
    }
}
```

---

## 13. Configuration Templates

### OpenAPI Configuration
```java
package com.yourcompany.yourmodule.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.yourcompany.yourmodule.configs.OpenApiConstants.*;

/**
 * OpenAPI 3.0 configuration for Swagger UI documentation.
 * Defines API info, security schemes, and shared response schemas.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        Components components = new Components()
            .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme(SCHEME_BEARER)
                .bearerFormat(BEARER_FORMAT))
            .addSchemas(SCHEMA_ERROR_RESPONSE, new Schema<>()
                .type("object")
                .addProperty("errorCode", new Schema<>().type("string"))
                .addProperty("message", new Schema<>().type("string"))
                .addProperty("messageAr", new Schema<>().type("string"))
                .addProperty("timestamp", new Schema<>().type("integer")))
            .addResponses(RESPONSE_ERROR, new ApiResponse()
                .description("Error response")
                .content(new Content()
                    .addMediaType(MEDIA_TYPE_JSON,
                        new MediaType()
                            .schema(new Schema<>().$ref(REF_ERROR_RESPONSE)))));

        return new OpenAPI()
            .addServersItem(new Server().url(SERVER_ROOT))
            .info(new Info()
                .title(API_TITLE)
                .version(API_VERSION)
                .description(API_DESCRIPTION))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
            .components(components);
    }
}
```

### OpenAPI Constants
```java
package com.yourcompany.yourmodule.configs;

/**
 * Constants for OpenAPI configuration.
 * Non-instantiable class.
 */
public final class OpenApiConstants {

    private OpenApiConstants() {
        throw new AssertionError("Cannot instantiate OpenApiConstants");
    }

    // API Info
    public static final String API_TITLE = "YourModule API";
    public static final String API_VERSION = "1.0.0";
    public static final String API_DESCRIPTION = "API for managing resources in YourModule";

    // Security
    public static final String BEARER_AUTH = "bearerAuth";
    public static final String SCHEME_BEARER = "bearer";
    public static final String BEARER_FORMAT = "JWT";

    // Servers
    public static final String SERVER_ROOT = "/";

    // Schemas
    public static final String SCHEMA_ERROR_RESPONSE = "ErrorResponse";
    public static final String REF_ERROR_RESPONSE = "#/components/schemas/ErrorResponse";

    // Responses
    public static final String RESPONSE_ERROR = "ErrorResponse";

    // Media Types
    public static final String MEDIA_TYPE_JSON = "application/json";
}
```

---

## 14. Correlation Filter Template

```java
package com.yourcompany.yourmodule.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

import static com.yourcompany.yourmodule.constants.YourModuleConstants.*;

/**
 * Filter to add correlation ID to every HTTP request.
 * Sets up MDC (Mapped Diagnostic Context) for request tracking.
 * Executed once per request.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Generate unique request ID
        String requestId = UUID.randomUUID().toString();

        // Place in MDC for logging (appears in all logs for this request)
        MDC.put(REQUEST_ID, requestId);

        // Add to response headers
        response.setHeader(X_REQUEST_ID, requestId);

        try {
            // Continue filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Always clean up MDC
            MDC.clear();
        }
    }
}
```

---

## 15. Token-Saving Tips & Patterns

### ✅ DO THIS

1. **Centralize all strings in Constants:** Don't scatter error messages in 20 files. Agents search one file.
   ```java
   // Good: One source of truth
   public static final String ERROR_NOT_FOUND = "error.not.found";
   ```

2. **Use consistent naming:** `*ServiceImpl`, `*Facade`, `*Repository`, `*Entity`, `*Dto`, `*OutputDto`
   ```
   // Agent can predict file locations with 100% accuracy
   ProductServiceImpl → src/main/java/.../services/impl/ProductServiceImpl.java
   ```

3. **Copy the exact structure:** Every new module = same folders and file patterns.
   - Reduces agent's "exploration" time
   - Consistency = predictability = fewer tokens

4. **Minimal DTO nesting:** Keep DTOs flat when possible.
   ```java
   // Saves mapper tokens — fewer fields to debug
   public class UserDto {
       private Long id;
       private String name;
       private String email;
   }
   ```

5. **Comment private methods:** Especially callback functions in services.
   ```java
   /**
    * Callback for enriching User model with related entities.
    */
   private User enrichUser(UserEntity entity, User model) { /*...*/ }
   ```

6. **Use @Log4j2 annotation:** Eliminates boilerplate, cleaner for agents to read.
   ```java
   @Log4j2
   public class MyService {
       // log object is automatically injected
       log.info("message");
   }
   ```

7. **Throw specific exceptions only:** 2 types: `ResourceNotFoundException`, `DatabaseException`.
   ```java
   // Bad: too many exception types
   throw new ItemNotFoundException();  // Unique exception class
   throw new CustomError();            // Another unique class
   
   // Good: standard exceptions
   throw new ResourceNotFoundException(ERROR_KEY, id);
   throw new DatabaseException(ERROR_KEY, EntityClass);
   ```

8. **Lombok on all entities:**
   ```java
   @Entity
   @Getter  // Agent doesn't need to read getter implementations
   @Setter
   public class MyEntity { /*...*/ }
   ```

9. **@Transactional on Service implementation class (not interface):**
   ```java
   // ✅ Correct
   @Service
   @Transactional
   public class MyServiceImpl implements MyService {
   
   // ❌ Avoid
   @Service
   public interface MyService {
       @Transactional
       void save();
   }
   ```

10. **OpenAPI annotations in Controller, not Service:**
    ```java
    // ✅ Controller only
    @GetMapping
    @Operation(summary = "...")
    @ApiResponse(...)
    public ResponseEntity<Dto> get(@PathVariable Long id) { /*...*/ }
    
    // ❌ Service doesn't have HTTP annotations
    ```

### ❌ DON'T DO THIS

1. **Multiple mapping libraries:** Use ONLY the custom Mapper for ALL conversions.
   - Agents spend tokens understanding which mapper to use
   
2. **Business logic in Controller:** That confuses layer separation.
   - Agents waste tokens refactoring between layers

3. **Custom exception types per domain:** Stick to 2: `ResourceNotFoundException`, `DatabaseException`.
   - Too many types = agent spends tokens choosing the right one

4. **Scattered error messages:** Don't hardcode `"Product not found"` in 15 files.
   - Agents search every file to find all error messages

5. **Deep DTO nesting:** Don't create `CompanyDto` containing `DepartmentDto` containing `EmployeeDto`.
   - Flat DTOs = fewer mapper tokens

6. **Optional in entity fields:** Use nullable columns instead.
   - `Optional` is overkill in JPA entities

7. **Multiple test patterns:** Use JUnit 5 + Mockito for all. No TestContextLoader, no embedded DB.
   - Consistency = predictability

8. **Comments on obvious code:**
   ```java
   // ❌ Wastes space
   // Set the ID
   model.setId(entity.getId());
   
   // ✅ Only comment WHY, not WHAT
   // Set ID for REST API response (required by client v1.2)
   model.setId(entity.getId());
   ```

---

## 16. Checklist for Adding New REST Endpoint

```markdown
## New Endpoint Checklist

### 1. Database & Domain Layer
- [ ] Create `*Entity` class (JPA @Entity with @Column)
- [ ] Create `*Repository` interface (extends JpaRepository, add custom queries if needed)
- [ ] Create `*Model` class (no annotations, pure domain)
- [ ] Add constants to `YourModuleConstants.java`
- [ ] Create/update database migration (Liquibase/Flyway)

### 2. Service Layer
- [ ] Create `*Service` interface (contract with javadoc)
- [ ] Create `*ServiceImpl` class (@Service, @Transactional, logging)
- [ ] Implement all CRUD methods (or needed subset)
- [ ] Throw only ResourceNotFoundException / DatabaseException
- [ ] Add callback methods for entity→model enrichment if needed
- [ ] Unit test with JUnit 5 + Mockito (NO Spring context)

### 3. Facade Layer
- [ ] Create `*Facade` class (@Service, @Autowired *Service)
- [ ] Add DTO→model conversions using Mapper.transform()
- [ ] Add model→DTO conversions using Mapper.transform()
- [ ] Delegate to service (no business logic)
- [ ] Wrap responses in Response DTOs

### 4. Controller Layer
- [ ] Create `*Controller` (@RestController, @RequestMapping)
- [ ] Add @GetMapping, @PostMapping, @PutMapping, @DeleteMapping
- [ ] Add @Operation, @ApiResponse, @Schema OpenAPI annotations
- [ ] Add @PathVariable, @RequestBody, @RequestParam annotations (for logging)
- [ ] Keep methods thin (delegate to Facade)
- [ ] Return ResponseEntity with appropriate status codes

### 5. DTOs & Models
- [ ] Create `*InputDto` for @RequestBody (add @Valid, @NotNull)
- [ ] Create `*OutputDto` for response body (@JsonInclude)
- [ ] Create response wrapper DTOs (`*ResponseDto`, `All*ResponseDto`)
- [ ] Ensure field names match between Dto↔Model↔Entity for Mapper

### 6. Error Handling
- [ ] Add error message keys to `YourModuleConstants`
- [ ] Add translations: `errors_en.properties`, `errors_ar.properties`
- [ ] Verify GlobalExceptionHandler catches exceptions and returns localized FailureResponse
- [ ] Test error scenarios (not found, DB failure, validation)

### 7. OpenAPI Documentation
- [ ] Verify OpenAPI configuration in `configs/OpenApiConfig.java`
- [ ] Check Swagger UI at http://localhost:8080/swagger-ui.html
- [ ] Verify all endpoints are documented
- [ ] Verify error schemas are properly referenced

### 8. Testing
- [ ] Unit test for Service layer (Mockito, no context)
- [ ] OPTIONAL: Integration test for Controller (MockMvc)
- [ ] OPTIONAL: E2E test (if critical endpoint)
- [ ] Test both success and failure scenarios

### 9. Documentation
- [ ] Add JavaDoc to Service interface and public methods
- [ ] Add JavaDoc to Controller methods
- [ ] Update API documentation / README
- [ ] Add examples for common operations
```

---

## Summary: What This Guide Provides

✅ **Complete, copy-paste-ready templates** for all layers
✅ **File structure** that prevents agent confusion
✅ **Constants pattern** to centralize strings (saves tokens)
✅ **Exception handling** with i18n support (standard pattern)
✅ **Mapper usage** with callback examples (reduces mapping complexity)
✅ **OpenAPI examples** for Swagger documentation
✅ **Test templates** using JUnit 5 + Mockito
✅ **Token-saving tips** to keep agent context efficient

**Usage:** For each new endpoint, copy the appropriate template, replace `YourResource` with your entity name, and modify as needed. By following this structure, AI agents can:
- Find files predictably (no exploration needed)
- Understand patterns at a glance (no deviation confusion)
- Make changes consistently (same approach everywhere)
- Run efficiently (fewer tokens on search/analysis)

**Final Recommendation:** Save this document and use it as the canonical reference when onboarding new AI agents to your project.

