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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String correo = credentials.get("correo");
        String contrasena = credentials.get("contrasena");

        // SENSOR: Imprime en consola cuando alguien intenta entrar
        System.out.println("🐾 MASCOTAS RECIBIÓ UN INTENTO DE LOGIN DE: " + correo);

        // 1. Buscar si el usuario existe en PostgreSQL
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // 2. Validar contraseña
            if (usuario.getContrasena().equals(contrasena)) {
                // ¡Pase VIP concedido!
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Login exitoso",
                    "correo", usuario.getCorreo(),
                    "token", "jwt-simulado-sanos-y-salvos-xyz123"
                ));
            }
        }

        // 3. Credenciales malas
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales incorrectas"));
    }
}
