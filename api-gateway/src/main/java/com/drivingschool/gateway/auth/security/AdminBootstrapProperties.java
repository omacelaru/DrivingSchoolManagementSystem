package com.drivingschool.gateway.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public class AdminBootstrapProperties {
    /**
     * Enables one-time bootstrap flow for initial admin account.
     * Must remain false by default in source control.
     */
    private boolean enabled = false;

    /**
     * Bootstrap admin email, usually provided via BOOTSTRAP_ADMIN_EMAIL env var.
     */
    private String email = "";

    /**
     * Bootstrap admin password, usually provided via BOOTSTRAP_ADMIN_PASSWORD env var.
     */
    private String password = "";
}
