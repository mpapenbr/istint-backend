package de.mp.istint.server.config.data;

import java.util.List;

import lombok.Data;

@Data
public class CorsData {
    List<String> allowedOrigins;
    List<String> allowedMethods;
    List<String> allowedHeaders;
    boolean allowCredentials;
}
