package ch.so.agi.sodata.domain;

import java.time.LocalDate;

public record Item(
        String identifier,
        String title,
        LocalDate lastPublishingDate,
        LocalDate secondToLastPublishingDate,
        Bbox bbox,
        String geometry
) {
}
