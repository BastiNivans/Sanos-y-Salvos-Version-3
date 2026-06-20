package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.model.Mascota;
import com.sanosysalvos.mascotas.repository.MascotaRepository;
import com.sanosysalvos.mascotas.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "http://localhost:3000")
public class MascotaController {

    @Autowired
    private MascotaRepository mascotaRepository;
    
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }

    // ✅ RECIBE JSON NORMAL CON BASE64
    @PostMapping
    public Mascota registrarMascota(@RequestBody Map<String, Object> datos) {
        System.out.println("🐾 DATOS RECIBIDOS: " + datos);
        
        Mascota mascota = new Mascota();
        mascota.setEspecie((String) datos.get("especie"));
        mascota.setRaza((String) datos.get("raza"));
        mascota.setTipoReporte((String) datos.get("tipoReporte"));
        mascota.setUbicacion((String) datos.get("ubicacion"));
        
        // Procesar imágenes Base64
        List<String> imagenesBase64 = (List<String>) datos.get("imagenesBase64");
        if (imagenesBase64 != null && !imagenesBase64.isEmpty()) {
            try {
                List<String> urls = fileStorageService.guardarImagenesBase64(imagenesBase64);
                mascota.setImagenesUrls(String.join(",", urls));
                System.out.println("📸 Imágenes guardadas: " + urls);
            } catch (Exception e) {
                System.err.println("Error al guardar imágenes: " + e.getMessage());
            }
        }
        
        return mascotaRepository.save(mascota);
    }
}