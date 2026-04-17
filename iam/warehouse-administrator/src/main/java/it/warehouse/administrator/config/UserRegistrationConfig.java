package it.warehouse.administrator.config;

import io.smallrye.config.ConfigMapping;
import jakarta.ws.rs.DefaultValue;

@ConfigMapping(prefix = "user-registration")
public interface UserRegistrationConfig {

    @DefaultValue("7")
    int expireInDays();
}
