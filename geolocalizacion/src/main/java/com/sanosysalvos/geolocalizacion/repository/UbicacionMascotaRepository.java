package com.sanosysalvos.geolocalizacion.repository;

import com.sanosysalvos.geolocalizacion.model.UbicacionMascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UbicacionMascotaRepository extends JpaRepository<UbicacionMascota, Long> {

    // Buscar por tipo de reporte
    List<UbicacionMascota> findByTipoReporte(String tipoReporte);
    
    // Buscar por ID de mascota
    UbicacionMascota findByIdMascota(Long idMascota);
    
    // Buscar mascotas en una comuna (por texto)
    @Query("SELECT u FROM UbicacionMascota u WHERE LOWER(u.direccionTexto) LIKE LOWER(CONCAT('%', :comuna, '%'))")
    List<UbicacionMascota> buscarPorComuna(@Param("comuna") String comuna);
}