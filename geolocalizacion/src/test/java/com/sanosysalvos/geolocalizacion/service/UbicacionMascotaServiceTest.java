package com.sanosysalvos.geolocalizacion.service;

import com.sanosysalvos.geolocalizacion.model.UbicacionMascota;
import com.sanosysalvos.geolocalizacion.repository.UbicacionMascotaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UbicacionMascotaService.
 * Esta clase no tiene dependencias externas vía RestTemplate, así que el
 * único colaborador a mockear es el repository: @InjectMocks funciona sin
 * necesidad de reflection.
 */
@ExtendWith(MockitoExtension.class)
class UbicacionMascotaServiceTest {

    @Mock
    private UbicacionMascotaRepository repository;

    @InjectMocks
    private UbicacionMascotaService service;

    // ---------- registrarUbicacion ----------

    @Test
    @DisplayName("Crea una nueva ubicación cuando la mascota no tiene una registrada previamente")
    void registrarUbicacion_mascotaSinUbicacionPrevia_creaNueva() {
        when(repository.findByIdMascota(1L)).thenReturn(null);
        when(repository.save(any(UbicacionMascota.class))).thenAnswer(inv -> inv.getArgument(0));

        UbicacionMascota resultado = service.registrarUbicacion(
            1L, "Perro", "Labrador", "PERDIDA", -33.6, -70.5, "Puente Alto");

        assertThat(resultado.getIdMascota()).isEqualTo(1L);
        assertThat(resultado.getEspecie()).isEqualTo("Perro");
        assertThat(resultado.getLatitud()).isEqualTo(-33.6);
        verify(repository).save(any(UbicacionMascota.class));
    }

    @Test
    @DisplayName("Actualiza la ubicación existente en vez de crear una nueva")
    void registrarUbicacion_mascotaConUbicacionPrevia_actualizaExistente() {
        UbicacionMascota existente = new UbicacionMascota();
        existente.setId(99L);
        existente.setIdMascota(1L);
        existente.setLatitud(-33.0);
        existente.setLongitud(-70.0);

        when(repository.findByIdMascota(1L)).thenReturn(existente);
        when(repository.save(any(UbicacionMascota.class))).thenAnswer(inv -> inv.getArgument(0));

        UbicacionMascota resultado = service.registrarUbicacion(
            1L, "Perro", "Labrador", "ENCONTRADA", -33.6, -70.5, "Nueva dirección");

        assertThat(resultado.getId()).isEqualTo(99L); // sigue siendo el mismo registro
        assertThat(resultado.getLatitud()).isEqualTo(-33.6);
        assertThat(resultado.getDireccionTexto()).isEqualTo("Nueva dirección");
        verify(repository, never()).save(argThat(u -> u.getId() == null));
    }

    // ---------- calcularDistancia (Haversine) ----------

    @Test
    @DisplayName("Calcula correctamente la distancia entre dos puntos conocidos (Santiago Centro - Puente Alto)")
    void calcularDistancia_puntosConocidos_calculaDistanciaAproximada() {
        // Plaza de Armas (Santiago) y Puente Alto (centro): ~20.66 km en línea recta
        Double distancia = service.calcularDistancia(-33.4372, -70.6506, -33.6119, -70.5746);

        assertThat(distancia).isCloseTo(20.66, within(0.5));
    }

    @Test
    @DisplayName("La distancia entre un punto y sí mismo es 0")
    void calcularDistancia_mismoPunto_devuelveCero() {
        Double distancia = service.calcularDistancia(-33.45, -70.65, -33.45, -70.65);

        assertThat(distancia).isCloseTo(0.0, within(0.001));
    }

    @ParameterizedTest
    @DisplayName("Si cualquiera de las coordenadas es null, devuelve Double.MAX_VALUE")
    @CsvSource({
        "true, false, false, false",
        "false, true, false, false",
        "false, false, true, false",
        "false, false, false, true"
    })
    void calcularDistancia_coordenadaNula_devuelveValorMaximo(
            boolean lat1Nula, boolean lon1Nula, boolean lat2Nula, boolean lon2Nula) {
        Double distancia = service.calcularDistancia(
            lat1Nula ? null : -33.45,
            lon1Nula ? null : -70.65,
            lat2Nula ? null : -33.50,
            lon2Nula ? null : -70.60);

        assertThat(distancia).isEqualTo(Double.MAX_VALUE);
    }

