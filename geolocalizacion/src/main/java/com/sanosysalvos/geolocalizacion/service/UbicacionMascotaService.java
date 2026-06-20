package com.sanosysalvos.geolocalizacion.service;

import com.sanosysalvos.geolocalizacion.model.UbicacionMascota;
import com.sanosysalvos.geolocalizacion.repository.UbicacionMascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UbicacionMascotaService {

    @Autowired
    private UbicacionMascotaRepository repository;

    // 💾 Registrar ubicación de mascota
    public UbicacionMascota registrarUbicacion(Long idMascota, String especie, String raza, 
                                                String tipoReporte, Double latitud, 
                                                Double longitud, String direccionTexto) {
        
        // Verificar si ya existe una ubicación para esta mascota
        UbicacionMascota existente = repository.findByIdMascota(idMascota);
        if (existente != null) {
            // Actualizar ubicación existente
            existente.setLatitud(latitud);
            existente.setLongitud(longitud);
            existente.setDireccionTexto(direccionTexto);
            existente.setFechaRegistro(LocalDateTime.now());
            return repository.save(existente);
        }

        // Crear nueva ubicación
        UbicacionMascota ubicacion = new UbicacionMascota();
        ubicacion.setIdMascota(idMascota);
        ubicacion.setEspecie(especie);
        ubicacion.setRaza(raza);
        ubicacion.setTipoReporte(tipoReporte);
        ubicacion.setLatitud(latitud);
        ubicacion.setLongitud(longitud);
        ubicacion.setDireccionTexto(direccionTexto);
        ubicacion.setFechaRegistro(LocalDateTime.now());

        return repository.save(ubicacion);
    }

    // 🔍 Buscar mascotas cercanas a una coordenada
    public Map<String, Object> buscarCercanas(Double latitud, Double longitud, 
                                               Double radioKm, Long idMascotaExcluir) {
        List<UbicacionMascota> todas = repository.findAll();
        
        List<Map<String, Object>> cercanas = todas.stream()
            .filter(m -> !m.getIdMascota().equals(idMascotaExcluir))
            .map(m -> {
                Double distancia = calcularDistancia(latitud, longitud, m.getLatitud(), m.getLongitud());
                if (distancia <= radioKm) {
                    Map<String, Object> resultado = new HashMap<>();
                    resultado.put("mascota", m);
                    resultado.put("distancia_km", Math.round(distancia * 100.0) / 100.0);
                    return resultado;
                }
                return null;
            })
            .filter(r -> r != null)
            .collect(Collectors.toList());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("total", cercanas.size());
        respuesta.put("radio_km", radioKm);
        respuesta.put("mascotas", cercanas);
        
        return respuesta;
    }

    // 📏 Calcular distancia entre dos puntos (Fórmula de Haversine)
    public Double calcularDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        
        final int R = 6371; // Radio de la Tierra en km
        
        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distancia en kilómetros
    }

    // 🎯 Buscar coincidencias geolocalizadas (mascotas perdidas cerca de una encontrada)
    public List<Map<String, Object>> buscarCoincidenciasGeolocalizadas(Long idMascotaEncontrada, Double radioKm) {
        UbicacionMascota encontrada = repository.findByIdMascota(idMascotaEncontrada);
        if (encontrada == null) return List.of();
        
        List<UbicacionMascota> perdidas = repository.findByTipoReporte("PERDIDA");
        
        return perdidas.stream()
            .filter(p -> !p.getIdMascota().equals(idMascotaEncontrada))
            .map(p -> {
                Double distancia = calcularDistancia(
                    encontrada.getLatitud(), encontrada.getLongitud(),
                    p.getLatitud(), p.getLongitud()
                );
                if (distancia <= radioKm) {
                    Map<String, Object> resultado = new HashMap<>();
                    resultado.put("mascota", p);
                    resultado.put("distancia_km", Math.round(distancia * 100.0) / 100.0);
                    return resultado;
                }
                return null;
            })
            .filter(r -> r != null)
            .collect(Collectors.toList());
    }

    // 📍 Obtener todas las ubicaciones
    public List<UbicacionMascota> obtenerTodas() {
        return repository.findAll();
    }
}