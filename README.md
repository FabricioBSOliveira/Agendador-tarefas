# 📅 Agendador-Tarefas — Task Scheduling Microservice

> A **Task Scheduling** microservice built with **Java 17** and **Spring Boot 4**, using **MongoDB** as its document store and communicating with peer services via **OpenFeign**. Part of a **multi-service distributed system** alongside [Usuario](https://github.com/FabricioBSOliveira/Usuario), [notificacao](https://github.com/FabricioBSOliveira/notificacao), and [bff-agendador-tarefas](https://github.com/FabricioBSOliveira/bff-agendador-tarefas).

---

## 🚀 What This Service Does

This is the **core domain service** of the task scheduling platform. It is responsible for:

- Creating, reading, updating, and deleting tasks for authenticated users
- Associating tasks with the users registered in the `Usuario` service via inter-service HTTP calls
- Validating JWT tokens issued by `Usuario` to authorize every request
- Persisting tasks in **MongoDB**, leveraging its flexible document model for scheduling data
- Mapping between domain entities and DTOs via **MapStruct** — compile-time safe, zero reflection overhead

---

## 🏗️ Architecture & Design Decisions

### Why MongoDB Here?

`Usuario` uses **PostgreSQL** — a relational database — because user data is highly structured and benefits from strict schema enforcement and ACID transactions.

`Agendador-Tarefas` uses **MongoDB** — a document database — because task data is naturally hierarchical and flexible: tasks may have varying fields, nested metadata, or evolve in structure without requiring schema migrations. This is a deliberate, intentional architectural choice to demonstrate **polyglot persistence** — a hallmark of mature microservice systems where each service owns its data store and picks the right tool for its domain.

```
┌────────────────────────────────────────────────┐
│               REST Controller Layer             │  ← HTTP, input validation
├────────────────────────────────────────────────┤
│                 Service Layer                   │  ← Business logic, orchestration
├────────────────────────────────────────────────┤
│              MapStruct DTO Mapping              │  ← Compile-time entity ↔ DTO conversion
├────────────────────────────────────────────────┤
│          Repository Layer (Spring Data)         │  ← MongoDB data access abstraction
├────────────────────────────────────────────────┤
│                   MongoDB                       │  ← Document-oriented data storage
└────────────────────────────────────────────────┘
```

### JWT Token Propagation

This service does **not issue tokens** — that's the responsibility of `Usuario`. Instead, it validates incoming JWTs on every request using the same JJWT library and secret, ensuring that only authenticated users can manage their tasks. This separation of concerns is the correct pattern for microservice auth: one issuer, many validators.

---

## ⚙️ Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Language | Java 17 | LTS release with modern language features |
| Framework | Spring Boot 4 | Production-grade application framework |
| Security | Spring Security + JJWT 0.12 | JWT validation and endpoint authorization |
| Persistence | Spring Data MongoDB | Document-oriented data access with repository abstraction |
| DTO Mapping | MapStruct 1.5 | Compile-time, type-safe entity ↔ DTO conversion |
| HTTP Client | Spring Cloud OpenFeign | Declarative inter-service communication (calls `Usuario`) |
| Build Tool | Gradle 8 | Dependency management and build pipeline |
| Boilerplate Reduction | Lombok | Eliminates repetitive getter/setter/builder code |
| Containerization | Docker | Reproducible, portable deployment |
| CI/CD | GitHub Actions | Automated build and test pipeline on every push |

---

## 🔍 MapStruct — Why It Matters

Most tutorials use manual converters or ModelMapper (reflection-based). This service uses **MapStruct**, which generates mapping code at **compile time**. The result:

- No runtime reflection → zero performance penalty
- Mapping errors caught at build time, not in production
- Clean separation between API contracts (DTOs) and internal domain models

```java
// MapStruct generates this implementation automatically at compile time
@Mapper(componentModel = "spring")
public interface TarefaMapper {
    TarefaResponseDTO toDTO(Tarefa tarefa);
    Tarefa toEntity(TarefaRequestDTO dto);
}
```

This is the pattern used in production Java codebases at scale.

---

## 🔗 Inter-Service Communication

This service calls `Usuario` via an **OpenFeign** client to validate user existence before associating tasks. OpenFeign turns HTTP calls into simple Java interface declarations:

```java
@FeignClient(name = "usuario", url = "${usuario.service.url}")
public interface UsuarioClient {
    @GetMapping("/usuario/{id}")
    UsuarioResponseDTO buscarUsuarioPorId(@PathVariable Long id,
                                          @RequestHeader("Authorization") String token);
}
```

No `RestTemplate` boilerplate, no manual HTTP wiring — just a typed, testable interface. This is standard practice in Spring Cloud microservice architectures.

---

## 🐳 Running with Docker

The service is fully containerized using a **multi-stage Docker build** to keep the final image minimal and production-ready.

```dockerfile
# Stage 1: Full build environment
FROM gradle:8.14-jdk17 AS build
RUN gradle build --no-daemon

# Stage 2: Lean runtime image only
FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /app/build/libs/*.jar /app/agendador-tarefas.jar
EXPOSE 8081
```

**Build and run:**

```bash
git clone https://github.com/FabricioBSOliveira/Agendador-tarefas.git
cd Agendador-tarefas

docker build -t agendador-tarefas .
docker run -p 8081:8081 \
  -e SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/db_tarefas \
  -e USUARIO_SERVICE_URL=http://localhost:8080 \
  agendador-tarefas
```

> For the full platform (all 4 services), see the [bff-agendador-tarefas](https://github.com/FabricioBSOliveira/bff-agendador-tarefas) repository.

---

## 🛠️ Running Locally Without Docker

**Prerequisites:** Java 17, MongoDB (local or Atlas), `Usuario` service running on port 8080.

```bash
# Configure environment variables or application.properties
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/db_tarefas
USUARIO_SERVICE_URL=http://localhost:8080
JWT_SECRET=<same secret used in the Usuario service>

# Build and run
./gradlew bootRun
```

The service starts on **port 8081**.

---

## 📡 Key API Endpoints

All endpoints require a valid `Authorization: Bearer <token>` header issued by the `Usuario` service.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/tarefas` | Create a new task for the authenticated user |
| `GET` | `/tarefas/{id}` | Retrieve a task by ID |
| `GET` | `/tarefas/usuario/{usuarioId}` | List all tasks for a given user |
| `PUT` | `/tarefas/{id}` | Update an existing task |
| `DELETE` | `/tarefas/{id}` | Delete a task |

---

## 🧪 Testing

- **JUnit 5** + To be implemented
- **Spring Security Test** for JWT authorization assertions in controller tests
- **Embedded MongoDB** (`spring-boot-starter-data-mongodb-test`) for repository tests without a real database instance — tests are fully self-contained and run in CI without external dependencies

---

## 📦 Project Structure

```
src/
└── main/
    └── java/com/Fabricio/
        ├── controller/     # REST endpoints
        ├── service/        # Business logic and orchestration
        ├── repository/     # Spring Data MongoDB repositories
        ├── model/          # Domain documents (MongoDB entities)
        ├── dto/            # Request/Response DTOs
        ├── mapper/         # MapStruct mapping interfaces
        ├── client/         # OpenFeign clients (calls Usuario)
        └── security/       # JWT validation filter and config
```

---

## 🌐 Platform Context

This service is one of four microservices in a distributed task scheduling platform:

```
[bff-agendador-tarefas]  ← API Gateway / BFF — single entry point for clients
        │
        ├── [Usuario]              ← Identity & Auth (PostgreSQL)
        ├── [Agendador-tarefas]    ← Task scheduling logic (MongoDB) ← YOU ARE HERE
        └── [notificacao]          ← Notification dispatch
```

The deliberate use of **two different databases** across services (PostgreSQL in `Usuario`, MongoDB here) demonstrates an understanding of polyglot persistence — picking the right data store for the domain rather than forcing every service into the same technology.

---

## 👨‍💻 About the Author
Fabricio Butti Santos de Oliveira — a career-switching Mechanical Engineer who chose to apply the same systems constrains, critical thinking and problem solving to distributed software.

[![GitHub](https://img.shields.io/badge/GitHub-FabricioBSOliveira-181717?style=flat&logo=github)](https://github.com/FabricioBSOliveira)
