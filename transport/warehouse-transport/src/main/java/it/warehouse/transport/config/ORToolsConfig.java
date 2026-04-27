package it.warehouse.transport.config;

import com.google.ortools.Loader;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ORToolsConfig {
    public ORToolsConfig() {
        Loader.loadNativeLibraries();
    }
}
