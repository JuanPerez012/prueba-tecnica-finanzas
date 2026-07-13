package com.pruebatecnica.clientesfinanzas.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Habilita CORS para que el frontend Vue (servido por Vite en desarrollo,
 * normalmente http://localhost:5173) pueda consumir la API REST de
 * Clientes, que corre en un origen distinto (http://localhost:8080).
 *
 * Copia este archivo dentro de tu paquete
 * com.pruebatecnica.clientesfinanzas.common.config del backend.
 * Ajusta los orígenes permitidos si despliegas el frontend en otra URL.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600);
    }
}
