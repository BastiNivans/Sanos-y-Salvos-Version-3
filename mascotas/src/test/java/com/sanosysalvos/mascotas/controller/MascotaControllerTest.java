package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.model.Mascota;
import com.sanosysalvos.mascotas.repository.MascotaRepository;
import com.sanosysalvos.mascotas.service.GeocodificacionService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para MascotaController.
 *
 * Nota de diseño: el controller crea una carpeta "uploads/" en su constructor
 * y, al registrar una mascota con imágenes, escribe archivos reales en disco.
 * Esto es un code smell de testability (debería delegarse a FileStorageService,
 * que ya existe en el proyecto pero no se está usando aquí). Mientras tanto,
 * estos tests conviven con ese efecto secundario y limpian los archivos que
 * generan al final de la clase.
 */
@ExtendWith(MockitoExtension.class)
class MascotaControllerTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private GeocodificacionService geocodificacionService;

    private MascotaController mascotaController;

    // Se crea en @BeforeEach (no como inicializador de campo) porque los
    // mocks anotados con @Mock recién quedan inyectados después de construir
    // la instancia de test; inicializar aquí antes le pasaría mocks nulos al controller.
    @BeforeEach
    void setUp() {
        mascotaController = new MascotaController();
        ReflectionTestUtils.setField(mascotaController, "mascotaRepository", mascotaRepository);
        ReflectionTestUtils.setField(mascotaController, "geocodificacionService", geocodificacionService);
    }

    // ---------- OBTENER ----------

    @Test
    @DisplayName("obtenerTodas devuelve la lista completa del repositorio")
    void obtenerTodas_devuelveListaDelRepositorio() {
        List<Mascota> mascotas = List.of(new Mascota(), new Mascota());
        when(mascotaRepository.findAll()).thenReturn(mascotas);

        List<Mascota> resultado = mascotaController.obtenerTodas();

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("obtenerPorId devuelve 200 con la mascota cuando existe")
    void obtenerPorId_existente_devuelve200() {
        Mascota mascota = new Mascota();
        mascota.setId(1L);
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascota));

        ResponseEntity<Mascota> respuesta = mascotaController.obtenerPorId(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("obtenerPorId devuelve 404 cuando no existe")
    void obtenerPorId_inexistente_devuelve404() {
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Mascota> respuesta = mascotaController.obtenerPorId(99L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ---------- REGISTRAR ----------

    @Test
    @DisplayName("registrarMascota sin imágenes guarda la mascota con imagenesUrls vacío")
    void registrarMascota_sinImagenes_guardaConUrlsVacias() {
        Map<String, Object> dto = Map.of(
            "especie", "Perro",
            "raza", "Quiltro",
            "tipoReporte", "PERDIDA",
            "ubicacion", "Puente Alto"
        );
        Mascota mascotaGuardada = new Mascota();
        mascotaGuardada.setId(10L);
        mascotaGuardada.setUbicacion("Puente Alto");
        when(mascotaRepository.save(any(Mascota.class))).thenReturn(mascotaGuardada);

        GeocodificacionService.CoordenadasDTO coords = new GeocodificacionService.CoordenadasDTO();
        coords.setLatitud(-33.6);
        coords.setLongitud(-70.5);
        when(geocodificacionService.geocodificarDireccion("Puente Alto")).thenReturn(coords);

        ResponseEntity<?> respuesta = mascotaController.registrarMascota(dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mascotaRepository).save(argThat(m ->
            m.getEspecie().equals("Perro")
            && m.getRaza().equals("Quiltro")
            && m.getImagenesUrls().equals("")));
    }

    @Test
    @DisplayName("registrarMascota decodifica imágenes Base64 y las guarda en imagenesUrls")
    void registrarMascota_conImagenes_decodificaYGeneraUrls() {
        String pixelBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
        Map<String, Object> dto = Map.of(
            "especie", "Gato",
            "raza", "Siames",
            "tipoReporte", "ENCONTRADA",
            "ubicacion", "",
            "imagenesBase64", List.of(pixelBase64)
        );
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invocation -> {
            Mascota m = invocation.getArgument(0);
            m.setId(20L);
            return m;
        });

        ResponseEntity<?> respuesta = mascotaController.registrarMascota(dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        Mascota body = (Mascota) respuesta.getBody();
        assertThat(body.getImagenesUrls()).startsWith("/uploads/mascota_");
        assertThat(body.getImagenesUrls()).endsWith(".jpg");
        // geocodificarDireccion no debería llamarse porque la ubicación viene vacía
        verify(geocodificacionService, never()).geocodificarDireccion(any());
    }

    @Test
    @DisplayName("registrarMascota devuelve 400 si el DTO provoca una excepción interna")
    void registrarMascota_errorInterno_devuelve400() {
        // imagenesBase64 con contenido no decodificable como Base64 -> fuerza la excepción
        Map<String, Object> dto = Map.of(
            "especie", "Perro",
            "imagenesBase64", List.of("esto-no-es-base64-valido-#$%")
        );

        ResponseEntity<?> respuesta = mascotaController.registrarMascota(dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(mascotaRepository, never()).save(any());
    }

    // ---------- ACTUALIZAR ----------

    @Test
    @DisplayName("actualizarMascota actualiza los campos cuando la mascota existe")
    void actualizarMascota_existente_actualizaYDevuelve200() {
        Mascota existente = new Mascota();
        existente.setId(1L);
        existente.setEspecie("Perro");

        Mascota datosNuevos = new Mascota();
        datosNuevos.setEspecie("Gato");
        datosNuevos.setRaza("Persa");
        datosNuevos.setTipoReporte("ENCONTRADA");
        datosNuevos.setUbicacion("Macul");
        datosNuevos.setImagenesUrls("/uploads/foto.jpg");

        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<Mascota> respuesta = mascotaController.actualizarMascota(1L, datosNuevos);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().getEspecie()).isEqualTo("Gato");
        assertThat(respuesta.getBody().getRaza()).isEqualTo("Persa");
    }

    @Test
    @DisplayName("actualizarMascota devuelve 404 cuando la mascota no existe")
    void actualizarMascota_inexistente_devuelve404() {
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Mascota> respuesta = mascotaController.actualizarMascota(99L, new Mascota());

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(mascotaRepository, never()).save(any());
    }

    // ---------- ELIMINAR ----------

    @Test
    @DisplayName("eliminarMascota elimina y devuelve 200 cuando la mascota existe")
    void eliminarMascota_existente_devuelve200() {
        Mascota existente = new Mascota();
        existente.setId(1L);
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(existente));

        ResponseEntity<Void> respuesta = mascotaController.eliminarMascota(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mascotaRepository).delete(existente);
    }

    @Test
    @DisplayName("eliminarMascota devuelve 404 cuando la mascota no existe")
    void eliminarMascota_inexistente_devuelve404() {
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Void> respuesta = mascotaController.eliminarMascota(99L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(mascotaRepository, never()).delete(any());
    }

    // Limpieza de los archivos reales que el controller escribe en disco durante los tests
    @AfterAll
    static void limpiarArchivosGenerados() {
        File uploadDir = new File("uploads/");
        File[] archivos = uploadDir.listFiles((dir, name) -> name.startsWith("mascota_"));
        if (archivos != null) {
            for (File archivo : archivos) {
                archivo.delete();
            }
        }
    }
}
