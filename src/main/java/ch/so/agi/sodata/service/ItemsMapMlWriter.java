package ch.so.agi.sodata.service;

import ch.so.agi.sodata.config.AppProperties;
import ch.so.agi.sodata.domain.FileFormat;
import ch.so.agi.sodata.domain.Item;
import ch.so.agi.sodata.domain.ThemePublication;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ItemsMapMlWriter {
    private static final Logger log = LoggerFactory.getLogger(ItemsMapMlWriter.class);

    private static final String EPSG_2056_PROJ4 =
            "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 "
                    + "+k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel "
                    + "+towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs";
    private static final String EPSG_3857_PROJ4 =
            "+proj=merc +a=6378137 +b=6378137 +lat_ts=0 +lon_0=0 +x_0=0 +y_0=0 "
                    + "+k=1 +units=m +nadgrids=@null +wktext +no_defs";

    private final AppProperties appProperties;
    private final WKTReader wktReader;
    private final CoordinateTransform transform;
    private final SubunitMapMlService subunitMapMlService;

    public ItemsMapMlWriter(
            AppProperties appProperties,
            SubunitMapMlService subunitMapMlService
    ) {
        this.appProperties = appProperties;
        this.subunitMapMlService = subunitMapMlService;
        this.wktReader = new WKTReader();

        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem src = crsFactory.createFromParameters("EPSG:2056", EPSG_2056_PROJ4);
        CoordinateReferenceSystem dst = crsFactory.createFromParameters("EPSG:3857", EPSG_3857_PROJ4);
        CoordinateTransformFactory transformFactory = new CoordinateTransformFactory();
        this.transform = transformFactory.createTransform(src, dst);
    }

    public void writeMapMlFiles(List<ThemePublication> publications) {
        if (publications == null || publications.isEmpty()) {
            return;
        }

        Path outputDir = Path.of(appProperties.itemsGeojsonDir());
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            log.warn("Failed to create items map directory: {}", outputDir.toAbsolutePath(), e);
            return;
        }

        for (ThemePublication publication : publications) {
            List<Item> items = publication.items();
            if (items == null || items.isEmpty()) {
                continue;
            }

            List<SubunitMapMlService.SubunitFeature> features = buildFeatures(items);
            if (features.isEmpty()) {
                continue;
            }

            writeMapMlFiles(outputDir, publication, features);
        }
    }

    private List<SubunitMapMlService.SubunitFeature> buildFeatures(List<Item> items) {
        List<SubunitMapMlService.SubunitFeature> features = new ArrayList<>();

        for (Item item : items) {
            try {
                if (item.geometry() == null || item.geometry().isBlank()) {
                    continue;
                }

                Geometry geometry = wktReader.read(item.geometry());
                transformGeometry(geometry);

                String itemIdentifier = normalizeText(item.identifier());
                String featureId = itemIdentifier;
                String title = normalizeText(item.title());

                features.add(new SubunitMapMlService.SubunitFeature(featureId, itemIdentifier, title, geometry));
            } catch (Exception e) {
                log.warn("Failed to create mapml feature for item {}", item.identifier(), e);
            }
        }

        return features;
    }

    private void writeMapMlFiles(
            Path outputDir,
            ThemePublication publication,
            List<SubunitMapMlService.SubunitFeature> features
    ) {
        if (publication.fileFormats() == null || publication.fileFormats().isEmpty()) {
            return;
        }

        String publicationPart = sanitizeFileName(publication.identifier());
        for (FileFormat fileFormat : publication.fileFormats()) {
            if (fileFormat == null || fileFormat.abbreviation() == null || fileFormat.abbreviation().isBlank()) {
                continue;
            }

            String format = fileFormat.abbreviation().trim().toLowerCase(Locale.ROOT);
            String mapml = subunitMapMlService.toMapMl(publication, format, features);
            Path outputPath = outputDir.resolve(publicationPart + "." + sanitizeFileName(format) + ".mapml");
            try {
                Files.writeString(outputPath, mapml, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("Failed to write mapml file {}", outputPath.toAbsolutePath(), e);
            }
        }
    }

    private void transformGeometry(Geometry geometry) {
        geometry.apply((CoordinateFilter) coordinate -> {
            ProjCoordinate src = new ProjCoordinate(coordinate.x, coordinate.y);
            ProjCoordinate dst = new ProjCoordinate();
            transform.transform(src, dst);
            coordinate.x = dst.x;
            coordinate.y = dst.y;
        });
        geometry.geometryChanged();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String sanitizeFileName(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return "items";
        }
        return identifier.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
