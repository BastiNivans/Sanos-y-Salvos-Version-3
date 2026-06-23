package com.sanosysalvos.service;

import com.sanosysalvos.model.Coincidencia;
import com.sanosysalvos.repository.CoincidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CoincidenciaService {

    @Autowired
    private CoincidenciaRepository coincidenciaRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String URL_MICROSERVICIO_MASCOTAS = "http://localhost:8081/api/mascotas";

    // Método para el test del Circuit Breaker
    public String buscarCoincidencias() {
        return "Servicio de coincidencias activo - Circuit Breaker funcionando";
    }

    // 🆕 MÉTODO PRINCIPAL: Calcular y guardar coincidencias (SIN DUPLICADOS)
    public List<Coincidencia> calcularYGuardarCoincidencias(Long mascotaId) {
        System.out.println("🔍 Calculando coincidencias para mascota ID: " + mascotaId);
        
        try {
            // 1. Obtener todas las mascotas del microservicio de mascotas
            List<Map<String, Object>> todasLasMascotas = obtenerTodasLasMascotas();
            
            // 2. Encontrar la mascota objetivo
            Map<String, Object> mascotaObjetivo = todasLasMascotas.stream()
                .filter(m -> ((Integer) m.get("id")).longValue() == mascotaId)
                .findFirst()
                .orElse(null);
            
            if (mascotaObjetivo == null) {
                System.out.println("❌ Mascota no encontrada: " + mascotaId);
                return new ArrayList<>();
            }
            
            // 3. Calcular similitud con todas las demás mascotas
            List<Coincidencia> nuevasCoincidencias = new ArrayList<>();
            
            for (Map<String, Object> otraMascota : todasLasMascotas) {
                Long otraMascotaId = ((Integer) otraMascota.get("id")).longValue();
                
                // No comparar consigo misma
                if (otraMascotaId.equals(mascotaId)) {
                    continue;
                }
                
                // Calcular porcentaje de similitud
                Double similitud = calcularSimilitud(mascotaObjetivo, otraMascota);
                
                // Solo procesar si hay más del 40% de similitud
                if (similitud >= 40.0) {
                    
                    // ️ VERIFICACIÓN ANTI-DUPLICADOS:
                    // Revisamos si ya existe la coincidencia en CUALQUIER orden (A con B, o B con A)
                    boolean yaExiste1 = coincidenciaRepository.existsByIdMascotaPerdidaAndIdMascotaEncontrada(mascotaId, otraMascotaId);
                    boolean yaExiste2 = coincidenciaRepository.existsByIdMascotaPerdidaAndIdMascotaEncontrada(otraMascotaId, mascotaId);

                    if (!yaExiste1 && !yaExiste2) {
                        // Si NO existe, la creamos y guardamos
                        Coincidencia coincidencia = new Coincidencia();
                        coincidencia.setIdMascotaPerdida(mascotaId);
                        coincidencia.setIdMascotaEncontrada(otraMascotaId);
                        coincidencia.setNivelSimilitud(similitud);
                        coincidencia.setFechaAnalisis(LocalDateTime.now());
                        
                        nuevasCoincidencias.add(coincidencia);
                    } else {
                        // Si ya existe, la ignoramos para no duplicar
                        System.out.println("⚠️ La coincidencia entre mascota " + mascotaId + " y " + otraMascotaId + " ya existía. Se ignora.");
                    }
                }
            }
            
            // 4. Guardar todas las coincidencias nuevas en la base de datos
            if (!nuevasCoincidencias.isEmpty()) {
                coincidenciaRepository.saveAll(nuevasCoincidencias);
                System.out.println("✅ Se guardaron " + nuevasCoincidencias.size() + " coincidencias nuevas");
            } else {
                System.out.println("⚠️ No se encontraron coincidencias nuevas (o ya existían)");
            }
            
            return nuevasCoincidencias;
            
        } catch (Exception e) {
            System.err.println("❌ Error al calcular coincidencias: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    //  MÉTODO: Obtener todas las mascotas del microservicio
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> obtenerTodasLasMascotas() {
        try {
            Object response = restTemplate.getForObject(URL_MICROSERVICIO_MASCOTAS, Object.class);
            if (response instanceof List) {
                return (List<Map<String, Object>>) response;
            }
        } catch (Exception e) {
            System.err.println("Error al obtener mascotas: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    //  MÉTODO: Calcular similitud entre dos mascotas
    @SuppressWarnings("unchecked")
    private Double calcularSimilitud(Map<String, Object> mascota1, Map<String, Object> mascota2) {
        double puntaje = 0.0;
        double maxPuntaje = 100.0;
        
        // Obtener atributos (con manejo de null)
        String especie1 = (String) mascota1.getOrDefault("especie", "");
        String especie2 = (String) mascota2.getOrDefault("especie", "");
        String raza1 = (String) mascota1.getOrDefault("raza", "");
        String raza2 = (String) mascota2.getOrDefault("raza", "");
        String ubicacion1 = (String) mascota1.getOrDefault("ubicacion", "");
        String ubicacion2 = (String) mascota2.getOrDefault("ubicacion", "");
        String tipo1 = (String) mascota1.getOrDefault("tipoReporte", "");
        String tipo2 = (String) mascota2.getOrDefault("tipoReporte", "");
        
        // Comparar especie (40 puntos)
        if (especie1 != null && especie2 != null) {
            String especie1Simple = especie1.toLowerCase().split(" ")[0];
            String especie2Simple = especie2.toLowerCase().split(" ")[0];
            
            if (especie1Simple.equals(especie2Simple) || 
                especie1Simple.contains(especie2Simple) || 
                especie2Simple.contains(especie1Simple)) {
                puntaje += 40;
            }
        }
        
        // Comparar raza (30 puntos)
        if (raza1 != null && raza2 != null && !raza1.isEmpty() && !raza2.isEmpty()) {
            if (raza1.toLowerCase().equals(raza2.toLowerCase())) {
                puntaje += 30;
            } else if (raza1.toLowerCase().contains(raza2.toLowerCase()) || 
                      raza2.toLowerCase().contains(raza1.toLowerCase())) {
                puntaje += 20;
            } else if (raza1.toLowerCase().equals("sin raza") || 
                      raza2.toLowerCase().equals("sin raza")) {
                puntaje += 10;
            }
        }
        
        // Comparar ubicación (20 puntos)
        if (ubicacion1 != null && ubicacion2 != null) {
            String ubicacion1Simple = ubicacion1.toLowerCase().split(" ")[0];
            String ubicacion2Simple = ubicacion2.toLowerCase().split(" ")[0];
            
            if (ubicacion1Simple.equals(ubicacion2Simple) || 
                ubicacion1.toLowerCase().contains(ubicacion2.toLowerCase()) || 
                ubicacion2.toLowerCase().contains(ubicacion1.toLowerCase())) {
                puntaje += 20;
            }
        }
        
        // Comparar tipo de reporte (10 puntos) - Deben ser complementarios
        if ((tipo1 != null && tipo2 != null) &&
            ((tipo1.equals("PERDIDA") && tipo2.equals("ENCONTRADA")) ||
             (tipo1.equals("ENCONTRADA") && tipo2.equals("PERDIDA")))) {
            puntaje += 10;
        }
        
        return Math.round((puntaje / maxPuntaje) * 100.0 * 10.0) / 10.0; // Redondear a 1 decimal
    }
}