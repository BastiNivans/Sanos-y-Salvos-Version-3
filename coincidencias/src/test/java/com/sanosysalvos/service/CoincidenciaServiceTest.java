package com.sanosysalvos.service;

import com.sanosysalvos.model.Coincidencia;
import com.sanosysalvos.repository.CoincidenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CoincidenciaService.
 *
 * Nota de diseño: igual que en GeocodificacionService (microservicio mascotas),
 * el RestTemplate aquí se instancia directamente como campo final en vez de
 * inyectarse, por lo que se reemplaza vía ReflectionTestUtils para aislar el
 * test de la red real (llamada al microservicio de mascotas en :8081).
 */
@ExtendWith(MockitoExtension.class)
class CoincidenciaServiceTest {

    @Mock
    private CoincidenciaRepository coincidenciaRepository;

    @Mock
    private RestTemplate restTemplateMock;

    @InjectMocks
    private CoincidenciaService coincidenciaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(coincidenciaService, "restTemplate", restTemplateMock);
    }

    @Test
    @DisplayName("buscarCoincidencias devuelve el mensaje fijo del circuit breaker")
    void buscarCoincidencias_devuelveMensajeFijo() {
        assertThat(coincidenciaService.buscarCoincidencias())
            .isEqualTo("Servicio de coincidencias activo - Circuit Breaker funcionando");
    }

    @Test
    @DisplayName("Si la mascota objetivo no existe en el microservicio de mascotas, devuelve lista vacía")
    void calcularYGuardar_mascotaObjetivoNoExiste_devuelveListaVacia() {
        List<Map<String, Object>> mascotas = List.of(
            mascota(2, "Gato", "Siames", "PERDIDA", "Macul")
        );
        when(restTemplateMock.getForObject(anyString(), eq(Object.class))).thenReturn(mascotas);

        List<Coincidencia> resultado = coincidenciaService.calcularYGuardarCoincidencias(99L);

        assertThat(resultado).isEmpty();
        verify(coincidenciaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Crea y guarda una nueva coincidencia cuando la similitud es alta y no existe previamente")
    void calcularYGuardar_similitudAlta_creaYGuardaCoincidencia() {
        List<Map<String, Object>> mascotas = List.of(
            mascota(1, "Perro", "Labrador", "PERDIDA", "Puente Alto"),
            mascota(2, "Perro", "Labrador", "ENCONTRADA", "Puente Alto")
        );
        when(restTemplateMock.getForObject(anyString(), eq(Object.class))).thenReturn(mascotas);
        when(coincidenciaRepository.existsByIdMascotaPerdidaAndIdMascotaEncontrada(any(), any()))
            .thenReturn(false);

        List<Coincidencia> resultado = coincidenciaService.calcularYGuardarCoincidencias(1L);

        assertThat(resultado).hasSize(1);
        Coincidencia coincidencia = resultado.get(0);
        assertThat(coincidencia.getIdMascotaPerdida()).isEqualTo(1L);
        assertThat(coincidencia.getIdMascotaEncontrada()).isEqualTo(2L);
        assertThat(coincidencia.getNivelSimilitud()).isEqualTo(100.0); // especie+raza+ubicación+tipo complementario
        verify(coincidenciaRepository).saveAll(resultado);
    }

    @Test
    @DisplayName("No crea coincidencia cuando la similitud calculada es menor al 40%")
    void calcularYGuardar_similitudBaja_noCreaCoincidencia() {
        List<Map<String, Object>> mascotas = List.of(
            mascota(1, "Perro", "Quiltro", "PERDIDA", "Santiago"),
            mascota(2, "Gato", "Persa", "PERDIDA", "Valparaiso")
        );
        when(restTemplateMock.getForObject(anyString(), eq(Object.class))).thenReturn(mascotas);

        List<Coincidencia> resultado = coincidenciaService.calcularYGuardarCoincidencias(1L);

        assertThat(resultado).isEmpty();
        verify(coincidenciaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("No duplica una coincidencia que ya existe en cualquiera de los dos sentidos")
    void calcularYGuardar_coincidenciaYaExiste_seIgnora() {
        List<Map<String, Object>> mascotas = List.of(
            mascota(1, "Perro", "Labrador", "PERDIDA", "Puente Alto"),
            mascota(2, "Perro", "Labrador", "ENCONTRADA", "Puente Alto")
        );
        when(restTemplateMock.getForObject(anyString(), eq(Object.class))).thenReturn(mascotas);
        when(coincidenciaRepository.existsByIdMascotaPerdidaAndIdMascotaEncontrada(1L, 2L))
            .thenReturn(true);
        when(coincidenciaRepository.existsByIdMascotaPerdidaAndIdMascotaEncontrada(2L, 1L))
            .thenReturn(false);

        List<Coincidencia> resultado = coincidenciaService.calcularYGuardarCoincidencias(1L);

        assertThat(resultado).isEmpty();
        verify(coincidenciaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Si la llamada al microservicio de mascotas falla, devuelve lista vacía sin lanzar excepción")
    void calcularYGuardar_errorDeRed_devuelveListaVaciaSinLanzar() {
        when(restTemplateMock.getForObject(anyString(), eq(Object.class)))
            .thenThrow(new RuntimeException("Connection refused"));

        List<Coincidencia> resultado = coincidenciaService.calcularYGuardarCoincidencias(1L);

        assertThat(resultado).isEmpty();
        verify(coincidenciaRepository, never()).saveAll(any());
    }

    /**
     * Construye un mapa que imita la forma del JSON que devuelve el
     * microservicio de mascotas. El "id" se guarda como Integer a propósito,
     * porque el servicio bajo prueba hace un cast explícito a Integer
     * (tal como llegaría deserializado desde una respuesta JSON real).
     */
    private Map<String, Object> mascota(int id, String especie, String raza, String tipoReporte, String ubicacion) {
        return Map.of(
            "id", id,
            "especie", especie,
            "raza", raza,
            "tipoReporte", tipoReporte,
            "ubicacion", ubicacion
        );
    }
}
