package ch.so.agi.sodata.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public record TableInfo(
        String sqlName,
        String title,
        String shortDescription
) {
}
