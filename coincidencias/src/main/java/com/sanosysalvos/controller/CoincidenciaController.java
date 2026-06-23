package com.sanosysalvos.controller;

import com.sanosysalvos.model.Coincidencia;
import com.sanosysalvos.repository.CoincidenciaRepository;
import com.sanosysalvos.service.CoincidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coincidencias")
@CrossOrigin(origins = "http://localhost:3000")
public class CoincidenciaController {

    @Autowired
    private CoincidenciaRepository coincidenciaRepository;

    @Autowired
    private CoincidenciaService coincidenciaService;

    @GetMapping
    public List<Coincidencia> listarCoincidencias() {
        return coincidenciaRepository.findAll();
    }

    @GetMapping("/test-circuit-breaker")
    public String probarCircuitBreaker() {
        return coincidenciaService.buscarCoincidencias();
    }

    // 🆕 Calcular coincidencias para una mascota específica
    @GetMapping("/calcular/{mascotaId}")
    public List<Coincidencia> calcularCoincidenciasParaMascota(@PathVariable Long mascotaId) {
        return coincidenciaService.calcularYGuardarCoincidencias(mascotaId);
    }

    // 🆕 Obtener coincidencias de una mascota
    @GetMapping("/mascota/{mascotaId}")
    public List<Coincidencia> obtenerCoincidenciasDeMascota(@PathVariable Long mascotaId) {
        return coincidenciaRepository.findAll()
            .stream()
            .filter(c -> c.getIdMascotaPerdida().equals(mascotaId) || 
                        c.getIdMascotaEncontrada().equals(mascotaId))
            .toList();
    }
}