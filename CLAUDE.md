# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

AgileCheckup is a **multi-module Java application with React frontend** for agile assessment management following microservices architecture:

Please refeer to the name as I called as follows:

Gate -> /Users/ggoncalves/dev/AgileCheckup/AgileCheckupGate
perpetua -> /Users/ggoncalves/dev/AgileCheckup/AgileCheckupPerpetua
UI (also frontend) -> /Users/ggoncalves/dev/AgileCheckup/AgileCheckupUI

### Directory and Commit Awareness
- UI, Gate and Perpetua project are Subdirectories of the current AgileCheckup dir. Make sure you know in which directory you are when commiting and making changes

### Git

#### Commits

Also add a prefix to each commit. If I dont tell you in the prompt, please ask me. Eg. Prefix is AC-10. So every branch should be named according to this prefix, like: feature/AC-10-create-ui. Remember to use all caps. Each commit text should start with "AC-10" as well.

In the commit, please add the Claude model responsible to implementing the code.

### Code

For coding, alongside all the preferences in this file, I will prefer as much as possible:

- Use Clean Code always
- Use SOLID principles
- Use AssertJ in unit testing
- Avoid mocking private and static methods. Prefer, instead, changes and refactors in the SUT

### Commit Guidelines
- Do not commit unit I explicitly ask you to do so
- Before commit check for Java Warnings and Unused Imports and run all the tests

### Module Structure
- **AgileCheckupPerpetua**: Core domain layer with business logic, entities, and AWS DynamoDB integration
- **AgileCheckupGate**: API Gateway layer with AWS Lambda handlers for REST endpoints  
- **AgileCheckupUI**: Next.js frontend with AdminLTE interface

### Key Technologies
- **Backend**: Java 11, Maven, AWS DynamoDB, AWS Lambda, Dagger 2 DI, Jackson JSON
- **Frontend**: Next.js 15, React 19, TypeScript, Bootstrap 4, AdminLTE
- **Testing**: JUnit 5, Mockito, AssertJ

## Common Commands

### Backend Development
```bash
# Core module (Perpetua)
cd AgileCheckupPerpetua
mvn clean install        # Build and install to local repository
mvn exec:java           # Run main application (E2E table operations)
mvn test                # Run all tests
mvn test -Dtest=ClassName # Run specific test class

# API Gateway module (Gate)  
cd AgileCheckupGate
mvn clean package       # Build Lambda deployment JAR
mvn test                # Run API handler tests
```

### Frontend Development
```bash
cd AgileCheckupUI
npm install             # Install dependencies
npm run dev             # Development server with hot reload (port 3000)
npm run build           # Production build
npm run start           # Start production server
npm run lint            # ESLint code checking
```

## Architecture Patterns

### Multi-Tenant Design
- Companies serve as tenants with automatic tenant ID filtering
- All entities implement `Tenantable` interface for data isolation
- UI requires company selection before feature access via `TenantContext`

### Domain-Driven Entities
Entity inheritance hierarchy: `BaseEntity` → `AuditableEntity` → `DescribableEntity` → `TenantableEntity`

Core domain objects:
- **Company**: Tenant root entity
- **Department/Team**: Organizational structure  
- **PerformanceCycle**: Assessment periods
- **AssessmentMatrix**: Question templates
- **Question/Answer**: Complex strategy pattern supporting multiple answer types (Yes/No, 1-10 scale, custom options)
- **EmployeeAssessment**: Individual assessment instances with scoring

### Layered Architecture Flow
1. **UI Components** (`AbstractCRUD.tsx`) → 
2. **API Service** (`apiService.ts`) →
3. **Lambda Handlers** (`ApiGatewayHandler.java`) →
4. **Business Services** (`AbstractCrudService`) →
5. **Repository Layer** (`AbstractCrudRepository`) →
6. **DynamoDB Entities**

### Dependency Injection
- **Backend**: Dagger 2 with `ServiceComponent`, `RepositoryComponent`, `AwsConfigComponent`
- **Frontend**: React Context API for tenant management and state

## Development Workflow

### Adding New Entities
1. Create entity in `AgileCheckupPerpetua/src/main/java/com/agilecheckup/persistency/entity/`
2. Extend `TenantableEntity` for multi-tenant support
3. Add repository extending `AbstractCrudRepository`
4. Add service extending `AbstractCrudService` 
5. Add request handler extending base handler in `AgileCheckupGate`
6. Update `RequestHandlerStrategy` for routing
7. Add UI component using `AbstractCRUD` pattern

### Testing Strategy
- **Unit Tests**: Mock dependencies using Mockito
- **Repository Tests**: Use `AbstractRepositoryTest` base class
- **Service Tests**: Use `AbstractCrudServiceTest` base class
- **Test Data**: Utilize `TestObjectFactory` for consistent test objects

#### Testing Best Practices
- **DO NOT create separate test classes for new methods** - Always add tests to existing test classes
- When adding new methods to a service, add the corresponding tests to the existing service test class
- Follow the existing test patterns and naming conventions in the codebase
- Example: When adding methods to `EmployeeAssessmentService`, add tests to `EmployeeAssessmentServiceTest`, not a new test class

#### Mockito Best Practices
**CRITICAL**: Follow these Mockito patterns to avoid stubbing issues:

1. **Use `doReturn().when()` instead of `when().thenReturn()` for complex stubbing:**
   ```java
   // GOOD - Avoids strict stubbing conflicts
   doReturn(Optional.of(entity))
       .when(service).create(anyString(), anyString(), any(LocalDateTime.class));
   
   // AVOID - Can cause PotentialStubbingProblem
   when(service.create(anyString(), anyString(), any(LocalDateTime.class)))
       .thenReturn(Optional.of(entity));
   ```

2. **Handle nullable parameters correctly:**
   ```java
   // GOOD - For parameters that can be null
   doReturn(result).when(service).method(anyString(), nullable(String.class));
   
   // GOOD - Alternative for any nullable parameter
   doReturn(result).when(service).method(anyString(), any());
   
   // AVOID - anyString() doesn't match null values
   doReturn(result).when(service).method(anyString(), anyString());
   ```

3. **Use lenient() for setup mocks that might not be called in all tests:**
   ```java
   @BeforeEach
   void setUp() {
       lenient().doReturn(mockService).when(serviceComponent).buildService();
   }
   ```

4. **Ensure argument matchers are consistent:**
   ```java
   // GOOD - All matchers or all exact values
   verify(service).create(eq("exact"), anyString(), any(LocalDateTime.class));
   
   // AVOID - Mixing exact values with matchers
   verify(service).create("exact", anyString(), any(LocalDateTime.class));
   ```

**Common Error**: `PotentialStubbingProblem` occurs when:
- Stubbed method arguments don't match actual invocation arguments
- Using `anyString()` when the actual parameter is `null` 
- Mixing exact values with argument matchers in verification

### Key Configuration Files
- `AgileCheckupUI/next.config.ts`: API proxy to AWS Gateway
- `AgileCheckupPerpetua/src/main/resources/application.properties`: AWS configuration
- Individual module `pom.xml` files for dependencies and build configuration

## Module Dependencies
AgileCheckupGate depends on AgileCheckupPerpetua, but both are independent of AgileCheckupUI. The frontend communicates via REST API only.

## UI Development Notes
- Please be aware that you also have other pages to other concepts that will can follow to create the UI, such as Company, Department, Assessment Matrices, Performance Cycle etc