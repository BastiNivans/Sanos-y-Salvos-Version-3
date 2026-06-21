package com.sanosysalvos.mascotas.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
            
            // 🆕 Construir URL manualmente (más compatible)
            String direccionCodificada = URLEncoder.encode(direccionCompleta, StandardCharsets.UTF_8.toString());
            String url = NOMINATIM_URL + "?q=" + direccionCodificada + "&format=json&limit=1";

            System.out.println("🌐 Consultando Nominatim: " + url);

            // Hacer la petición
            GeocodificacionResponse[] response = restTemplate.getForObject(url, GeocodificacionResponse[].class);
            
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
            e.printStackTrace();
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