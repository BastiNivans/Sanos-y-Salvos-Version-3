package com.sanosysalvos.mascotas.factory;

import com.sanosysalvos.mascotas.model.Mascota;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias para MascotaFactory.
 * No requiere contexto de Spring ni mocks: es una clase de lógica pura.
 */
class MascotaFactoryTest {

    private final MascotaFactory factory = new MascotaFactory();

    @Test
    @DisplayName("Crea correctamente un reporte de tipo PERDIDA")
    void crearReporte_tipoPerdida_creaMascotaConDatosCorrectos() {
        Mascota mascota = factory.crearReporte("PERDIDA", "Perro", "Labrador", "Puente Alto");

        assertThat(mascota.getTipoReporte()).isEqualTo("PERDIDA");
        assertThat(mascota.getEspecie()).isEqualTo("Perro");
        assertThat(mascota.getRaza()).isEqualTo("Labrador");
        assertThat(mascota.getUbicacion()).isEqualTo("Puente Alto");
    }

    @Test
    @DisplayName("Crea correctamente un reporte de tipo ENCONTRADA")
    void crearReporte_tipoEncontrada_creaMascotaConDatosCorrectos() {
        Mascota mascota = factory.crearReporte("ENCONTRADA", "Gato", "Siames", "La Florida");

        assertThat(mascota.getTipoReporte()).isEqualTo("ENCONTRADA");
        assertThat(mascota.getEspecie()).isEqualTo("Gato");
    }

    @ParameterizedTest
    @DisplayName("El tipo de reporte se normaliza a mayúsculas sin importar el casing de entrada")
    @ValueSource(strings = {"perdida", "Perdida", "PERDIDA", "PeRdIdA"})
    void crearReporte_aceptaTipoEnCualquierCasing(String tipoEntrada) {
        Mascota mascota = factory.crearReporte(tipoEntrada, "Perro", "Mestizo", "Santiago");

        assertThat(mascota.getTipoReporte()).isEqualTo("PERDIDA");
    }

    @Test
    @DisplayName("Lanza IllegalArgumentException si el tipo de reporte no es válido")
    void crearReporte_tipoInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> factory.crearReporte("DESAPARECIDA", "Perro", "Mestizo", "Santiago"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tipo de reporte no válido");
    }

    @Test
    @DisplayName("Lanza IllegalArgumentException si el tipo es nulo")
    void crearReporte_tipoNulo_lanzaExcepcion() {
        assertThatThrownBy(() -> factory.crearReporte(null, "Perro", "Mestizo", "Santiago"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Lanza IllegalArgumentException si el tipo es una cadena vacía")
    void crearReporte_tipoVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> factory.crearReporte("", "Perro", "Mestizo", "Santiago"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
