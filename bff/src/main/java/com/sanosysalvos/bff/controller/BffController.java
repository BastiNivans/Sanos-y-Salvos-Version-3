package com.sanosysalvos.bff.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class BffController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/mascotas")
    public Object obtenerMascotas() {
        return restTemplate.getForObject("http://localhost:8081/api/mascotas", Object.class);
    }

    // ✅ VERSIÓN SIMPLE: Solo reenvía el JSON tal cual
    @PostMapping("/mascotas")
    public Object registrarMascota(@RequestBody Object mascota) {
        return restTemplate.postForObject("http://localhost:8081/api/mascotas", mascota, Object.class);
    }

    @GetMapping("/coincidencias")
    public Object obtenerCoincidencias() {
        return restTemplate.getForObject("http://localhost:8082/api/coincidencias", Object.class);
    }
}