package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.model.Usuario;
import com.sanosysalvos.mascotas.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String correo = credentials.get("correo");
        String contrasena = credentials.get("contrasena");

        System.out.println("🐾 MASCOTAS RECIBIÓ UN INTENTO DE LOGIN DE: " + correo);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            if (usuario.getContrasena().equals(contrasena)) {
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Login exitoso",
                    "correo", usuario.getCorreo(),
                    "token", "jwt-simulado-sanos-y-salvos-xyz123"
                ));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales incorrectas"));
    }

    // --- REGISTRO ---
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Map<String, String> datos) {
        String correo = datos.get("correo");
        String contrasena = datos.get("contrasena");

        System.out.println("📝 NUEVO INTENTO DE REGISTRO PARA: " + correo);

        // Validar que los datos no vengan vacíos
        if (correo == null || correo.isEmpty() || contrasena == null || contrasena.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El correo y la contraseña son obligatorios"));
        }

        // Verificar si el correo ya está registrado
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Este correo ya está registrado. Intenta con otro o inicia sesión."));
        }

        // Crear el nuevo objeto Usuario y guardarlo en PostgreSQL
        Usuario nuevoUsuario = new Usuario(correo, contrasena);
        usuarioRepository.save(nuevoUsuario);

        System.out.println("✅ USUARIO CREADO EXITOSAMENTE EN LA BD");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "mensaje", "¡Registro exitoso! Ya puedes iniciar sesión.",
                    "correo", nuevoUsuario.getCorreo()
                ));
    }
}