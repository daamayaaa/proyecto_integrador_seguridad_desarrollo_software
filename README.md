# secure-demo — Proyecto integrador de verificación y pruebas de seguridad

Microservicio Spring Boot (Java 17) usado como caso práctico para el proyecto
integrador de **verificación, pruebas de seguridad, fuzzing y detección de
vulnerabilidades** sobre el software de una organización (caso: "TechCommerce
S.A.S.", API de gestión de usuarios).

## Estructura relevante

```
src/main/java/.../model         -> Entidad User (JPA)
src/main/java/.../repository    -> Acceso a datos con consultas parametrizadas
src/main/java/.../dto           -> Validación de entrada (Bean Validation)
src/main/java/.../service       -> Lógica de negocio, hashing BCrypt, control anti-IDOR
src/main/java/.../security      -> Spring Security (BCrypt, cabeceras, stateless)
src/main/java/.../exception     -> Manejo centralizado de errores (sin fuga de info)
src/test/java/.../UserFunctionalTests.java   -> 5 casos de prueba FUNCIONALES
src/test/java/.../PenetrationTests.java      -> 2 casos de prueba de INTRUSIÓN
src/test/java/.../RegistrationFuzzTest.java  -> Prueba de FUZZING (Jazzer)
```

## Cómo ejecutar

### 1. Pruebas funcionales y de intrusión (JUnit + MockMvc)
```bash
mvn test -Dtest=UserFunctionalTests,PenetrationTests
```

### 2. Fuzz testing (Jazzer)
```bash
mvn test -Dtest=RegistrationFuzzTest
```
Jazzer genera automáticamente miles de combinaciones de entrada (cadenas
vacías, caracteres de control, payloads tipo `' OR '1'='1`, unicode, cadenas
extremadamente largas, etc.) guiándose por la cobertura de código para
maximizar la exploración de rutas del validador y del servicio.

### 3. Análisis estático de seguridad — SAST (SpotBugs + Find-Sec-Bugs)
```bash
mvn spotbugs:check
```
Detecta patrones inseguros en el código fuente (p. ej. concatenación de SQL,
uso de algoritmos criptográficos débiles, manejo inseguro de sesiones) antes
de compilar el artefacto final.

### 4. Análisis de composición de software — SCA (OWASP Dependency-Check)
```bash
mvn org.owasp:dependency-check-maven:check
```
Compara cada dependencia declarada en `pom.xml` contra la base de datos
NVD (CVE) y falla el build si aparece una vulnerabilidad con CVSS ≥ 7.

### 5. Análisis dinámico — DAST (recomendado, fuera de Maven)
Con la aplicación en ejecución (`mvn spring-boot:run`), ejecutar un *baseline
scan* con OWASP ZAP contra `http://localhost:8080` (ver recomendación de
herramienta en el informe del proyecto).

## Resumen de hallazgos esperados (referencia para el informe)

| # | Prueba | Resultado esperado | Vulnerabilidad relacionada (OWASP) |
|---|--------|---------------------|-------------------------------------|
| PT-01 | Inyección SQL en login | 400, credenciales inválidas, sin 500 ni datos filtrados | A03:2021 Injection |
| PT-02 | IDOR sobre `/api/users/{id}` | 403/404, nunca datos de otro usuario | API1:2023 BOLA |
| Fuzzing | Entradas aleatorias al validador de registro | Nunca excepción no controlada | A04/A05:2021 Diseño/Config inseguros |
