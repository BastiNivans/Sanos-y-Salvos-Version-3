package com.sanosysalvos.bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class BffController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/mascotas")
    public Object obtenerMascotas() {
        return restTemplate.getForObject("http://localhost:8081/api/mascotas", Object.class);
    }

    // 🆕 POST con automatización de coincidencias
    @PostMapping("/mascotas")
    public Object registrarMascota(@RequestBody Object mascota) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "http://localhost:8081/api/mascotas", 
            mascota, 
            Map.class
        );
        
        Map mascotaGuardada = response.getBody();

        if (mascotaGuardada != null && mascotaGuardada.get("id") != null) {
            Long idMascota = Long.valueOf(mascotaGuardada.get("id").toString());
            System.out.println(" AUTOMATIZACIÓN: Calculando coincidencias para la mascota ID: " + idMascota);
            
            try {
                restTemplate.getForObject(
                    "http://localhost:8082/api/coincidencias/calcular/" + idMascota, 
                    Object.class
                );
                System.out.println("✅ Coincidencias calculadas automáticamente.");
            } catch (Exception e) {
                System.err.println("️ Falló el cálculo automático: " + e.getMessage());
            }
        }

        return mascotaGuardada;
    }

    @GetMapping("/coincidencias")
    public Object obtenerCoincidencias() {
        return restTemplate.getForObject("http://localhost:8082/api/coincidencias", Object.class);
    }

    @GetMapping("/coincidencias/calcular/{mascotaId}")
    public Object calcularCoincidencias(@PathVariable Long mascotaId) {
        return restTemplate.getForObject(
            "http://localhost:8082/api/coincidencias/calcular/" + mascotaId, 
            Object.class
        );
    }

    @GetMapping("/coincidencias/mascota/{mascotaId}")
    public Object obtenerCoincidenciasDeMascota(@PathVariable Long mascotaId) {
        return restTemplate.getForObject(
            "http://localhost:8082/api/coincidencias/mascota/" + mascotaId, 
            Object.class
        );
    }
}