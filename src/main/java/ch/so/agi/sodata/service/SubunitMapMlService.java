package ch.so.agi.sodata.service;

import ch.so.agi.sodata.domain.ThemePublication;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class SubunitMapMlService {
    private static final String GEOMETRY_CLASS = "subunit-geometry";

    public String toMapMl(ThemePublication publication, String format, JsonNode featureCollection) {
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
                .append(":active { stroke: #1f6fd6 !important; fill: #ffffff !important; fill-opacity: 0.1 !important; }</map-style>\n");
        mapml.append("  </map-head>\n");
        mapml.append("  <map-body>\n");
        appendFeatures(mapml, publication, format, featureCollection);
        mapml.append("  </map-body>\n");
        mapml.append("</mapml->\n");
        return mapml.toString();
    }

    private void appendFeatures(StringBuilder mapml, ThemePublication publication, String format, JsonNode featureCollection) {
        JsonNode features = featureCollection.path("features");
        if (!features.isArray()) {
            return;
        }

        for (JsonNode feature : features) {
            if (!"Feature".equals(feature.path("type").asText(""))) {
                continue;
            }

            JsonNode geometry = feature.path("geometry");
            StringBuilder geometryMarkup = new StringBuilder();
            if (!appendGeometry(geometry, geometryMarkup)) {
                continue;
            }

            JsonNode properties = feature.path("properties");
            String itemIdentifier = firstNonBlank(
                    textValue(properties.path("identifier")),
                    textValue(feature.path("id"))
            );
            String featureTitle = firstNonBlank(
                    textValue(properties.path("title")),
                    itemIdentifier,
                    "Subunit"
            );
            String featureId = firstNonBlank(textValue(feature.path("id")), itemIdentifier);
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

    private boolean appendGeometry(JsonNode geometry, StringBuilder out) {
        if (geometry == null || !geometry.isObject()) {
            return false;
        }

        String type = geometry.path("type").asText("");
        JsonNode coordinates = geometry.path("coordinates");
        return switch (type) {
            case "Point" -> appendPoint(coordinates, out);
            case "MultiPoint" -> appendMultiPoint(coordinates, out);
            case "LineString" -> appendLineString(coordinates, out);
            case "MultiLineString" -> appendMultiLineString(coordinates, out);
            case "Polygon" -> appendPolygon(coordinates, out);
            case "MultiPolygon" -> appendMultiPolygon(coordinates, out);
            case "GeometryCollection" -> appendGeometryCollection(geometry.path("geometries"), out);
            default -> false;
        };
    }

    private boolean appendPoint(JsonNode coordinates, StringBuilder out) {
        String pair = coordinatePair(coordinates);
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

    private boolean appendMultiPoint(JsonNode coordinates, StringBuilder out) {
        if (!coordinates.isArray()) {
            return false;
        }

        StringBuilder coordinateText = new StringBuilder();
        int validCount = 0;
        for (JsonNode coordinate : coordinates) {
            String pair = coordinatePair(coordinate);
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

    private boolean appendLineString(JsonNode coordinates, StringBuilder out) {
        String line = lineCoordinates(coordinates);
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

    private boolean appendMultiLineString(JsonNode coordinates, StringBuilder out) {
        if (!coordinates.isArray()) {
            return false;
        }

        StringBuilder content = new StringBuilder();
        int validLines = 0;
        for (JsonNode lineNode : coordinates) {
            String line = lineCoordinates(lineNode);
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

    private boolean appendPolygon(JsonNode coordinates, StringBuilder out) {
        if (!coordinates.isArray()) {
            return false;
        }

        StringBuilder rings = new StringBuilder();
        int validRings = 0;
        for (JsonNode ring : coordinates) {
            String line = lineCoordinates(ring);
            if (line == null) {
                continue;
            }
            rings.append("          <map-coordinates>")
                    .append(line)
                    .append("</map-coordinates>\n");
            validRings++;
        }
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

    private boolean appendMultiPolygon(JsonNode coordinates, StringBuilder out) {
        if (!coordinates.isArray()) {
            return false;
        }

        StringBuilder polygons = new StringBuilder();
        int validPolygons = 0;
        for (JsonNode polygon : coordinates) {
            StringBuilder polygonContent = new StringBuilder();
            if (!appendPolygonContent(polygon, polygonContent)) {
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

    private boolean appendGeometryCollection(JsonNode geometries, StringBuilder out) {
        if (!geometries.isArray()) {
            return false;
        }

        StringBuilder children = new StringBuilder();
        int validChildren = 0;
        for (JsonNode geometry : geometries) {
            StringBuilder child = new StringBuilder();
            if (!appendGeometry(geometry, child)) {
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

    private boolean appendPolygonContent(JsonNode polygon, StringBuilder out) {
        if (!polygon.isArray()) {
            return false;
        }

        int validRings = 0;
        for (JsonNode ring : polygon) {
            String line = lineCoordinates(ring);
            if (line == null) {
                continue;
            }
            out.append("            <map-coordinates>")
                    .append(line)
                    .append("</map-coordinates>\n");
            validRings++;
        }
        return validRings > 0;
    }

    private String lineCoordinates(JsonNode coordinates) {
        if (!coordinates.isArray()) {
            return null;
        }
        StringBuilder line = new StringBuilder();
        int validPoints = 0;
        for (JsonNode coordinate : coordinates) {
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

    private String coordinatePair(JsonNode coordinate) {
        if (!coordinate.isArray() || coordinate.size() < 2) {
            return null;
        }

        String x = numericText(coordinate.get(0));
        String y = numericText(coordinate.get(1));
        if (x == null || y == null) {
            return null;
        }
        return x + " " + y;
    }

    private String numericText(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asText();
        }
        if (value.isTextual()) {
            String text = value.asText().trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                Double.parseDouble(text);
                return text;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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

    private String textValue(JsonNode node) {
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String value = node.asText();
        return value == null ? null : value.trim();
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
}
