package com.sanosysalvos.bff.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Permiso directo para tu React
public class BffController {

    private final RestTemplate restTemplate = new RestTemplate();

    // Redirige las peticiones de ver mascotas al puerto 8081
    @GetMapping("/mascotas")
    public Object obtenerMascotas() {
        return restTemplate.getForObject("http://localhost:8081/api/mascotas", Object.class);
    }

    // Redirige las peticiones de guardar mascotas al puerto 8081
    @PostMapping("/mascotas")
    public Object registrarMascota(@RequestBody Object mascota) {
        return restTemplate.postForObject("http://localhost:8081/api/mascotas", mascota, Object.class);
    }

    // Redirige las peticiones de coincidencias al puerto 8082
    @GetMapping("/coincidencias")
    public Object obtenerCoincidencias() {
        return restTemplate.getForObject("http://localhost:8082/api/coincidencias", Object.class);
    }
}