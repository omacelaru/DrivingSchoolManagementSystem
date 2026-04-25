package com.drivingschool.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.services")
public class ServiceUrlProperties {
    private String studentBaseUrl = "http://localhost:8081";
    private String instructorBaseUrl = "http://localhost:8086";
    private String schedulingBaseUrl = "http://localhost:8082";
    private String vehicleBaseUrl = "http://localhost:8083";
    private String paymentBaseUrl = "http://localhost:8084";
}
