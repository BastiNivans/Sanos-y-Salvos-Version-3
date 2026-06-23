package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.model.Usuario;
import com.sanosysalvos.mascotas.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UsuarioController.
 * Al usar inyección por constructor, se puede testear como un POJO simple
 * con Mockito, sin necesidad de levantar el contexto de Spring (@WebMvcTest).
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioController usuarioController;

    // ---------- LOGIN ----------

    @Test
    @DisplayName("Login exitoso cuando el correo existe y la contraseña coincide")
    void login_credencialesCorrectas_devuelveOkConToken() {
        Usuario usuario = new Usuario("ana@test.com", "clave123");
        when(usuarioRepository.findByCorreo("ana@test.com")).thenReturn(Optional.of(usuario));

        ResponseEntity<?> respuesta = usuarioController.login(
            Map.of("correo", "ana@test.com", "contrasena", "clave123"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) respuesta.getBody();
        assertThat(body).containsEntry("mensaje", "Login exitoso");
        assertThat(body).containsEntry("correo", "ana@test.com");
        assertThat(body).containsKey("token");
    }

    @Test
    @DisplayName("Login falla con 401 cuando la contraseña es incorrecta")
    void login_contrasenaIncorrecta_devuelve401() {
        Usuario usuario = new Usuario("ana@test.com", "clave123");
        when(usuarioRepository.findByCorreo("ana@test.com")).thenReturn(Optional.of(usuario));

        ResponseEntity<?> respuesta = usuarioController.login(
            Map.of("correo", "ana@test.com", "contrasena", "claveIncorrecta"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Login falla con 401 cuando el correo no existe")
    void login_correoNoExiste_devuelve401() {
        when(usuarioRepository.findByCorreo("desconocido@test.com")).thenReturn(Optional.empty());

        ResponseEntity<?> respuesta = usuarioController.login(
            Map.of("correo", "desconocido@test.com", "contrasena", "cualquiera"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ---------- REGISTRO ----------

    @Test
    @DisplayName("Registro exitoso cuando el correo no existe previamente")
    void registrarUsuario_correoNuevo_devuelve201YGuardaUsuario() {
        when(usuarioRepository.findByCorreo("nuevo@test.com")).thenReturn(Optional.empty());

        ResponseEntity<?> respuesta = usuarioController.registrarUsuario(
            Map.of("correo", "nuevo@test.com", "contrasena", "clave123"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(usuarioRepository).save(argThat(u ->
            u.getCorreo().equals("nuevo@test.com") && u.getContrasena().equals("clave123")));
    }

    @Test
    @DisplayName("Registro falla con 409 cuando el correo ya está registrado")
    void registrarUsuario_correoYaExiste_devuelve409YNoGuarda() {
        when(usuarioRepository.findByCorreo("existente@test.com"))
            .thenReturn(Optional.of(new Usuario("existente@test.com", "otraClave")));

        ResponseEntity<?> respuesta = usuarioController.registrarUsuario(
            Map.of("correo", "existente@test.com", "contrasena", "clave123"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro falla con 400 cuando el correo viene vacío")
    void registrarUsuario_correoVacio_devuelve400() {
        ResponseEntity<?> respuesta = usuarioController.registrarUsuario(
            Map.of("correo", "", "contrasena", "clave123"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro falla con 400 cuando la contraseña viene vacía")
    void registrarUsuario_contrasenaVacia_devuelve400() {
        ResponseEntity<?> respuesta = usuarioController.registrarUsuario(
            Map.of("correo", "ana@test.com", "contrasena", ""));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(usuarioRepository, never()).save(any());
    }
}
