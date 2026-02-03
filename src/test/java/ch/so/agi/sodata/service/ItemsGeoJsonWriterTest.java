package ch.so.agi.sodata.service;

import ch.so.agi.sodata.config.AppProperties;
import ch.so.agi.sodata.domain.ThemePublication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemsGeoJsonWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesGeoJsonForItems() throws Exception {
        Path xmlPath = Path.of("src/test/resources/datasearch-test.xml").toAbsolutePath();
        AppProperties appProperties = new AppProperties(xmlPath.toString(), tempDir.toString());
        ThemePublicationXmlParser parser = new ThemePublicationXmlParser(appProperties);
        List<ThemePublication> publications = parser.loadThemePublications();

        ItemsGeoJsonWriter writer = new ItemsGeoJsonWriter(appProperties, new ObjectMapper());
        writer.writeGeoJsonFiles(publications);

        Path geoJsonPath = tempDir.resolve("ch.so.agi.alpha.geojson");
        assertThat(Files.exists(geoJsonPath)).isTrue();

        JsonNode root = new ObjectMapper().readTree(geoJsonPath.toFile());
        assertThat(root.get("type").asText()).isEqualTo("FeatureCollection");
        assertThat(root.get("features")).isNotNull();
        assertThat(root.get("features").size()).isEqualTo(1);
    }
}
