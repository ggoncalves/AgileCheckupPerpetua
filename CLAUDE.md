# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AgileCheckupPerpetua is a Java application that uses AWS DynamoDB for data persistence. It implements a system for agile assessment management with entities like Companies, Departments, Teams, and Assessment Matrices.

The architecture follows a layered approach:
- Entity classes (in `com.agilecheckup.persistency.entity`)
- Repository classes (in `com.agilecheckup.persistency.repository`)
- Service classes (in `com.agilecheckup.service`)
- Runner classes (in `com.agilecheckup.main.runner`)

The application uses Dagger for dependency injection and Lombok for reducing boilerplate code.

## Build Commands

### Building the Project
```bash
mvn clean install
```

### Running the Application
```bash
mvn exec:java
```

### Running Tests
```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=CompanyServiceTest

# Run a specific test method
mvn test -Dtest=CompanyServiceTest#create
```

## Architecture Details

### Entity Structure
The application uses a hierarchy of entity classes:
- `BaseEntity`: Provides common ID functionality for all entities
- `AuditableEntity`: Adds audit fields (created/updated timestamps)
- `DescribableEntity`: Adds name and description fields
- `TenantableEntity`: Adds multi-tenant support

### Repository Pattern
- Each entity has a corresponding repository for CRUD operations
- `AbstractCrudRepository` provides common CRUD functionality
- All repositories interact with DynamoDB using the AWS SDK

### Service Layer
- Services implement business logic and interact with repositories
- `AbstractCrudService` provides common CRUD service functionality
- Services handle validation, relationships, and business rules

### Runners
- The application uses runners to execute operations on tables
- `AbstractCrudRunner` defines the template for CRUD operations
- Entity-specific runners implement the actual operations
- `E2EAllTablesInvoker` orchestrates all runners

### Recent Feature Development
- Added update functionality to services
- Added tenant ID filtering capabilities
- Implemented global secondary indexes for tenant-based queries

## Key Classes and Responsibilities

1. `AbstractCrudService`: Base service providing CRUD operations
2. `AbstractCrudRepository`: Base repository for DynamoDB operations
3. `BaseEntity`: Base entity class with ID handling
4. `TenantableEntity`: Entity with multi-tenant support
5. `E2EAllTablesInvoker`: Main orchestrator for running operations

## Testing Approach

The project uses JUnit 5 for testing with Mockito for mocking:
- `AbstractCrudServiceTest`: Base test class for services
- Entity-specific service tests extend from the abstract test
- `TestObjectFactory`: Utility for creating test objects