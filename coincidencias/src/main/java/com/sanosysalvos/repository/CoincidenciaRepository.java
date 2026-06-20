package com.sanosysalvos.repository;

import com.sanosysalvos.model.Coincidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CoincidenciaRepository extends JpaRepository<Coincidencia, Long> {

    /**
     * 🚀 CONSULTA GEOESPACIAL OPTIMIZADA:
     * Extraemos latitud y longitud como texto plano directamente desde la base de datos
     * para evitar que Hibernate Spatial colapse al compilar.
     */
    @Query(value = "SELECT m.id, m.especie, m.raza, m.tipo_reporte, " +
                   "concat(ST_Y(m.ubicacion), ',', ST_X(m.ubicacion)) as lat_lng " +
                   "FROM mascota m WHERE " +
                   "m.tipo_reporte != :tipoReporte AND " +
                   "m.especie = :especie AND " +
                   "ST_DWithin(m.ubicacion, ST_GeomFromText(:puntoWkt, 4326), :radioMetros)", 
           nativeQuery = true)
    List<Object[]> buscarMascotasCercanas(
        @Param("tipoReporte") String tipoReporte,
        @Param("especie") String especie,
        @Param("puntoWkt") String puntoWkt,
        @Param("radioMetros") double radioMetros
    );
}