package com.sanosysalvos.controller;

import com.sanosysalvos.model.Coincidencia;
import com.sanosysalvos.repository.CoincidenciaRepository;
import com.sanosysalvos.service.CoincidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coincidencias")
public class CoincidenciaController {

    @Autowired
    private CoincidenciaRepository coincidenciaRepository;

    @Autowired
    private CoincidenciaService coincidenciaService; // Aquí usamos el servicio que tiene tu Circuit Breaker

    @GetMapping
    public List<Coincidencia> listarCoincidencias() {
        return coincidenciaRepository.findAll();
    }

    // Este endpoint probará tu Patrón Circuit Breaker conectándose al otro microservicio
    @GetMapping("/test-circuit-breaker")
    public String probarCircuitBreaker() {
        return coincidenciaService.buscarCoincidencias();
    }
}