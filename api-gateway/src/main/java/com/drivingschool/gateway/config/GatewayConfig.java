package com.drivingschool.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final ServiceUrlProperties serviceUrlProperties;

    public GatewayConfig(ServiceUrlProperties serviceUrlProperties) {
        this.serviceUrlProperties = serviceUrlProperties;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("student-service", r -> r
                        .path("/api/students/**")
                        .uri(serviceUrlProperties.getStudentBaseUrl()))
                .route("instructor-service", r -> r
                        .path("/api/instructors/**")
                        .uri(serviceUrlProperties.getInstructorBaseUrl()))
                .route("scheduling-service-lessons", r -> r
                        .path("/api/lessons/**")
                        .uri(serviceUrlProperties.getSchedulingBaseUrl()))
                .route("scheduling-service-courses", r -> r
                        .path("/api/courses/**")
                        .uri(serviceUrlProperties.getSchedulingBaseUrl()))
                .route("vehicle-service", r -> r
                        .path("/api/vehicles/**")
                        .uri(serviceUrlProperties.getVehicleBaseUrl()))
                .route("vehicle-service-maintenances", r -> r
                        .path("/api/maintenances/**")
                        .uri(serviceUrlProperties.getVehicleBaseUrl()))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri(serviceUrlProperties.getPaymentBaseUrl()))
                .build();
    }
}
