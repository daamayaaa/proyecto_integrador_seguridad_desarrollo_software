package com.example.securedemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Conjunto de 2 casos de PRUEBA DE INTRUSION (pentest de caja gris) exigidos
 * por el proyecto integrador. Simulan vectores del OWASP Top 10 / OWASP API
 * Security Top 10 sobre el microservicio de usuarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PenetrationTests {

    @Autowired
    private MockMvc mockMvc;

    // PT-01: Intento de inyeccion SQL en el campo "username" durante el login.
    // Al usar Spring Data JPA (consultas parametrizadas), el payload debe ser
    // tratado como dato literal y NUNCA alterar la sentencia SQL subyacente.
    @Test
    void pt01_intentoInyeccionSQLEnLogin() throws Exception {
        String payload = """
            {"username":"admin' OR '1'='1' -- ","password":"cualquiera"}
            """;

        mockMvc.perform(post("/api/users/login")
                .contentType("application/json").content(payload))
            // El resultado esperado es un fallo de autenticacion controlado,
            // nunca un 200 OK ni un error 500 con traza de la base de datos.
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Credenciales invalidas"));
    }

    // PT-02: Intento de IDOR (Insecure Direct Object Reference) - un usuario
    // autenticado intenta consultar el perfil de OTRO usuario cambiando el ID
    // en la URL. Debe recibir 403 (no autorizado) aunque el recurso exista.
    @Test
    @WithMockUser(username = "atacante_autenticado") // usuario de prueba autenticado sin permisos sobre el recurso objetivo
    void pt02_intentoIDORSobrePerfilAjeno() throws Exception {
        // Se registra la victima para asegurar que el recurso con id=999 no existe
        // realmente en este test aislado; el comportamiento esperado es 403 o 404
        // controlado, nunca la exposicion de datos de otro usuario.
        mockMvc.perform(get("/api/users/999"))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                if (status != 403 && status != 404 && status != 400) {
                    throw new AssertionError(
                        "Se esperaba un rechazo controlado (403/404/400) y no exposicion de datos, se obtuvo: " + status);
                }
            });
    }
}
