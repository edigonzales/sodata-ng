package ch.so.agi.sodata.service;

import ch.so.agi.sodata.config.AppProperties;
import ch.so.agi.sodata.domain.Item;
import ch.so.agi.sodata.domain.ThemePublication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ItemsGeoJsonWriter {
    private static final Logger log = LoggerFactory.getLogger(ItemsGeoJsonWriter.class);

    private static final String EPSG_2056_PROJ4 =
            "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 "
                    + "+k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel "
                    + "+towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs";
    private static final String EPSG_3857_PROJ4 =
            "+proj=merc +a=6378137 +b=6378137 +lat_ts=0 +lon_0=0 +x_0=0 +y_0=0 "
                    + "+k=1 +units=m +nadgrids=@null +wktext +no_defs";

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final WKTReader wktReader;
    private final CoordinateTransform transform;

    public ItemsGeoJsonWriter(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.wktReader = new WKTReader();

        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem src = crsFactory.createFromParameters("EPSG:2056", EPSG_2056_PROJ4);
        CoordinateReferenceSystem dst = crsFactory.createFromParameters("EPSG:3857", EPSG_3857_PROJ4);
        CoordinateTransformFactory transformFactory = new CoordinateTransformFactory();
        this.transform = transformFactory.createTransform(src, dst);
    }

    public void writeGeoJsonFiles(List<ThemePublication> publications) {
        if (publications == null || publications.isEmpty()) {
            return;
        }

        Path outputDir = Path.of(appProperties.itemsGeojsonDir());
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            log.warn("Failed to create items GeoJSON directory: {}", outputDir.toAbsolutePath(), e);
            return;
        }

        for (ThemePublication publication : publications) {
            List<Item> items = publication.items();
            if (items == null || items.isEmpty()) {
                continue;
            }
            writeGeoJsonFile(outputDir, publication.identifier(), items);
        }
    }

    private void writeGeoJsonFile(Path outputDir, String identifier, List<Item> items) {
        ObjectNode featureCollection = objectMapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        ArrayNode features = featureCollection.putArray("features");

        for (Item item : items) {
            try {
                if (item.geometry() == null || item.geometry().isBlank()) {
                    continue;
                }
                Geometry geometry = wktReader.read(item.geometry());
                transformGeometry(geometry);

                ObjectNode feature = features.addObject();
                feature.put("type", "Feature");
                if (item.identifier() != null) {
                    feature.put("id", item.identifier());
                }

                ObjectNode properties = feature.putObject("properties");
                properties.put("identifier", item.identifier());
                properties.put("title", item.title());
                if (item.lastPublishingDate() != null) {
                    properties.put("lastPublishingDate", item.lastPublishingDate().toString());
                }
                if (item.secondToLastPublishingDate() != null) {
                    properties.put("secondToLastPublishingDate", item.secondToLastPublishingDate().toString());
                }

                feature.set("geometry", geometryToGeoJson(geometry));
            } catch (Exception e) {
                log.warn("Failed to create GeoJSON feature for item {}", item.identifier(), e);
            }
        }

        Path outputPath = outputDir.resolve(sanitizeFileName(identifier) + ".geojson");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), featureCollection);
        } catch (IOException e) {
            log.warn("Failed to write GeoJSON file {}", outputPath.toAbsolutePath(), e);
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

    private ObjectNode geometryToGeoJson(Geometry geometry) {
        ObjectNode geomNode = objectMapper.createObjectNode();
        if (geometry instanceof Point point) {
            geomNode.put("type", "Point");
            geomNode.set("coordinates", coordinateArray(point.getCoordinate()));
        } else if (geometry instanceof MultiPoint multiPoint) {
            geomNode.put("type", "MultiPoint");
            ArrayNode coords = geomNode.putArray("coordinates");
            for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
                Point p = (Point) multiPoint.getGeometryN(i);
                coords.add(coordinateArray(p.getCoordinate()));
            }
        } else if (geometry instanceof LineString lineString) {
            geomNode.put("type", "LineString");
            geomNode.set("coordinates", lineStringCoordinates(lineString));
        } else if (geometry instanceof MultiLineString multiLineString) {
            geomNode.put("type", "MultiLineString");
            ArrayNode coords = geomNode.putArray("coordinates");
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                LineString ls = (LineString) multiLineString.getGeometryN(i);
                coords.add(lineStringCoordinates(ls));
            }
        } else if (geometry instanceof Polygon polygon) {
            geomNode.put("type", "Polygon");
            geomNode.set("coordinates", polygonCoordinates(polygon));
        } else if (geometry instanceof MultiPolygon multiPolygon) {
            geomNode.put("type", "MultiPolygon");
            ArrayNode coords = geomNode.putArray("coordinates");
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                coords.add(polygonCoordinates(polygon));
            }
        } else if (geometry instanceof GeometryCollection collection) {
            geomNode.put("type", "GeometryCollection");
            ArrayNode geometries = geomNode.putArray("geometries");
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                geometries.add(geometryToGeoJson(collection.getGeometryN(i)));
            }
        } else {
            geomNode.put("type", geometry.getGeometryType());
            geomNode.set("coordinates", objectMapper.createArrayNode());
        }
        return geomNode;
    }

    private ArrayNode lineStringCoordinates(LineString lineString) {
        ArrayNode coords = objectMapper.createArrayNode();
        for (Coordinate coordinate : lineString.getCoordinates()) {
            coords.add(coordinateArray(coordinate));
        }
        return coords;
    }

    private ArrayNode polygonCoordinates(Polygon polygon) {
        ArrayNode rings = objectMapper.createArrayNode();
        rings.add(lineStringCoordinates(polygon.getExteriorRing()));
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            rings.add(lineStringCoordinates(polygon.getInteriorRingN(i)));
        }
        return rings;
    }

    private ArrayNode coordinateArray(Coordinate coordinate) {
        ArrayNode coord = objectMapper.createArrayNode();
        coord.add(coordinate.x);
        coord.add(coordinate.y);
        return coord;
    }

    private String sanitizeFileName(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return "items";
        }
        return identifier.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
