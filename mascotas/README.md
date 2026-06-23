# Microservicio: mascotas — Sanos y Salvos

Gestiona usuarios (login/registro) y el ciclo de vida de los reportes de mascotas (perdida/encontrada), incluyendo imágenes y geocodificación de direcciones.

## Tecnologías

- Java 21
- Spring Boot 4.0.6
- Spring Data JPA (Hibernate) + PostgreSQL
- RestTemplate (geocodificación vía Nominatim/OpenStreetMap, y notificación a `geolocalizacion`)

## Requisitos previos

- JDK 21 instalado y `JAVA_HOME` configurado.
- PostgreSQL corriendo localmente (puerto 5432) con la base de datos `sanos_salvos_db` creada:
  ```sql
  CREATE DATABASE sanos_salvos_db;
  ```
- Usuario/contraseña configurados en `src/main/resources/application.properties` (por defecto `postgres` / `1234`; ajusta según tu instalación).

## Instalar y ejecutar

```bash
cd mascotas
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8081`. El esquema de tablas se crea/actualiza automáticamente al arrancar (`ddl-auto=update`).

## Ejecutar las pruebas y ver la cobertura

```bash
cd mascotas
./mvnw clean test
```

Las pruebas usan una base de datos **H2 en memoria** (configurada en `src/test/resources/application.properties`), por lo que **no necesitas Postgres corriendo** para correr los tests.

Reporte de cobertura JaCoCo: `mascotas/target/site/jacoco/index.html`.

## Documentación interactiva de la API (Swagger)

Con el servicio corriendo:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- Especificación OpenAPI (JSON): `http://localhost:8081/v3/api-docs`

## Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/mascotas` | Lista todas las mascotas |
| GET | `/api/mascotas/{id}` | Obtiene una mascota por id |
| POST | `/api/mascotas` | Registra una mascota (con imágenes en Base64 opcionales) |
| PUT | `/api/mascotas/{id}` | Actualiza una mascota |
| DELETE | `/api/mascotas/{id}` | Elimina una mascota |
| POST | `/api/usuarios/login` | Login (correo + contraseña) |
| POST | `/api/usuarios/registro` | Registro de un nuevo usuario |

Ejemplos de peticiones y respuestas: ver la colección de Postman en la raíz del repo (`Sanos-y-Salvos.postman_collection.json`).
