package com.sanosysalvos.bff.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para BffController.
 * El RestTemplate es un campo final instanciado directamente (no inyectado),
 * por lo que se reemplaza vía ReflectionTestUtils para no depender de red real
 * hacia los microservicios de mascotas (:8081) y coincidencias (:8082).
 */
@ExtendWith(MockitoExtension.class)
class BffControllerTest {

    @Mock
    private RestTemplate restTemplateMock;

    private BffController bffController;

    @BeforeEach
    void setUp() {
        bffController = new BffController();
        ReflectionTestUtils.setField(bffController, "restTemplate", restTemplateMock);
    }

    @Test
    @DisplayName("obtenerMascotas delega en el microservicio de mascotas (:8081)")
    void obtenerMascotas_delegaEnMicroservicioDeMascotas() {
        List<Map<String, Object>> mascotasEsperadas = List.of(Map.of("id", 1));
        when(restTemplateMock.getForObject("http://localhost:8081/api/mascotas", Object.class))
            .thenReturn(mascotasEsperadas);

        Object resultado = bffController.obtenerMascotas();

        assertThat(resultado).isEqualTo(mascotasEsperadas);
    }

    @Test
    @DisplayName("registrarMascota dispara automáticamente el cálculo de coincidencias cuando la mascota se guarda con id")
    void registrarMascota_conId_disparaCalculoDeCoincidencias() {
        Map<String, Object> mascotaGuardada = Map.of("id", 10, "especie", "Perro");
        when(restTemplateMock.postForEntity(eq("http://localhost:8081/api/mascotas"), any(), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(mascotaGuardada));

        Object resultado = bffController.registrarMascota(Map.of("especie", "Perro"));

        assertThat(resultado).isEqualTo(mascotaGuardada);
        verify(restTemplateMock).getForObject(
            "http://localhost:8082/api/coincidencias/calcular/10", Object.class);
    }

    @Test
    @DisplayName("registrarMascota no dispara cálculo de coincidencias si la respuesta no trae id")
    void registrarMascota_sinId_noDisparaCalculo() {
        Map<String, Object> mascotaSinId = Map.of("especie", "Perro");
        when(restTemplateMock.postForEntity(eq("http://localhost:8081/api/mascotas"), any(), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(mascotaSinId));

        bffController.registrarMascota(Map.of("especie", "Perro"));

        verify(restTemplateMock, never()).getForObject(contains("/coincidencias/calcular/"), any());
    }

    @Test
    @DisplayName("registrarMascota no propaga la excepción si falla el cálculo automático de coincidencias")
    void registrarMascota_errorEnCalculoAutomatico_noPropagaExcepcion() {
        Map<String, Object> mascotaGuardada = Map.of("id", 10, "especie", "Perro");
        when(restTemplateMock.postForEntity(eq("http://localhost:8081/api/mascotas"), any(), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(mascotaGuardada));
        when(restTemplateMock.getForObject(
            "http://localhost:8082/api/coincidencias/calcular/10", Object.class))
            .thenThrow(new RuntimeException("coincidencias no disponible"));

        Object resultado = bffController.registrarMascota(Map.of("especie", "Perro"));

        assertThat(resultado).isEqualTo(mascotaGuardada);
    }

    @Test
    @DisplayName("obtenerCoincidencias delega en el microservicio de coincidencias (:8082)")
    void obtenerCoincidencias_delegaEnMicroservicio() {
        List<Map<String, Object>> esperado = List.of(Map.of("id", 1));
        when(restTemplateMock.getForObject("http://localhost:8082/api/coincidencias", Object.class))
            .thenReturn(esperado);

        Object resultado = bffController.obtenerCoincidencias();

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    @DisplayName("calcularCoincidencias delega con el id de mascota correcto en la URL")
    void calcularCoincidencias_delegaConIdCorrecto() {
        when(restTemplateMock.getForObject(
            "http://localhost:8082/api/coincidencias/calcular/7", Object.class))
            .thenReturn(List.of());

        bffController.calcularCoincidencias(7L);

        verify(restTemplateMock).getForObject(
            "http://localhost:8082/api/coincidencias/calcular/7", Object.class);
    }

    @Test
    @DisplayName("obtenerCoincidenciasDeMascota delega con el id de mascota correcto en la URL")
    void obtenerCoincidenciasDeMascota_delegaConIdCorrecto() {
        when(restTemplateMock.getForObject(
            "http://localhost:8082/api/coincidencias/mascota/7", Object.class))
            .thenReturn(List.of());

        bffController.obtenerCoincidenciasDeMascota(7L);

        verify(restTemplateMock).getForObject(
            "http://localhost:8082/api/coincidencias/mascota/7", Object.class);
    }
}
