package ch.so.agi.sodata.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class DownloadBadgeFormatSupport {
    private static final Map<String, Integer> ORDER = Map.of(
            "xtf.zip", 0,
            "itf.zip", 1,
            "gpkg.zip", 2,
            "shp.zip", 3,
            "dxf.zip", 4,
            "laz", 5,
            "tif", 6
    );

    private static final Map<String, String> LABEL = Map.of(
            "xtf.zip", "XTF",
            "itf.zip", "ITF",
            "gpkg.zip", "GPKG",
            "shp.zip", "SHP",
            "dxf.zip", "DXF",
            "laz", "LAZ",
            "tif", "GeoTIFF"
    );

    private DownloadBadgeFormatSupport() {
    }

    public static List<FileFormat> orderedFormats(List<FileFormat> fileFormats) {
        if (fileFormats == null || fileFormats.isEmpty()) {
            return List.of();
        }
        return fileFormats.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt((FileFormat format) -> ORDER.getOrDefault(normalizedAbbreviation(format), Integer.MAX_VALUE))
                        .thenComparing(DownloadBadgeFormatSupport::normalizedAbbreviation)
                )
                .toList();
    }

    public static String badgeLabel(FileFormat fileFormat) {
        if (fileFormat == null) {
            return "";
        }
        String abbreviation = normalizedAbbreviation(fileFormat);
        String label = LABEL.get(abbreviation);
        if (label != null) {
            return label;
        }

        if (abbreviation.endsWith(".zip")) {
            abbreviation = abbreviation.substring(0, abbreviation.length() - 4);
        }
        if (!abbreviation.isBlank()) {
            return abbreviation.toUpperCase(Locale.ROOT);
        }

        if (fileFormat.name() != null && !fileFormat.name().isBlank()) {
            return fileFormat.name();
        }
        return "";
    }

    public static String badgeLabel(ThemePublication publication, FileFormat fileFormat) {
        if (isRasterWithoutSubunits(publication) && isGeoTiff(fileFormat)) {
            return "Cloud Optimized GeoTIFF";
        }
        return badgeLabel(fileFormat);
    }

    private static boolean isRasterWithoutSubunits(ThemePublication publication) {
        if (publication == null) {
            return false;
        }
        if (Boolean.TRUE.equals(publication.hasSubunits())) {
            return false;
        }
        return publication.model() == null || publication.model().isBlank();
    }

    private static boolean isGeoTiff(FileFormat fileFormat) {
        return "tif".equals(normalizedAbbreviation(fileFormat));
    }

    private static String normalizedAbbreviation(FileFormat fileFormat) {
        if (fileFormat == null || fileFormat.abbreviation() == null) {
            return "";
        }
        return fileFormat.abbreviation().trim().toLowerCase(Locale.ROOT);
    }
}
