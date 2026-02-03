package ch.so.agi.sodata.domain;

import java.util.List;

public record Service(
        String endpoint,
        String type,
        List<Layer> layers
) {
}
