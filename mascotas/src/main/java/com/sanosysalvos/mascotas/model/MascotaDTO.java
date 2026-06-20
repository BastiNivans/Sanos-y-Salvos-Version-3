package com.sanosysalvos.mascotas.model;

public class MascotaDTO {
    private String especie;
    private String raza;
    private String tipoReporte;
    private String ubicacion; // 🚀 Cambiado a String para recibir el texto "lat,long" del mapa

    // --- GETTERS Y SETTERS ---
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }

    public String getUbicacion() { return ubicacion; } // 🚀 Esto arregla el error de compilación del Controller
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
}