package com.sanosysalvos.mascotas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para GeocodificacionService.
 *
 * Nota de diseño: el RestTemplate se instancia internamente en la clase
 * (no se inyecta por constructor), por lo que para aislar el test de la red
 * real lo reemplazamos vía reflection con ReflectionTestUtils. Esto es un
 * "code smell" típico de testability — lo ideal a futuro sería inyectar el
 * RestTemplate como dependencia (constructor o @Bean) para no depender de
 * reflection en los tests.
 */
@ExtendWith(MockitoExtension.class)
class GeocodificacionServiceTest {

    @Mock
    private RestTemplate restTemplateMock;

    private GeocodificacionService geocodificacionService;

    @BeforeEach
    void setUp() {
        geocodificacionService = new GeocodificacionService();
        ReflectionTestUtils.setField(geocodificacionService, "restTemplate", restTemplateMock);
    }

    @Test
    @DisplayName("Devuelve coordenadas correctas cuando Nominatim responde con resultados")
    void geocodificarDireccion_respuestaValida_devuelveCoordenadas() {
        Object[] respuestaSimulada = crearRespuestaNominatim("-33.5970", "-70.5784");
        when(restTemplateMock.getForObject(any(String.class), any()))
            .thenReturn(respuestaSimulada);

        GeocodificacionService.CoordenadasDTO resultado =
            geocodificacionService.geocodificarDireccion("Av. Concha y Toro 3000, Puente Alto");

        assertThat(resultado.getLatitud()).isEqualTo(-33.5970);
        assertThat(resultado.getLongitud()).isEqualTo(-70.5784);
    }

    @Test
    @DisplayName("Devuelve coordenadas por defecto cuando Nominatim no encuentra resultados")
    void geocodificarDireccion_sinResultados_devuelveCoordenadasPorDefecto() {
        when(restTemplateMock.getForObject(any(String.class), any()))
            .thenReturn(null);

        GeocodificacionService.CoordenadasDTO resultado =
            geocodificacionService.geocodificarDireccion("Dirección que no existe en ningún lado");

        assertThat(resultado.getLatitud()).isEqualTo(-33.6119);
        assertThat(resultado.getLongitud()).isEqualTo(-70.5746);
    }

    @Test
    @DisplayName("Devuelve coordenadas por defecto cuando la llamada a Nominatim falla")
    void geocodificarDireccion_errorDeRed_devuelveCoordenadasPorDefecto() {
        when(restTemplateMock.getForObject(any(String.class), any()))
            .thenThrow(new RuntimeException("Timeout de conexión"));

        GeocodificacionService.CoordenadasDTO resultado =
            geocodificacionService.geocodificarDireccion("Cualquier dirección");

        assertThat(resultado.getLatitud()).isEqualTo(-33.6119);
        assertThat(resultado.getLongitud()).isEqualTo(-70.5746);
    }

    /**
     * Construye un array de objetos vía reflection que imita la forma de
     * GeocodificacionResponse[] (clase privada interna), para poder
     * stubear el retorno de restTemplate.getForObject sin exponer esa clase.
     */
    private Object[] crearRespuestaNominatim(String lat, String lon) {
        try {
            Class<?> responseClass = Class.forName(
                "com.sanosysalvos.mascotas.service.GeocodificacionService$GeocodificacionResponse");
            java.lang.reflect.Constructor<?> constructor = responseClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instancia = constructor.newInstance();
            ReflectionTestUtils.setField(instancia, "lat", lat);
            ReflectionTestUtils.setField(instancia, "lon", lon);
            Object[] array = (Object[]) java.lang.reflect.Array.newInstance(responseClass, 1);
            array[0] = instancia;
            return array;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo construir la respuesta simulada de Nominatim", e);
        }
    }
}
