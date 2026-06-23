# Microservicio: geolocalizacion — Sanos y Salvos

Almacena las coordenadas GPS de cada mascota reportada y calcula distancias entre ubicaciones (fórmula de Haversine) para sugerir coincidencias por cercanía geográfica.

## Tecnologías

- Java 21
- Spring Boot 4.0.7
- Spring Data JPA (Hibernate) + PostgreSQL

## Requisitos previos

- JDK 21 instalado y `JAVA_HOME` configurado.
- PostgreSQL corriendo localmente (puerto 5432) con la base de datos creada:
  ```sql
  CREATE DATABASE sanos_salvos_geolocalizacion;
  ```

> Nota: este microservicio **no usa PostGIS**. Las coordenadas se guardan como columnas `Double` simples (`latitud`, `longitud`) y la distancia se calcula en Java puro con la fórmula de Haversine.

## Instalar y ejecutar

```bash
cd geolocalizacion
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8083`.

## Ejecutar las pruebas y ver la cobertura

```bash
cd geolocalizacion
./mvnw clean test
```

Las pruebas usan una base de datos **H2 en memoria** (configurada en `src/test/resources/application.properties`); no necesitas Postgres corriendo para correr los tests.

Reporte de cobertura JaCoCo: `geolocalizacion/target/site/jacoco/index.html`.

## Documentación interactiva de la API (Swagger)

Con el servicio corriendo:

- Swagger UI: `http://localhost:8083/swagger-ui.html`
- Especificación OpenAPI (JSON): `http://localhost:8083/v3/api-docs`

## Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/geolocalizacion/registrar` | Registra o actualiza la ubicación de una mascota |
| GET | `/api/geolocalizacion/cercanas` | Busca mascotas dentro de un radio (km) de un punto |
| GET | `/api/geolocalizacion/distancia` | Calcula la distancia entre dos mascotas |
| GET | `/api/geolocalizacion/coincidencias/{idMascota}` | Mascotas perdidas cerca de una mascota encontrada |
| GET | `/api/geolocalizacion/todas` | Lista todas las ubicaciones registradas (para el mapa) |

Ejemplos de peticiones y respuestas: ver la colección de Postman en la raíz del repo (`Sanos-y-Salvos.postman_collection.json`).
