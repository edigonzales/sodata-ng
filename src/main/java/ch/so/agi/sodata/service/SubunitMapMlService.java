package ch.so.agi.sodata.service;

import ch.so.agi.sodata.domain.ThemePublication;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
public class SubunitMapMlService {
    private static final String GEOMETRY_CLASS = "subunit-geometry";

    public String toMapMl(ThemePublication publication, String format, List<SubunitFeature> features) {
        String layerLabel = firstNonBlank(publication.title(), publication.identifier(), "Subunits") + " (Subunits)";

        StringBuilder mapml = new StringBuilder();
        mapml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        mapml.append("<mapml- lang=\"de\" xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        mapml.append("  <map-head>\n");
        mapml.append("    <map-title>").append(escapeXml(layerLabel)).append("</map-title>\n");
        mapml.append("    <map-meta http-equiv=\"Content-Type\" content=\"text/mapml;charset=UTF-8\" />\n");
        mapml.append("    <map-meta charset=\"utf-8\" />\n");
        mapml.append("    <map-meta name=\"projection\" content=\"OSMTILE\" />\n");
        mapml.append("    <map-meta name=\"cs\" content=\"pcrs\" />\n");
        mapml.append("    <map-style>.")
                .append(GEOMETRY_CLASS)
                .append(" { stroke: #1f6fd6; stroke-width: 3px; fill: #ffffff; fill-opacity: 0.4; }")
                .append(".")
                .append(GEOMETRY_CLASS)
                .append(":hover, .")
                .append(GEOMETRY_CLASS)
                .append(":focus, .")
                .append(GEOMETRY_CLASS)
                .append(":active { stroke: #1f6fd6 !important; stroke-width: 3px !important; fill: #ffffff !important; fill-opacity: 0.1 !important; }</map-style>\n");
        mapml.append("  </map-head>\n");
        mapml.append("  <map-body>\n");
        appendFeatures(mapml, publication, format, features);
        mapml.append("  </map-body>\n");
        mapml.append("</mapml->\n");
        return mapml.toString();
    }

    private void appendFeatures(StringBuilder mapml, ThemePublication publication, String format, List<SubunitFeature> features) {
        if (features == null || features.isEmpty()) {
            return;
        }

        for (SubunitFeature feature : features) {
            if (feature == null) {
                continue;
            }

            StringBuilder geometryMarkup = new StringBuilder();
            if (!appendGeometry(feature.geometry(), geometryMarkup)) {
                continue;
            }

            String itemIdentifier = firstNonBlank(feature.itemIdentifier(), feature.featureId());
            String featureTitle = firstNonBlank(feature.title(), itemIdentifier, "Subunit");
            String featureId = firstNonBlank(feature.featureId(), itemIdentifier);
            String downloadUrl = buildDownloadUrl(publication, itemIdentifier, format);

            mapml.append("    <map-feature");
            if (featureId != null) {
                mapml.append(" id=\"").append(escapeXml(featureId)).append("\"");
            }
            mapml.append(">\n");
            mapml.append("      <map-featurecaption>").append(escapeXml(featureTitle)).append("</map-featurecaption>\n");
            mapml.append("      <map-geometry cs=\"pcrs\">\n");
            mapml.append(geometryMarkup);
            mapml.append("      </map-geometry>\n");
            mapml.append("      <map-properties>\n");
            mapml.append("        <div>\n");
            mapml.append("          <p><strong>Subunit:</strong> ").append(escapeXml(featureTitle)).append("</p>\n");
            if (itemIdentifier != null) {
                mapml.append("          <p><strong>Identifier:</strong> ")
                        .append(escapeXml(itemIdentifier))
                        .append("</p>\n");
            }
            if (downloadUrl != null) {
                mapml.append("          <p><a href=\"")
                        .append(escapeXml(downloadUrl))
                        .append("\" target=\"_blank\" rel=\"noopener noreferrer\">Download ")
                        .append(escapeXml(format.toUpperCase(Locale.ROOT)))
                        .append("</a></p>\n");
            }
            mapml.append("        </div>\n");
            mapml.append("      </map-properties>\n");
            mapml.append("    </map-feature>\n");
        }
    }

    private boolean appendGeometry(Geometry geometry, StringBuilder out) {
        if (geometry == null || geometry.isEmpty()) {
            return false;
        }

        if (geometry instanceof Point point) {
            return appendPoint(point, out);
        }
        if (geometry instanceof MultiPoint multiPoint) {
            return appendMultiPoint(multiPoint, out);
        }
        if (geometry instanceof LineString lineString) {
            return appendLineString(lineString, out);
        }
        if (geometry instanceof MultiLineString multiLineString) {
            return appendMultiLineString(multiLineString, out);
        }
        if (geometry instanceof Polygon polygon) {
            return appendPolygon(polygon, out);
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            return appendMultiPolygon(multiPolygon, out);
        }
        if (geometry instanceof GeometryCollection geometryCollection) {
            return appendGeometryCollection(geometryCollection, out);
        }

        return false;
    }

    private boolean appendPoint(Point point, StringBuilder out) {
        String pair = coordinatePair(point.getCoordinate());
        if (pair == null) {
            return false;
        }
        out.append("        <map-point class=\"")
                .append(GEOMETRY_CLASS)
                .append("\"><map-coordinates>")
                .append(pair)
                .append("</map-coordinates></map-point>\n");
        return true;
    }

    private boolean appendMultiPoint(MultiPoint multiPoint, StringBuilder out) {
        StringBuilder coordinateText = new StringBuilder();
        int validCount = 0;
        for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
            Point point = (Point) multiPoint.getGeometryN(i);
            String pair = coordinatePair(point.getCoordinate());
            if (pair == null) {
                continue;
            }
            if (validCount > 0) {
                coordinateText.append(' ');
            }
            coordinateText.append(pair);
            validCount++;
        }
        if (validCount == 0) {
            return false;
        }

        out.append("        <map-multipoint class=\"")
                .append(GEOMETRY_CLASS)
                .append("\"><map-coordinates>")
                .append(coordinateText)
                .append("</map-coordinates></map-multipoint>\n");
        return true;
    }

