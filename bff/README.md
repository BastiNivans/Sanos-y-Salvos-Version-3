# BFF (Backend For Frontend) — Sanos y Salvos

Gateway que centraliza el acceso del frontend a los microservicios `mascotas` y `coincidencias`. No tiene base de datos propia.

## Tecnologías

- Java 21
- Spring Boot 4.0.6
- Spring Cloud Gateway (WebMVC)
- RestTemplate (comunicación síncrona con los demás microservicios)

## Requisitos previos

- JDK 21 instalado y `JAVA_HOME` configurado.
- Los microservicios `mascotas` (8081) y `coincidencias` (8082) corriendo, si quieres probar los endpoints que dependen de ellos.

## Instalar y ejecutar

```bash
cd bff
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8080`.

## Ejecutar las pruebas y ver la cobertura

```bash
cd bff
./mvnw clean test
```

Reporte de cobertura JaCoCo: abre `bff/target/site/jacoco/index.html` en el navegador después de correr el comando anterior.

## Documentación interactiva de la API (Swagger)

Con el servicio corriendo:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificación OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

## Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/mascotas` | Lista todas las mascotas (vía microservicio `mascotas`) |
| POST | `/api/mascotas` | Registra una mascota y dispara el cálculo automático de coincidencias |
| GET | `/api/coincidencias` | Lista todas las coincidencias calculadas |
| GET | `/api/coincidencias/calcular/{mascotaId}` | Recalcula coincidencias para una mascota |
| GET | `/api/coincidencias/mascota/{mascotaId}` | Coincidencias asociadas a una mascota |
| POST | `/api/login` | Login de usuario (reenvía a `mascotas`) |
| POST | `/api/registro` | Registro de usuario (reenvía a `mascotas`) |

Ejemplos de peticiones y respuestas: ver la colección de Postman en la raíz del repo (`Sanos-y-Salvos.postman_collection.json`).
