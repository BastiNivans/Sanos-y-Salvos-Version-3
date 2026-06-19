package com.sanosysalvos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coincidencia")
public class Coincidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_mascota_perdida")
    private Long idMascotaPerdida;

    @Column(name = "id_mascota_encontrada")
    private Long idMascotaEncontrada;

    @Column(name = "nivel_similitud")
    private Double nivelSimilitud; // Para guardar el % de coincidencia

    @Column(name = "fecha_analisis")
    private LocalDateTime fechaAnalisis;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdMascotaPerdida() { return idMascotaPerdida; }
    public void setIdMascotaPerdida(Long idMascotaPerdida) { this.idMascotaPerdida = idMascotaPerdida; }
    public Long getIdMascotaEncontrada() { return idMascotaEncontrada; }
    public void setIdMascotaEncontrada(Long idMascotaEncontrada) { this.idMascotaEncontrada = idMascotaEncontrada; }
    public Double getNivelSimilitud() { return nivelSimilitud; }
    public void setNivelSimilitud(Double nivelSimilitud) { this.nivelSimilitud = nivelSimilitud; }
    public LocalDateTime getFechaAnalisis() { return fechaAnalisis; }
    public void setFechaAnalisis(LocalDateTime fechaAnalisis) { this.fechaAnalisis = fechaAnalisis; }
}