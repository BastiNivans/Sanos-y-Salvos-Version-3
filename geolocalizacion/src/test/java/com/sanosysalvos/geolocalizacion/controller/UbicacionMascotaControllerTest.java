package com.sanosysalvos.geolocalizacion.controller;

import com.sanosysalvos.geolocalizacion.model.UbicacionMascota;
import com.sanosysalvos.geolocalizacion.service.UbicacionMascotaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UbicacionMascotaControllerTest {

    @Mock
    private UbicacionMascotaService service;

    @InjectMocks
    private UbicacionMascotaController controller;

    @Test
    @DisplayName("registrarUbicacion delega en el service y devuelve 200 con la ubicación guardada")
    void registrarUbicacion_delegaEnServicio() {
        UbicacionMascota guardada = new UbicacionMascota();
        guardada.setIdMascota(1L);
        when(service.registrarUbicacion(1L, "Perro", "Labrador", "PERDIDA", -33.6, -70.5, "Macul"))
            .thenReturn(guardada);

        ResponseEntity<UbicacionMascota> respuesta = controller.registrarUbicacion(
            1L, "Perro", "Labrador", "PERDIDA", -33.6, -70.5, "Macul");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getIdMascota()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarCercanas delega en el service y devuelve 200 con el resultado")
    void buscarCercanas_delegaEnServicio() {
        Map<String, Object> resultadoEsperado = Map.of("total", 1);
        when(service.buscarCercanas(-33.6, -70.5, 2.0, 5L)).thenReturn(resultadoEsperado);

        ResponseEntity<Map<String, Object>> respuesta = controller.buscarCercanas(-33.6, -70.5, 2.0, 5L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(resultadoEsperado);
    }

    @Test
    @DisplayName("calcularDistancia devuelve 404 si alguna de las mascotas no tiene ubicación registrada")
    void calcularDistancia_mascotaInexistente_devuelve404() {
        UbicacionMascota m1 = ubicacion(1L);
        when(service.obtenerTodas()).thenReturn(List.of(m1));

        ResponseEntity<Double> respuesta = controller.calcularDistancia(1L, 99L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("calcularDistancia devuelve 200 con la distancia redondeada cuando ambas mascotas existen")
    void calcularDistancia_ambasMascotasExisten_devuelve200() {
        UbicacionMascota m1 = ubicacion(1L);
        m1.setLatitud(-33.45);
        m1.setLongitud(-70.65);
        UbicacionMascota m2 = ubicacion(2L);
        m2.setLatitud(-33.45);
        m2.setLongitud(-70.65);

        when(service.obtenerTodas()).thenReturn(List.of(m1, m2));
        when(service.calcularDistancia(-33.45, -70.65, -33.45, -70.65)).thenReturn(0.0);

        ResponseEntity<Double> respuesta = controller.calcularDistancia(1L, 2L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("buscarCoincidencias delega en el service con el radio indicado")
    void buscarCoincidencias_delegaEnServicio() {
        List<Map<String, Object>> esperado = List.of(Map.of("mascota", ubicacion(2L)));
        when(service.buscarCoincidenciasGeolocalizadas(1L, 3.0)).thenReturn(esperado);

        ResponseEntity<List<Map<String, Object>>> respuesta = controller.buscarCoincidencias(1L, 3.0);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(esperado);
    }

    @Test
    @DisplayName("obtenerTodas delega en el service")
    void obtenerTodas_delegaEnServicio() {
        List<UbicacionMascota> esperado = List.of(ubicacion(1L), ubicacion(2L));
        when(service.obtenerTodas()).thenReturn(esperado);

        ResponseEntity<List<UbicacionMascota>> respuesta = controller.obtenerTodas();

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).hasSize(2);
    }

    private UbicacionMascota ubicacion(Long idMascota) {
        UbicacionMascota u = new UbicacionMascota();
        u.setIdMascota(idMascota);
        return u;
    }
}
