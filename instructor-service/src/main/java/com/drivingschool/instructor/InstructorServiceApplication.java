package com.drivingschool.instructor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.drivingschool"})
@EnableJpaAuditing
public class InstructorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InstructorServiceApplication.class, args);
    }
}

