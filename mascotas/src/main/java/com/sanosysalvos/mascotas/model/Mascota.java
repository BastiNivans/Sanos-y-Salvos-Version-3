package com.sanosysalvos.mascotas.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point; 
import com.fasterxml.jackson.annotation.JsonIgnore; // 🚀 IMPORTANTE
import com.fasterxml.jackson.annotation.JsonProperty; // 🚀 IMPORTANTE

@Entity
@Table(name = "mascota")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String especie;
    private String raza;
    
    @Column(name = "tipo_reporte")
    private String tipoReporte;
    
    @Column(columnDefinition = "geometry(Point, 4326)")
    @JsonIgnore // 🚀 Crucial: Evita que Jackson intente procesar el Point directamente y explote
    private Point ubicacion;

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    
    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }
    
    public Point getUbicacion() { return ubicacion; }
    public void setUbicacion(Point ubicacion) { this.ubicacion = ubicacion; }

    // 🚀 TRUCO CLAVE: Este método engaña a Jackson. El JSON final tendrá una propiedad "ubicacion" 
    // pero con el formato de texto limpio "latitud, longitud" que tu frontend maneja perfectamente.
    @JsonProperty("ubicacion")
    public String getUbicacionFormateada() {
        if (this.ubicacion == null) return null;
        // JTS almacena como (X, Y) -> (Longitud, Latitud). 
        // Lo extraemos al revés para que vuelva como "latitud,longitud"
        return this.ubicacion.getY() + "," + this.ubicacion.getX();
    }
}