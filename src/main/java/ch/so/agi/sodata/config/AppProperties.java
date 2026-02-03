package ch.so.agi.sodata.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String configFile,
        String itemsGeojsonDir
) {
}
