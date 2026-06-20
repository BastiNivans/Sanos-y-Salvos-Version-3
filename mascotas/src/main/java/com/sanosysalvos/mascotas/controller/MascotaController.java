package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.factory.MascotaFactory; // 🚀 Importamos tu Factory
import com.sanosysalvos.mascotas.model.Mascota;
import com.sanosysalvos.mascotas.model.MascotaDTO;
import com.sanosysalvos.mascotas.repository.MascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Agregado para manejo seguro de errores
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "*") 
public class MascotaController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MascotaFactory mascotaFactory; // 🚀 Inyectamos tu Factory corregida

    // Endpoint para obtener todas las mascotas (GET)
    @GetMapping
    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }

    // Endpoint para registrar una nueva mascota (POST) - Conectado a la Factory
    @PostMapping
    public ResponseEntity<?> registrarMascota(@RequestBody MascotaDTO dto) {
        try {
            // 🚀 Usamos tu factory pasándole el String crudo de la ubicación que viene del Frontend/BFF
            Mascota mascota = mascotaFactory.crearReporte(
                dto.getTipoReporte(),
                dto.getEspecie(),
                dto.getRaza(),
                dto.getUbicacion() // Cambiado para recibir el String de coordenadas "lat,long"
            );

            // Guarda en la base de datos de manera normal
            Mascota guardada = mascotaRepository.save(mascota);
            return ResponseEntity.ok(guardada);

        } catch (IllegalArgumentException e) {
            // Si el formato de la latitud/longitud está mal, responde un error limpio 400
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Si hay un error de base de datos, nos mostrará exactamente qué pasó en lugar de un 500 genérico
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }
}