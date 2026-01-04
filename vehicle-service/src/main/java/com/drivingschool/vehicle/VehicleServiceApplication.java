package com.drivingschool.vehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.drivingschool"})
@EnableJpaAuditing
@EnableCaching
@EnableFeignClients
public class VehicleServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VehicleServiceApplication.class, args);
    }
}

