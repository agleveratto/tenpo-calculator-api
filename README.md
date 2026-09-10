# Tenpo Backend Challenge - Calculator API (Spring Boot & Arquitectura Hexagonal)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED.svg)](https://www.docker.com/)

API REST desarrollada en **Spring Boot 3** y **Java 21** para el desafío técnico de Tenpo. El proyecto implementa una **Arquitectura Hexagonal (Puertos y Adaptadores)** estricta, asegurando un desacoplamiento total entre el dominio de negocio, la lógica de aplicación y los detalles de infraestructura.

---

## 🛠️ Tecnologías y Stack Utilizado

* **Lenguaje:** Java 21 (LTS)
* **Framework:** Spring Boot 3.2.3 (Spring MVC, Spring Data JPA)
* **Arquitectura:** Arquitectura Hexagonal (Ports & Adapters / Clean Architecture)
* **Base de Datos:** PostgreSQL 15
* **Resiliencia:** Resilience4j 2.2.0 (mecanismo de reintentos con backoff exponencial)
* **Rate Limiting:** Control de concurrencia y tasa de peticiones con semáforos / ventana deslizante (máximo 3 RPM por endpoint)
* **Auditoría Asíncrona:** Publicación y persistencia de logs en segundo plano (`@Async` con `ThreadPoolTaskExecutor`)
* **Documentación:** SpringDoc OpenAPI 2.3.0 (Swagger UI)
* **Contenedores:** Docker & Docker Compose
* **Testing:** JUnit 5, Mockito, Spring Boot Test

---

## 📐 Arquitectura Hexagonal

La estructura del código sigue el patrón de Puertos y Adaptadores, garantizando que las reglas del negocio no dependan de frameworks ni librerías externas:

```text
com.tenpo.calculator
├── domain                      # Núcleo de Negocio (Puro, sin dependencias de frameworks)
│    ├── limiters               # Interfaces y lógica de dominio para limitadores (RpmLimiter)
│    ├── model                  # Modelos y Records de Dominio (CalculationResult, ApiLog)
│    └── port                   # Puertos del Dominio
│         ├── in                # Casos de Uso / Puertos de Entrada (CalculateUseCase, GetHistoryUseCase)
│         └── out               # Puertos de Salida (DynamicPercentagePort, ApiLogRepositoryPort)
│
├── application                 # Capa de Aplicación / Servicios
│    └── service                # Implementación de Casos de Uso (CalculateService, GetHistoryService)
│
└── infrastructure              # Capa de Infraestructura (Adaptadores y Frameworks)
     ├── adapter
     │    ├── in                # Adaptadores de Entrada (REST Controllers, Interceptores, Handlers)
     │    │    └── web
     │    │         ├── exception       # GlobalExceptionHandler, RateLimitExceededException
     │    │         └── interceptor     # RateLimitInterceptor, SemaphoreRpmLimiter
     │    └── out               # Adaptadores de Salida
     │         ├── async        # Publicación asíncrona de eventos de auditoría (AsyncLogPublisher)
     │         ├── external     # Cliente mock para porcentaje externo con reintentos (PercentageMockAdapter)
     │         └── persistence  # Persistencia JPA con PostgreSQL (ApiLogJpaAdapter, SpringDataJpaLogRepository)
     └── config                 # Configuración de Spring (WebConfig, OpenAPI, Async)
```

### 💡 Decisiones de Diseño y Arquitectura

1. **Arquitectura Hexagonal:** El dominio y los casos de uso están completamente aislados de Spring Boot y JPA. Los puertos (`CalculateUseCase`, `DynamicPercentagePort`, `ApiLogRepositoryPort`) definen los contratos sin acoplamiento técnico.
2. **Auditoría Asíncrona:** El registro de cada llamada a los endpoints se realiza de manera no bloqueante mediante un publicador asíncrono (`AsyncLogPublisher`), evitando penalizar la latencia de la respuesta al cliente.
3. **Resiliencia con Resilience4j:** El adaptador del servicio externo de porcentaje (`PercentageMockAdapter`) utiliza `@Retry(name = "percentageServiceRetry")` con un máximo de 3 intentos y backoff exponencial configurable en `application.yml`.
4. **Rate Limiting (3 RPM):** Implementado a nivel de adaptador web (`RateLimitInterceptor` con `SemaphoreRpmLimiter`), limitando las solicitudes a 3 llamadas por minuto y respondiendo con HTTP `429 Too Many Requests` cuando se supera la cuota.

