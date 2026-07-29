package com.example.securedemo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegistrationRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 40, message = "El usuario debe tener entre 4 y 40 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_.]+$", message = "El usuario contiene caracteres no permitidos")
    private String username;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato de correo no es valido")
    @Size(max = 120)
    private String email;

    // Se exige complejidad minima para reducir riesgo de fuerza bruta / diccionario
    @NotBlank(message = "La contrasena es obligatoria")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#.]).{8,64}$",
        message = "La contrasena debe tener min. 8 caracteres, mayuscula, minuscula, numero y simbolo"
    )
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
