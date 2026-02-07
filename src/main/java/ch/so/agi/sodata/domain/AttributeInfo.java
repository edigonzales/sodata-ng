package ch.so.agi.sodata.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeInfo(
        String name,
        String alias,
        String shortDescription,
        String datatype,
        Boolean mandatory
) {
}
