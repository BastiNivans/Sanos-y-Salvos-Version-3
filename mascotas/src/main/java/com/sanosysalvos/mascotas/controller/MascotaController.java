package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.model.Mascota;
import com.sanosysalvos.mascotas.repository.MascotaRepository;
import com.sanosysalvos.mascotas.service.GeocodificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "http://localhost:3000")
public class MascotaController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private GeocodificacionService geocodificacionService; // 🆕 Inyectar servicio de geocodificación

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public List<Mascota> obtenerTodas() {
        return mascotaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> obtenerPorId(@PathVariable Long id) {
        Optional<Mascota> mascota = mascotaRepository.findById(id);
        return mascota.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> registrarMascota(@RequestBody Map<String, Object> mascotaDTO) {
        try {
            // 1. Crear y guardar la mascota
            Mascota mascota = new Mascota();
            mascota.setEspecie((String) mascotaDTO.get("especie"));
            mascota.setRaza((String) mascotaDTO.get("raza"));
            mascota.setTipoReporte((String) mascotaDTO.get("tipoReporte"));
            mascota.setUbicacion((String) mascotaDTO.get("ubicacion"));
            
            // Manejar imágenes (si vienen en Base64)
            if (mascotaDTO.containsKey("imagenesBase64")) {
                List<String> imagenesBase64 = (List<String>) mascotaDTO.get("imagenesBase64");
                // Aquí deberías guardar las imágenes y obtener las URLs
                // Por ahora, dejamos esto como placeholder
                mascota.setImagenesUrls(""); // TODO: Implementar guardado de imágenes
            }
            
            // Guardar mascota en la BD
            Mascota mascotaGuardada = mascotaRepository.save(mascota);
            System.out.println("✅ Mascota guardada con ID: " + mascotaGuardada.getId());

            // 2. 🆕 GEOCODIFICAR LA DIRECCIÓN
            String ubicacionTexto = mascotaGuardada.getUbicacion();
            if (ubicacionTexto != null && !ubicacionTexto.isEmpty()) {
                System.out.println("🗺️ Geocodificando dirección: " + ubicacionTexto);
                
                GeocodificacionService.CoordenadasDTO coordenadas = 
                    geocodificacionService.geocodificarDireccion(ubicacionTexto);
                
                // 3. 🆕 ENVIAR COORDENADAS AL MICROSERVICIO DE GEOLOCALIZACIÓN (8083)
                try {
                    Map<String, Object> datosGeolocalizacion = new HashMap<>();
                    datosGeolocalizacion.put("idMascota", mascotaGuardada.getId());
                    datosGeolocalizacion.put("especie", mascotaGuardada.getEspecie());
                    datosGeolocalizacion.put("raza", mascotaGuardada.getRaza());
                    datosGeolocalizacion.put("tipoReporte", mascotaGuardada.getTipoReporte());
                    datosGeolocalizacion.put("latitud", coordenadas.getLatitud());
                    datosGeolocalizacion.put("longitud", coordenadas.getLongitud());
                    datosGeolocalizacion.put("direccionTexto", ubicacionTexto);
                    
                    // Llamar al microservicio de geolocalización
                    restTemplate.postForObject(
                        "http://localhost:8083/api/geolocalizacion/registrar",
                        datosGeolocalizacion,
                        Object.class
                    );
                    
                    System.out.println("✅ Ubicación registrada en geolocalización (lat: " + 
                        coordenadas.getLatitud() + ", lon: " + coordenadas.getLongitud() + ")");
                } catch (Exception e) {
                    System.err.println("⚠️ Error al registrar en geolocalización: " + e.getMessage());
                    // No fallar todo el proceso si falla la geolocalización
                }
            }
            
            return ResponseEntity.ok(mascotaGuardada);
            
        } catch (Exception e) {
            System.err.println("❌ Error al registrar mascota: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizarMascota(@PathVariable Long id, @RequestBody Mascota mascotaActualizada) {
        return mascotaRepository.findById(id)
            .map(mascota -> {
                mascota.setEspecie(mascotaActualizada.getEspecie());
                mascota.setRaza(mascotaActualizada.getRaza());
                mascota.setTipoReporte(mascotaActualizada.getTipoReporte());
                mascota.setUbicacion(mascotaActualizada.getUbicacion());
                mascota.setImagenesUrls(mascotaActualizada.getImagenesUrls());
                return ResponseEntity.ok(mascotaRepository.save(mascota));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMascota(@PathVariable Long id) {
        return mascotaRepository.findById(id)
            .map(mascota -> {
                mascotaRepository.delete(mascota);
                return ResponseEntity.ok().<Void>build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}