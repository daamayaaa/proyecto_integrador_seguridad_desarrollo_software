package com.example.securedemo;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Conjunto de 5 casos de prueba FUNCIONALES exigidos por el proyecto integrador.
 * Cada caso valida un comportamiento esperado del software bajo prueba
 * (organizacion ficticia: "TechCommerce S.A.S." - microservicio de usuarios).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserFunctionalTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // CP-F01: Registro exitoso con datos validos
    @Test
    void cp_f01_registroExitoso() throws Exception {
        String body = """
            {"username":"ana_perez","email":"ana@correo.com","password":"Segura#123"}
            """;
        mockMvc.perform(post("/api/users/register")
                .contentType("application/json").content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("ana_perez"));
    }

    // CP-F02: Rechazo de registro con correo con formato invalido
    @Test
    void cp_f02_registroRechazadoPorEmailInvalido() throws Exception {
        String body = """
            {"username":"luis01","email":"correo-no-valido","password":"Segura#123"}
            """;
        mockMvc.perform(post("/api/users/register")
                .contentType("application/json").content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    // CP-F03: Rechazo de registro con contrasena que no cumple la politica de complejidad
    @Test
    void cp_f03_registroRechazadoPorContrasenaDebil() throws Exception {
        String body = """
            {"username":"carla02","email":"carla@correo.com","password":"12345678"}
            """;
        mockMvc.perform(post("/api/users/register")
                .contentType("application/json").content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    // CP-F04: Inicio de sesion exitoso con credenciales correctas
    @Test
    void cp_f04_loginExitoso() throws Exception {
        String registro = """
            {"username":"mario03","email":"mario@correo.com","password":"Clave#456"}
            """;
        mockMvc.perform(post("/api/users/register")
                .contentType("application/json").content(registro))
            .andExpect(status().isCreated());

        String login = """
            {"username":"mario03","password":"Clave#456"}
            """;
        mockMvc.perform(post("/api/users/login")
                .contentType("application/json").content(login))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("AUTHENTICATED"));
    }

    // CP-F05: Acceso denegado a un recurso protegido sin autenticacion (401)
    @Test
    void cp_f05_accesoDenegadoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isUnauthorized());
    }
}
