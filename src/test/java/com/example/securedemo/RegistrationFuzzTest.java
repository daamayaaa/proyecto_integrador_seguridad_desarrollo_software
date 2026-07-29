package com.example.securedemo;

import com.code_intelligence.jazzer.junit.FuzzTest;
import com.example.securedemo.dto.UserRegistrationRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Prueba de FUZZ TESTING exigida por el proyecto: genera automaticamente miles
 * de combinaciones de username/email/password (incluyendo cadenas extremas,
 * caracteres de control, secuencias de escape SQL/NoSQL, unicode y entradas
 * vacias) para verificar que la capa de validacion nunca deje pasar una
 * excepcion no controlada ni un estado inconsistente.
 *
 * Esta clase se ejecuta con Jazzer (motor de fuzzing guiado por cobertura,
 * basado en libFuzzer) integrado como JUnit 5 extension.
 */
class RegistrationFuzzTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @FuzzTest
    void fuzzUserRegistrationValidation(String username, String email, String password) {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);

        try {
            Set<jakarta.validation.ConstraintViolation<UserRegistrationRequest>> violations =
                    validator.validate(request);

            // Invariante de seguridad: si la validacion pasa (0 violaciones),
            // los campos DEBEN cumplir las reglas de negocio esperadas.
            if (violations.isEmpty()) {
                if (password == null || password.length() < 8) {
                    fail("Fuzzing detecto bypass de la politica minima de contrasena");
                }
                if (username != null && !username.matches("^[A-Za-z0-9_.]+$")) {
                    fail("Fuzzing detecto bypass del patron permitido de username");
                }
            }
        } catch (ConstraintViolationException e) {
            // Comportamiento esperado y controlado: no es un hallazgo.
        } catch (RuntimeException unexpected) {
            // Cualquier otra excepcion no controlada SI es un hallazgo de seguridad:
            // indica que una entrada arbitraria puede tumbar el servicio (DoS)
            // o revelar informacion interna.
            fail("Excepcion no controlada ante entrada fuzz: " + unexpected);
        }
    }
}
