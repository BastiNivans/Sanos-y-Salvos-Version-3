package com.sanosysalvos.bff.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para AuthController.
 * Igual que en BffController, el RestTemplate se reemplaza vía
 * ReflectionTestUtils para no depender del microservicio real de usuarios (:8081).
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RestTemplate restTemplateMock;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController();
        ReflectionTestUtils.setField(authController, "restTemplate", restTemplateMock);
    }

    @Test
    @DisplayName("login reenvía la respuesta exitosa del microservicio de usuarios")
    void login_credencialesValidas_reenviaRespuestaExitosa() {
        Map<String, Object> cuerpoExitoso = Map.of("mensaje", "Login exitoso", "correo", "ana@test.com");
        when(restTemplateMock.postForEntity(
                eq("http://localhost:8081/api/usuarios/login"), any(), eq(Object.class)))
            .thenReturn(ResponseEntity.ok(cuerpoExitoso));

        ResponseEntity<?> respuesta = authController.login(
            Map.of("correo", "ana@test.com", "contrasena", "clave123"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(cuerpoExitoso);
    }

    @Test
    @DisplayName("login devuelve 401 cuando el microservicio de usuarios lanza una excepción")
    void login_microservicioFalla_devuelve401() {
        when(restTemplateMock.postForEntity(
                eq("http://localhost:8081/api/usuarios/login"), any(), eq(Object.class)))
            .thenThrow(HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        ResponseEntity<?> respuesta = authController.login(
            Map.of("correo", "ana@test.com", "contrasena", "incorrecta"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(respuesta.getBody()).isEqualTo(Map.of("error", "Credenciales incorrectas"));
    }

    @Test
    @DisplayName("registro reenvía la respuesta exitosa del microservicio de usuarios")
    void registro_correoNuevo_reenviaRespuestaExitosa() {
        Map<String, Object> cuerpoExitoso = Map.of("mensaje", "Registro exitoso", "correo", "nuevo@test.com");
        when(restTemplateMock.postForEntity(
                eq("http://localhost:8081/api/usuarios/registro"), any(), eq(Object.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(cuerpoExitoso));

        ResponseEntity<?> respuesta = authController.registro(
            Map.of("correo", "nuevo@test.com", "contrasena", "clave123"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isEqualTo(cuerpoExitoso);
    }

    @Test
    @DisplayName("registro devuelve 409 cuando el microservicio de usuarios reporta correo duplicado")
    void registro_correoDuplicado_devuelve409() {
        when(restTemplateMock.postForEntity(
                eq("http://localhost:8081/api/usuarios/registro"), any(), eq(Object.class)))
            .thenThrow(HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", null, null, null));

        ResponseEntity<?> respuesta = authController.registro(
            Map.of("correo", "existente@test.com", "contrasena", "clave123"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(respuesta.getBody()).isEqualTo(Map.of("error", "Este correo ya está registrado"));
    }

    @Test
    @DisplayName("registro devuelve 400 ante cualquier otro error del microservicio de usuarios")
    void registro_errorGenerico_devuelve400() {
        when(restTemplateMock.postForEntity(
                eq("http://localhost:8081/api/usuarios/registro"), any(), eq(Object.class)))
            .thenThrow(new RuntimeException("Timeout"));

        ResponseEntity<?> respuesta = authController.registro(
            Map.of("correo", "ana@test.com", "contrasena", "clave123"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody())
            .isEqualTo(Map.of("error", "Error al registrar usuario. Intenta nuevamente."));
    }
}
