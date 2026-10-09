package org.example.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Gateway {

    @Bean
    public RouteLocator configureRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p.path("/api/games/**").uri("http://localhost:8081"))
                .route(p -> p.path("/api/library/**").uri("http://localhost:8082"))
                .build();
    }
}
