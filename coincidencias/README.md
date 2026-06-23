# Microservicio: coincidencias — Sanos y Salvos

Calcula el porcentaje de similitud entre mascotas perdidas y encontradas (especie, raza, ubicación y tipo de reporte), evitando guardar coincidencias duplicadas.

## Tecnologías

- Java 21
- Spring Boot 4.0.6
- Spring Data JPA (Hibernate) + PostgreSQL
- Spring Cloud Circuit Breaker (Resilience4j)
- RestTemplate (consulta al microservicio `mascotas`)

## Requisitos previos

- JDK 21 instalado y `JAVA_HOME` configurado.
- PostgreSQL corriendo localmente (puerto 5432) con la base de datos creada:
  ```sql
  CREATE DATABASE sanos_salvos_coincidencias_db;
  ```
- El microservicio `mascotas` (puerto 8081) corriendo, si quieres probar el cálculo real de coincidencias.

## Instalar y ejecutar

```bash
cd coincidencias
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8082`.

## Ejecutar las pruebas y ver la cobertura

```bash
cd coincidencias
./mvnw clean test
```

Las pruebas usan una base de datos **H2 en memoria** (configurada en `src/test/resources/application.properties`); no necesitas Postgres corriendo para correr los tests.

Reporte de cobertura JaCoCo: `coincidencias/target/site/jacoco/index.html`.

## Documentación interactiva de la API (Swagger)

Con el servicio corriendo:

- Swagger UI: `http://localhost:8082/swagger-ui.html`
- Especificación OpenAPI (JSON): `http://localhost:8082/v3/api-docs`

## Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/coincidencias` | Lista todas las coincidencias guardadas |
| GET | `/api/coincidencias/calcular/{mascotaId}` | Calcula y guarda nuevas coincidencias para una mascota |
| GET | `/api/coincidencias/mascota/{mascotaId}` | Coincidencias en las que participa una mascota |
| GET | `/api/coincidencias/test-circuit-breaker` | Endpoint de prueba del circuit breaker |

Ejemplos de peticiones y respuestas: ver la colección de Postman en la raíz del repo (`Sanos-y-Salvos.postman_collection.json`).
