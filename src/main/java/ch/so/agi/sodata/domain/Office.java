package ch.so.agi.sodata.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public record Office(
        String agencyName,
        String abbreviation,
        String division,
        String officeAtWeb,
        String email,
        String phone
) {
}
