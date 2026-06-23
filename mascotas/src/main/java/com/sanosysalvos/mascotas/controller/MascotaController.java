package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.model.Mascota;
import com.sanosysalvos.mascotas.repository.MascotaRepository;
import com.sanosysalvos.mascotas.service.GeocodificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
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
    private GeocodificacionService geocodificacionService;

    private final RestTemplate restTemplate = new RestTemplate();
    
    // 🆕 Carpeta donde se guardarán las imágenes
    private static final String UPLOAD_DIR = "uploads/";

    public MascotaController() {
        // Crear carpeta de uploads si no existe
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println("📁 Carpeta uploads creada: " + uploadDir.getAbsolutePath());
        }
    }

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
            System.out.println("📥 Recibiendo mascota...");
            System.out.println("📥 Datos: " + mascotaDTO.keySet());
            
            Mascota mascota = new Mascota();
            mascota.setEspecie((String) mascotaDTO.get("especie"));
            mascota.setRaza((String) mascotaDTO.get("raza"));
            mascota.setTipoReporte((String) mascotaDTO.get("tipoReporte"));
            mascota.setUbicacion((String) mascotaDTO.get("ubicacion"));
            
            // 🆕 GUARDAR IMÁGENES
            if (mascotaDTO.containsKey("imagenesBase64")) {
                List<String> imagenesBase64 = (List<String>) mascotaDTO.get("imagenesBase64");
                System.out.println("📸 Cantidad de imágenes: " + imagenesBase64.size());
                
                StringBuilder imagenesUrls = new StringBuilder();
                
                for (int i = 0; i < imagenesBase64.size(); i++) {
                    String base64 = imagenesBase64.get(i);
                    System.out.println("📸 Procesando imagen " + (i+1) + "...");
                    
                    // Eliminar el prefijo "data:image/jpeg;base64," si existe
                    if (base64.contains(",")) {
                        base64 = base64.split(",")[1];
                    }
                    
                    // Decodificar Base64 a bytes
                    byte[] imageBytes = Base64.getDecoder().decode(base64);
                    
                    // Generar nombre único para la imagen
                    String fileName = "mascota_" + System.currentTimeMillis() + "_" + i + ".jpg";
                    String filePath = UPLOAD_DIR + fileName;
                    
                    // Guardar archivo en el servidor
                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        fos.write(imageBytes);
                        System.out.println("✅ Imagen guardada: " + filePath);
                    } catch (Exception e) {
                        System.err.println("❌ Error al guardar imagen: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // Agregar URL a la lista (separada por comas)
                    if (imagenesUrls.length() > 0) {
                        imagenesUrls.append(",");
                    }
                    imagenesUrls.append("/uploads/" + fileName);
                }
                
                mascota.setImagenesUrls(imagenesUrls.toString());
                System.out.println("📸 URLs guardadas: " + mascota.getImagenesUrls());
            } else {
                System.out.println("⚠️ No se recibieron imágenes");
                mascota.setImagenesUrls("");
            }
            
            // Guardar mascota en la BD
            Mascota mascotaGuardada = mascotaRepository.save(mascota);
            System.out.println("✅ Mascota guardada con ID: " + mascotaGuardada.getId());

            // 🗺️ GEOCODIFICAR LA DIRECCIÓN
            String ubicacionTexto = mascotaGuardada.getUbicacion();
            if (ubicacionTexto != null && !ubicacionTexto.isEmpty()) {
                System.out.println("🗺️ Geocodificando dirección: " + ubicacionTexto);
                
                GeocodificacionService.CoordenadasDTO coordenadas = 
                    geocodificacionService.geocodificarDireccion(ubicacionTexto);
                
                // 🗺️ ENVIAR COORDENADAS AL MICROSERVICIO DE GEOLOCALIZACIÓN (8083)
                try {
                    Map<String, Object> datosGeolocalizacion = new HashMap<>();
                    datosGeolocalizacion.put("idMascota", mascotaGuardada.getId());
                    datosGeolocalizacion.put("especie", mascotaGuardada.getEspecie());
                    datosGeolocalizacion.put("raza", mascotaGuardada.getRaza());
                    datosGeolocalizacion.put("tipoReporte", mascotaGuardada.getTipoReporte());
                    datosGeolocalizacion.put("latitud", coordenadas.getLatitud());
                    datosGeolocalizacion.put("longitud", coordenadas.getLongitud());
                    datosGeolocalizacion.put("direccionTexto", ubicacionTexto);
                    
                    restTemplate.postForObject(
                        "http://localhost:8083/api/geolocalizacion/registrar",
                        datosGeolocalizacion,
                        Object.class
                    );
                    
                    System.out.println("✅ Ubicación registrada en geolocalización (lat: " + 
                        coordenadas.getLatitud() + ", lon: " + coordenadas.getLongitud() + ")");
                } catch (Exception e) {
                    System.err.println("⚠️ Error al registrar en geolocalización: " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(mascotaGuardada);
            
        } catch (Exception e) {
            System.err.println("❌ Error al registrar mascota: " + e.getMessage());
            e.printStackTrace();
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