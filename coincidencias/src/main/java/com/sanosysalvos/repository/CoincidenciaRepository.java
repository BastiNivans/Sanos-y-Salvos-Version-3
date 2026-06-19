package com.sanosysalvos.repository;

import com.sanosysalvos.model.Coincidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoincidenciaRepository extends JpaRepository<Coincidencia, Long> {
}