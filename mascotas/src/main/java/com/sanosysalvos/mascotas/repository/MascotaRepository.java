package com.sanosysalvos.mascotas.repository;

import com.sanosysalvos.mascotas.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    // Solo con hacer esto, ya tienes los métodos para guardar, borrar y buscar mascotas en PostgreSQL
}