---

## 🚀 Guía de Inicio Rápido (Despliegue con Docker Compose)

El proyecto incluye configuración lista para ejecutar mediante Docker y Docker Compose, integrando la base de datos PostgreSQL y la aplicación Spring Boot.

### Prerrequisitos
* [Docker](https://www.docker.com/) y Docker Compose instalados.
* *(Opcional)* Java 21 y Maven 3.9+ si se desea compilar y ejecutar localmente.

### Pasos para Ejecutar

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/agleveratto/calculator-api.git
   cd calculator-api
   ```

**Opcional descargar la imagen de docker hub**
  ```bash
  docker pull aleveratto/calculator-api:latest
  ```

2. **Levantar los servicios:**
   ```bash
   docker compose up --build -d
   ```

3. **Verificar estado de los contenedores:**
   ```bash
   docker compose ps
   ```

La API quedará expuesta y lista en: `http://localhost:8080`

---

## 📄 Documentación Interactiva (Swagger / OpenAPI)

Con la aplicación en ejecución, se puede acceder a la documentación interactiva:

* **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Spec (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🔌 Endpoints de la API

### 1. Realizar Cálculo con Porcentaje Dinámico
Realiza la suma de dos números (`num1` y `num2`) y le aplica el porcentaje dinámico obtenido del servicio externo. Cada llamada se registra en el historial de auditoría de forma asíncrona.

* **Método:** `GET`
* **URL:** `/api/calculate`
* **Parámetros (Query Params):**
  * `num1` (double, requerido): Primer número (ej. `5.0`)
  * `num2` (double, requerido): Segundo número (ej. `5.0`)
* **Rate Limit:** Máximo 3 peticiones por minuto (RPM).

#### Ejemplo de Solicitud:
```bash
curl -X GET "http://localhost:8080/api/calculate?num1=5.0&num2=5.0"
```

#### Respuesta Exitosa (`200 OK`):
```json
{
  "num1": 5.0,
  "num2": 5.0,
  "percentageApplied": 10.0,
  "finalResult": 11.0
}
```

#### Respuesta de Rate Limit Excedido (`429 Too Many Requests`):
```text
Rate limit exceeded. Maximum 3 requests per minute allowed.
```

---

### 2. Consultar Historial de Auditoría
Consulta el listado paginado de todas las llamadas realizadas a la API registradas en la base de datos.

* **Método:** `GET`
* **URL:** `/api/history`
* **Parámetros (Query Params):**
  * `page` (int, opcional, por defecto `0`): Índice de página (base 0).
  * `size` (int, opcional, por defecto `10`): Tamaño de la página.
* **Rate Limit:** Máximo 3 peticiones por minuto (RPM).

#### Ejemplo de Solicitud:
```bash
curl -X GET "http://localhost:8080/api/history?page=0&size=10"
```

#### Respuesta Exitosa (`200 OK`):
```json
[
  {
    "id": 1,
    "timestamp": "2026-03-30T14:30:00",
    "endpoint": "/api/calculate",
    "parameters": "num1=5.00, num2=5.00",
    "response": "CalculationResult[num1=5.0, num2=5.0, percentageApplied=10.0, finalResult=11.0]",
    "error": null
  }
]
```

---

## 🧪 Pruebas Unitarias y de Integración

Para ejecutar la suite completa de pruebas unitarias y de integración:

```bash
mvn clean test
```

### Cobertura de Pruebas:
* **Dominio y Aplicación:** Verificación de la lógica de cálculo (`CalculateServiceTest`), consultas de historial (`GetHistoryServiceTest`) y modelos (`CalculationResultTest`).
* **Resiliencia y Reintentos:** Comportamiento ante fallos y reintentos del servicio externo (`PercentageMockAdapterTest`).
* **Rate Limiting e Interceptores:** Validación del límite de 3 RPM y excepciones HTTP 429 (`CalculatorRestControllerRateLimitTest`, `HistoryRestControllerRateLimitTest`, `RateLimitInterceptorTest`, `SemaphoreRpmLimiterTest`).
* **Asincronía y Persistencia:** Verificación de publicación asíncrona de logs (`AsyncLogPublisherTest`) y mapeo/persistencia JPA (`ApiLogJpaAdapterTest`).
