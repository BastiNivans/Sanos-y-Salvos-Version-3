package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.model.Mascota;
import com.sanosysalvos.mascotas.repository.MascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaRepository mascotaRepository;

    // Endpoint para obtener todas las mascotas (GET)
    @GetMapping
    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }

    // Endpoint para registrar una nueva mascota (POST)
    @PostMapping
    public Mascota registrarMascota(@RequestBody Mascota mascota) {
        return mascotaRepository.save(mascota);
    }
}