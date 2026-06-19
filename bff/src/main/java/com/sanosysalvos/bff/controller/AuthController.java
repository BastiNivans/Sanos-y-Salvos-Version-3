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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        
        String urlMicroservicioMascotas = "http://localhost:8081/api/usuarios/login";
        
        try {
            ResponseEntity<Object> respuesta = restTemplate.postForEntity(urlMicroservicioMascotas, credenciales, Object.class);
            return respuesta;
            
        } catch (Exception e) {
            // SENSOR DE ERROR: Esto imprimirá la verdad en la terminal
            System.out.println("⚠️ EL BFF CHOCÓ POR ESTA RAZÓN: " + e.getMessage());
            
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
        }
    }
}