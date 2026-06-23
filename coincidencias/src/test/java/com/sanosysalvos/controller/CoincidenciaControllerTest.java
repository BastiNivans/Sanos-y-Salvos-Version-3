package com.sanosysalvos.controller;

import com.sanosysalvos.model.Coincidencia;
import com.sanosysalvos.repository.CoincidenciaRepository;
import com.sanosysalvos.service.CoincidenciaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CoincidenciaController.
 * Sin RestTemplate involucrado directamente aquí: el controller solo delega
 * en el repository y el service, por lo que un test con Mockito puro
 * (@InjectMocks) es suficiente, sin necesidad de reflection.
 */
@ExtendWith(MockitoExtension.class)
class CoincidenciaControllerTest {

    @Mock
    private CoincidenciaRepository coincidenciaRepository;

    @Mock
    private CoincidenciaService coincidenciaService;

    @InjectMocks
    private CoincidenciaController coincidenciaController;

    @Test
    @DisplayName("listarCoincidencias devuelve todas las coincidencias del repositorio")
    void listarCoincidencias_devuelveTodasDelRepositorio() {
        List<Coincidencia> coincidencias = List.of(new Coincidencia(), new Coincidencia());
        when(coincidenciaRepository.findAll()).thenReturn(coincidencias);

        List<Coincidencia> resultado = coincidenciaController.listarCoincidencias();

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("probarCircuitBreaker delega en el service")
    void probarCircuitBreaker_delegaEnElService() {
        when(coincidenciaService.buscarCoincidencias()).thenReturn("activo");

        String resultado = coincidenciaController.probarCircuitBreaker();

        assertThat(resultado).isEqualTo("activo");
    }

    @Test
    @DisplayName("calcularCoincidenciasParaMascota delega en el service con el id correcto")
    void calcularCoincidenciasParaMascota_delegaEnElService() {
        List<Coincidencia> esperado = List.of(new Coincidencia());
        when(coincidenciaService.calcularYGuardarCoincidencias(5L)).thenReturn(esperado);

        List<Coincidencia> resultado = coincidenciaController.calcularCoincidenciasParaMascota(5L);

        assertThat(resultado).isEqualTo(esperado);
        verify(coincidenciaService).calcularYGuardarCoincidencias(5L);
    }

    @Test
    @DisplayName("obtenerCoincidenciasDeMascota filtra las coincidencias donde la mascota es perdida o encontrada")
    void obtenerCoincidenciasDeMascota_filtraPorPerdidaOEncontrada() {
        Coincidencia c1 = new Coincidencia();
        c1.setIdMascotaPerdida(1L);
        c1.setIdMascotaEncontrada(2L);

        Coincidencia c2 = new Coincidencia();
        c2.setIdMascotaPerdida(3L);
        c2.setIdMascotaEncontrada(1L);

        Coincidencia c3 = new Coincidencia();
        c3.setIdMascotaPerdida(7L);
        c3.setIdMascotaEncontrada(8L);

        when(coincidenciaRepository.findAll()).thenReturn(List.of(c1, c2, c3));

        List<Coincidencia> resultado = coincidenciaController.obtenerCoincidenciasDeMascota(1L);

        assertThat(resultado).containsExactlyInAnyOrder(c1, c2);
    }
}
