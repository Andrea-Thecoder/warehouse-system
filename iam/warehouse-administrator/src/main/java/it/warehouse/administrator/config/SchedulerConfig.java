package it.warehouse.administrator.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.ws.rs.DefaultValue;

@ConfigMapping(prefix = "scheduler")
public interface SchedulerConfig {



    @WithDefault("0 0 3 * * ?")
    String cron();
    @WithDefault("true")
    boolean enabled();
}
