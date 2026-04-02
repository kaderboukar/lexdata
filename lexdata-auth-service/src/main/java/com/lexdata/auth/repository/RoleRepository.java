package com.lexdata.auth.repository;

import com.lexdata.auth.models.ERole;
import com.lexdata.auth.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    // Utilisation de Optional pour éviter les NullPointerException
    // Si la base est mal initialisée, le code nous le dira proprement
    Optional<Role> findByName(ERole name);
}