package ch.so.agi.sodata.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indexing")
public record IndexingProperties(
        String directory,
        int queryMaxRecords
) {
}
