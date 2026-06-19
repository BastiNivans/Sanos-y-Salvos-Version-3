package com.sanosysalvos.mascotas.factory;

import com.sanosysalvos.mascotas.model.Mascota;
import org.springframework.stereotype.Component;

@Component
public class MascotaFactory {
    
    public Mascota crearReporte(String tipo, String especie, String raza, String ubicacion) {
        if ("PERDIDA".equalsIgnoreCase(tipo) || "ENCONTRADA".equalsIgnoreCase(tipo)) {
            
            // 1. Creamos la mascota vacía
            Mascota nuevaMascota = new Mascota();
            
            // 2. Le asignamos los valores usando los setters
            nuevaMascota.setTipoReporte(tipo.toUpperCase());
            nuevaMascota.setEspecie(especie);
            nuevaMascota.setRaza(raza);
            nuevaMascota.setUbicacion(ubicacion);
            
            return nuevaMascota;
        }
        
        throw new IllegalArgumentException("Tipo de reporte no válido. Debe ser PERDIDA o ENCONTRADA.");
    }
}