package com.drivingschool.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("student-service", r -> r
                        .path("/api/students/**")
                        .uri("http://localhost:8081"))
                .route("instructor-service", r -> r
                        .path("/api/instructors/**")
                        .uri("http://localhost:8082"))
                .route("scheduling-service", r -> r
                        .path("/api/lessons/**")
                        .uri("http://localhost:8082"))
                .route("vehicle-service", r -> r
                        .path("/api/vehicles/**")
                        .uri("http://localhost:8083"))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("http://localhost:8084"))
                .build();
    }
}