    private boolean appendLineString(LineString lineString, StringBuilder out) {
        String line = lineCoordinates(lineString.getCoordinates());
        if (line == null) {
            return false;
        }
        out.append("        <map-linestring class=\"")
                .append(GEOMETRY_CLASS)
                .append("\"><map-coordinates>")
                .append(line)
                .append("</map-coordinates></map-linestring>\n");
        return true;
    }

    private boolean appendMultiLineString(MultiLineString multiLineString, StringBuilder out) {
        StringBuilder content = new StringBuilder();
        int validLines = 0;
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            LineString lineString = (LineString) multiLineString.getGeometryN(i);
            String line = lineCoordinates(lineString.getCoordinates());
            if (line == null) {
                continue;
            }
            content.append("          <map-coordinates>")
                    .append(line)
                    .append("</map-coordinates>\n");
            validLines++;
        }
        if (validLines == 0) {
            return false;
        }

        out.append("        <map-multilinestring class=\"")
                .append(GEOMETRY_CLASS)
                .append("\">\n")
                .append(content)
                .append("        </map-multilinestring>\n");
        return true;
    }

    private boolean appendPolygon(Polygon polygon, StringBuilder out) {
        StringBuilder rings = new StringBuilder();
        int validRings = appendPolygonRings(polygon, "          ", rings);
        if (validRings == 0) {
            return false;
        }

        out.append("        <map-polygon class=\"")
                .append(GEOMETRY_CLASS)
                .append("\">\n")
                .append(rings)
                .append("        </map-polygon>\n");
        return true;
    }

    private boolean appendMultiPolygon(MultiPolygon multiPolygon, StringBuilder out) {
        StringBuilder polygons = new StringBuilder();
        int validPolygons = 0;
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
            StringBuilder polygonContent = new StringBuilder();
            if (appendPolygonRings(polygon, "            ", polygonContent) == 0) {
                continue;
            }
            polygons.append("          <map-polygon class=\"")
                    .append(GEOMETRY_CLASS)
                    .append("\">\n")
                    .append(polygonContent)
                    .append("          </map-polygon>\n");
            validPolygons++;
        }
        if (validPolygons == 0) {
            return false;
        }

        out.append("        <map-multipolygon class=\"")
                .append(GEOMETRY_CLASS)
                .append("\">\n")
                .append(polygons)
                .append("        </map-multipolygon>\n");
        return true;
    }

    private int appendPolygonRings(Polygon polygon, String indent, StringBuilder out) {
        int validRings = 0;

        String exterior = lineCoordinates(polygon.getExteriorRing().getCoordinates());
        if (exterior != null) {
            out.append(indent).append("<map-coordinates>")
                    .append(exterior)
                    .append("</map-coordinates>\n");
            validRings++;
        }

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            String interior = lineCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (interior == null) {
                continue;
            }
            out.append(indent).append("<map-coordinates>")
                    .append(interior)
                    .append("</map-coordinates>\n");
            validRings++;
        }

        return validRings;
    }

    private boolean appendGeometryCollection(GeometryCollection geometryCollection, StringBuilder out) {
        StringBuilder children = new StringBuilder();
        int validChildren = 0;
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            StringBuilder child = new StringBuilder();
            if (!appendGeometry(geometryCollection.getGeometryN(i), child)) {
                continue;
            }
            children.append(child);
            validChildren++;
        }
        if (validChildren == 0) {
            return false;
        }

        out.append("        <map-geometrycollection>\n")
                .append(children)
                .append("        </map-geometrycollection>\n");
        return true;
    }

    private String lineCoordinates(Coordinate[] coordinates) {
        if (coordinates == null || coordinates.length == 0) {
            return null;
        }

        StringBuilder line = new StringBuilder();
        int validPoints = 0;
        for (Coordinate coordinate : coordinates) {
            String pair = coordinatePair(coordinate);
            if (pair == null) {
                continue;
            }
            if (validPoints > 0) {
                line.append(' ');
            }
            line.append(pair);
            validPoints++;
        }

        return validPoints >= 2 ? line.toString() : null;
    }

    private String coordinatePair(Coordinate coordinate) {
        if (coordinate == null || !Double.isFinite(coordinate.x) || !Double.isFinite(coordinate.y)) {
            return null;
        }
        return coordinate.x + " " + coordinate.y;
    }

    private String buildDownloadUrl(ThemePublication publication, String itemIdentifier, String format) {
        if (publication.downloadHostUrl() == null || publication.downloadHostUrl().isBlank()
                || publication.identifier() == null || publication.identifier().isBlank()
                || itemIdentifier == null || itemIdentifier.isBlank()
                || format == null || format.isBlank()) {
            return null;
        }

        String host = publication.downloadHostUrl().replaceAll("/+$", "");
        return host + "/"
                + encodePathSegment(publication.identifier())
                + "/aktuell/"
                + encodePathSegment(itemIdentifier + "." + format);
    }

    private String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public record SubunitFeature(
            String featureId,
            String itemIdentifier,
            String title,
            Geometry geometry
    ) {
    }
}
