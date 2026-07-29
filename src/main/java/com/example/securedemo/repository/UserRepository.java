package com.example.securedemo.repository;

import com.example.securedemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Metodo derivado -> Spring Data genera una consulta parametrizada (PreparedStatement).
    // NUNCA se debe reemplazar esto por concatenacion manual de Strings + @Query nativo sin parametros.
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
