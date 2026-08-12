package com.cinemahub.cinemahub.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cinemahubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CinemaHub API")
                        .version("v1")
                        .description("API REST para gestión de cines, películas, funciones, reservas, "
                                + "órdenes de compra, dulcería, promociones, membresías y notificaciones."));
    }
}