    // ---------- buscarCercanas ----------

    @Test
    @DisplayName("buscarCercanas devuelve solo las mascotas dentro del radio, excluyendo la indicada")
    void buscarCercanas_filtraPorRadioYExcluyeMascotaIndicada() {
        UbicacionMascota cercana = ubicacion(1L, -33.45, -70.65);
        UbicacionMascota lejana = ubicacion(2L, -10.0, -70.0); // muy lejos
        UbicacionMascota excluida = ubicacion(3L, -33.45, -70.65); // cerca pero excluida

        when(repository.findAll()).thenReturn(List.of(cercana, lejana, excluida));

        Map<String, Object> resultado = service.buscarCercanas(-33.45, -70.65, 5.0, 3L);

        assertThat(resultado.get("total")).isEqualTo(1);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mascotas = (List<Map<String, Object>>) resultado.get("mascotas");
        assertThat(mascotas).hasSize(1);
        assertThat(((UbicacionMascota) mascotas.get(0).get("mascota")).getIdMascota()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarCercanas devuelve total 0 cuando ninguna mascota está dentro del radio")
    void buscarCercanas_sinResultadosEnElRadio_devuelveTotalCero() {
        UbicacionMascota lejana = ubicacion(1L, 10.0, 10.0);
        when(repository.findAll()).thenReturn(List.of(lejana));

        Map<String, Object> resultado = service.buscarCercanas(-33.45, -70.65, 1.0, null);

        assertThat(resultado.get("total")).isEqualTo(0);
    }

    // ---------- buscarCoincidenciasGeolocalizadas ----------

    @Test
    @DisplayName("Devuelve lista vacía si la mascota encontrada no tiene ubicación registrada")
    void buscarCoincidenciasGeolocalizadas_sinUbicacionRegistrada_devuelveListaVacia() {
        when(repository.findByIdMascota(1L)).thenReturn(null);

        List<Map<String, Object>> resultado = service.buscarCoincidenciasGeolocalizadas(1L, 2.0);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Encuentra mascotas perdidas dentro del radio, excluyendo la mascota encontrada")
    void buscarCoincidenciasGeolocalizadas_encuentraPerdidasDentroDelRadio() {
        UbicacionMascota encontrada = ubicacion(1L, -33.45, -70.65);
        encontrada.setTipoReporte("ENCONTRADA");

        UbicacionMascota perdidaCercana = ubicacion(2L, -33.45, -70.65);
        perdidaCercana.setTipoReporte("PERDIDA");

        when(repository.findByIdMascota(1L)).thenReturn(encontrada);
        when(repository.findByTipoReporte("PERDIDA")).thenReturn(List.of(perdidaCercana));

        List<Map<String, Object>> resultado = service.buscarCoincidenciasGeolocalizadas(1L, 2.0);

        assertThat(resultado).hasSize(1);
        assertThat(((UbicacionMascota) resultado.get(0).get("mascota")).getIdMascota()).isEqualTo(2L);
    }

    // ---------- obtenerTodas ----------

    @Test
    @DisplayName("obtenerTodas devuelve todas las ubicaciones del repositorio")
    void obtenerTodas_devuelveTodasLasUbicaciones() {
        when(repository.findAll()).thenReturn(List.of(ubicacion(1L, -33.0, -70.0)));

        List<UbicacionMascota> resultado = service.obtenerTodas();

        assertThat(resultado).hasSize(1);
    }

    private UbicacionMascota ubicacion(Long idMascota, double lat, double lon) {
        UbicacionMascota u = new UbicacionMascota();
        u.setIdMascota(idMascota);
        u.setLatitud(lat);
        u.setLongitud(lon);
        return u;
    }
}
