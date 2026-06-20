package com.sanosysalvos.geolocalizacion.controller;

import com.sanosysalvos.geolocalizacion.model.UbicacionMascota;
import com.sanosysalvos.geolocalizacion.service.UbicacionMascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geolocalizacion")
@CrossOrigin(origins = "http://localhost:3000")
public class UbicacionMascotaController {

    @Autowired
    private UbicacionMascotaService service;

    // 💾 Registrar ubicación
    @PostMapping("/registrar")
    public ResponseEntity<UbicacionMascota> registrarUbicacion(
            @RequestParam Long idMascota,
            @RequestParam String especie,
            @RequestParam String raza,
            @RequestParam String tipoReporte,
            @RequestParam Double latitud,
            @RequestParam Double longitud,
            @RequestParam(required = false) String direccionTexto) {
        
        UbicacionMascota ubicacion = service.registrarUbicacion(
            idMascota, especie, raza, tipoReporte, 
            latitud, longitud, direccionTexto
        );
        return ResponseEntity.ok(ubicacion);
    }

    // 🔍 Buscar mascotas cercanas a una coordenada
    @GetMapping("/cercanas")
    public ResponseEntity<Map<String, Object>> buscarCercanas(
            @RequestParam Double latitud,
            @RequestParam Double longitud,
            @RequestParam(defaultValue = "1.0") Double radioKm,
            @RequestParam(required = false) Long idMascotaExcluir) {
        
        return ResponseEntity.ok(service.buscarCercanas(latitud, longitud, radioKm, idMascotaExcluir));
    }

    // 📏 Calcular distancia entre dos mascotas
    @GetMapping("/distancia")
    public ResponseEntity<Double> calcularDistancia(
            @RequestParam Long idMascota1,
            @RequestParam Long idMascota2) {
        
        UbicacionMascota m1 = service.obtenerTodas().stream()
            .filter(m -> m.getIdMascota().equals(idMascota1))
            .findFirst().orElse(null);
        UbicacionMascota m2 = service.obtenerTodas().stream()
            .filter(m -> m.getIdMascota().equals(idMascota2))
            .findFirst().orElse(null);
        
        if (m1 == null || m2 == null) {
            return ResponseEntity.notFound().build();
        }
        
        Double distancia = service.calcularDistancia(
            m1.getLatitud(), m1.getLongitud(),
            m2.getLatitud(), m2.getLongitud()
        );
        
        return ResponseEntity.ok(Math.round(distancia * 100.0) / 100.0);
    }

    // 🎯 Buscar coincidencias geolocalizadas
    @GetMapping("/coincidencias/{idMascota}")
    public ResponseEntity<List<Map<String, Object>>> buscarCoincidencias(
            @PathVariable Long idMascota,
            @RequestParam(defaultValue = "2.0") Double radioKm) {
        
        return ResponseEntity.ok(service.buscarCoincidenciasGeolocalizadas(idMascota, radioKm));
    }

    // 📍 Obtener todas las ubicaciones (para el mapa)
    @GetMapping("/todas")
    public ResponseEntity<List<UbicacionMascota>> obtenerTodas() {
        return ResponseEntity.ok(service.obtenerTodas());
    }
}