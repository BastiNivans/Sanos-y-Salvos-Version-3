package com.sanosysalvos.mascotas.factory;

import com.sanosysalvos.mascotas.model.Mascota;
import org.springframework.stereotype.Component;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

@Component
public class MascotaFactory {
    
    public Mascota crearReporte(String tipo, String especie, String raza, String ubicacion) {
        if ("PERDIDA".equalsIgnoreCase(tipo) || "ENCONTRADA".equalsIgnoreCase(tipo)) {
            
            // 1. Creamos la mascota vacía
            Mascota nuevaMascota = new Mascota();
            
            // 2. Le asignamos los valores simples usando los setters
            nuevaMascota.setTipoReporte(tipo.toUpperCase());
            nuevaMascota.setEspecie(especie);
            nuevaMascota.setRaza(raza);
            
            // 3. 🚀 CONVERSIÓN DE STRING A POINT (POSTGIS SRID 4326)
            if (ubicacion != null && !ubicacion.isBlank()) {
                try {
                    // Inicializamos la fábrica de geometrías espaciales con el estándar GPS internacional (4326)
                    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
                    
                    // Separamos el texto "latitud,longitud" por la coma
                    String[] partes = ubicacion.split(",");
                    double latitud = Double.parseDouble(partes[0].trim());
                    double longitud = Double.parseDouble(partes[1].trim());
                    
                    // ⚠️ IMPORTANTE EN GEOMETRÍA JTS: El orden de los ejes es (X, Y) -> (Longitud, Latitud)
                    Point puntoEspacial = geometryFactory.createPoint(new Coordinate(longitud, latitud));
                    
                    // Asignamos el objeto geométrico real
                    nuevaMascota.setUbicacion(puntoEspacial);
                    
                } catch (Exception e) {
                    throw new IllegalArgumentException("El formato de la ubicación debe ser numérico 'latitud,longitud'. Ejemplo: '-34.60, -58.38'");
                }
            } else {
                nuevaMascota.setUbicacion(null);
            }
            
            return nuevaMascota;
        }
        
        throw new IllegalArgumentException("Tipo de reporte no válido. Debe ser PERDIDA o ENCONTRADA.");
    }
}