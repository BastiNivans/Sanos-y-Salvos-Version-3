package com.sanosysalvos.mascotas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanosysalvos.mascotas.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Este método mágico buscará en PostgreSQL si existe un usuario con ese correo
    Optional<Usuario> findByCorreo(String correo);
}
