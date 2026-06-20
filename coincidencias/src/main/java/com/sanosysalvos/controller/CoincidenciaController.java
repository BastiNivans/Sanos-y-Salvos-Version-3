package com.sanosysalvos.controller;

import com.sanosysalvos.model.Coincidencia;
import com.sanosysalvos.repository.CoincidenciaRepository;
import com.sanosysalvos.service.CoincidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coincidencias")
@CrossOrigin(origins = "*") // 🌐 Evita problemas de CORS al conectar con React
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

    /**
     * 🛰️ NUEVO ENDPOINT DE BÚSQUEDA ESPACIAL (Fusión con PostGIS)
     * Permite que tu mapa de React busque reportes cruzados a menos de 3km a la redonda.
     */
  // REEMPLAZA EL MÉTODO COMPLETO buscarCoincidenciasEspatiales EN TU CONTROLADOR POR ESTE:
@GetMapping("/buscar")
public ResponseEntity<?> buscarCoincidenciasEspatiales(
        @RequestParam String lat,
        @RequestParam String lng,
        @RequestParam String especie,
        @RequestParam String tipoReporte) {
    
    try {
        String puntoWkt = "POINT(" + lng + " " + lat + ")";
        double radioMetros = 3000.0; // 3 Kilómetros
        
        List<Object[]> resultados = coincidenciaRepository.buscarMascotasCercanas(tipoReporte, especie, puntoWkt, radioMetros);
        List<Map<String, Object>> respuestaFinal = new ArrayList<>();
        
        for (Object[] columna : resultados) {
            Map<String, Object> jsonPet = new HashMap<>();
            jsonPet.put("id", columna[0]);
            jsonPet.put("especie", columna[1]);
            jsonPet.put("raza", columna[2]);
            jsonPet.put("tipoReporte", columna[3]);
            // Ya viene formateado como "lat,lng" desde la base de datos:
            jsonPet.put("ubicacion", columna[4] != null ? columna[4].toString() : null);
            
            respuestaFinal.add(jsonPet);
        }
        
        return ResponseEntity.ok(respuestaFinal);
        
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error en búsqueda espacial: " + e.getMessage());
    }
}
}