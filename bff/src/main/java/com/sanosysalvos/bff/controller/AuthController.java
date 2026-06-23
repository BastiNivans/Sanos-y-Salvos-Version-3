package com.sanosysalvos.bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") 
public class AuthController {

    private final RestTemplate restTemplate = new RestTemplate();

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        
        String urlMicroservicioUsuarios = "http://localhost:8081/api/usuarios/login";
        
        try {
            ResponseEntity<Object> respuesta = restTemplate.postForEntity(urlMicroservicioUsuarios, credenciales, Object.class);
            return respuesta;
            
        } catch (Exception e) {
            System.out.println("⚠️ EL BFF CHOCÓ EN LOGIN POR ESTA RAZÓN: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
        }
    }

    // --- REGISTRO ---
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> datosUsuario) {
        
        String urlMicroservicioUsuarios = "http://localhost:8081/api/usuarios/registro";
        
        try {
            System.out.println("📝 BFF RECIBIÓ SOLICITUD DE REGISTRO PARA: " + datosUsuario.get("correo"));
            
            ResponseEntity<Object> respuesta = restTemplate.postForEntity(urlMicroservicioUsuarios, datosUsuario, Object.class);
            
            System.out.println("✅ REGISTRO EXITOSO EN EL MICROSERVICIO");
            return respuesta;
            
        } catch (Exception e) {
            System.out.println("⚠️ EL BFF CHOCÓ EN REGISTRO POR ESTA RAZÓN: " + e.getMessage());
            
            if (e.getMessage().contains("409")) {
                return ResponseEntity.status(409).body(Map.of("error", "Este correo ya está registrado"));
            }
            
            return ResponseEntity.status(400).body(Map.of("error", "Error al registrar usuario. Intenta nuevamente."));
        }
    }
}