package com.sanosysalvos.mascotas.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeocodificacionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    /**
     * Convierte una dirección de texto en coordenadas GPS (latitud/longitud)
     * usando la API gratuita de Nominatim (OpenStreetMap)
     */
    public CoordenadasDTO geocodificarDireccion(String direccion) {
        try {
            // Agregar "Chile" para mejorar la precisión
            String direccionCompleta = direccion + ", Chile";
            
            String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
                .queryParam("q", direccionCompleta)
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .toUriString();

            // Agregar header User-Agent (requisito de Nominatim)
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "SanosYSalvosApp/1.0 (tu-email@ejemplo.com)");
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            GeocodificacionResponse[] response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.GET, 
                entity, 
                GeocodificacionResponse[].class
            ).getBody();
            
            if (response != null && response.length > 0) {
                CoordenadasDTO coordenadas = new CoordenadasDTO();
                coordenadas.setLatitud(Double.parseDouble(response[0].lat));
                coordenadas.setLongitud(Double.parseDouble(response[0].lon));
                System.out.println("✅ Coordenadas obtenidas: " + coordenadas.getLatitud() + ", " + coordenadas.getLongitud());
                return coordenadas;
            }
            
            System.out.println("⚠️ No se encontraron coordenadas para: " + direccion);
            // Si no encuentra, devolver coordenadas por defecto (centro de Puente Alto)
            return obtenerCoordenadasPorDefecto();
            
        } catch (Exception e) {
            System.err.println("❌ Error al geocodificar: " + e.getMessage());
            return obtenerCoordenadasPorDefecto();
        }
    }

    /**
     * Coordenadas por defecto (centro de Puente Alto, Chile)
     */
    private CoordenadasDTO obtenerCoordenadasPorDefecto() {
        CoordenadasDTO coords = new CoordenadasDTO();
        coords.setLatitud(-33.6119);
        coords.setLongitud(-70.5746);
        return coords;
    }

    // Clase interna para la respuesta de la API de Nominatim
    private static class GeocodificacionResponse {
        public String lat;
        public String lon;
        public String display_name;
    }

    // DTO para retornar coordenadas
    public static class CoordenadasDTO {
        private Double latitud;
        private Double longitud;

        public Double getLatitud() { return latitud; }
        public void setLatitud(Double latitud) { this.latitud = latitud; }
        public Double getLongitud() { return longitud; }
        public void setLongitud(Double longitud) { this.longitud = longitud; }
    }
}