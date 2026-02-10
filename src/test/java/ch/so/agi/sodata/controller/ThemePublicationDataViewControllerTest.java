package ch.so.agi.sodata.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ThemePublicationDataViewControllerTest {
    private static final Path tempDir = createTempDir();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        Path xmlPath = Path.of("src/test/resources/datasearch-view-test.xml").toAbsolutePath();
        registry.add("app.config-file", xmlPath::toString);
        registry.add("app.items-geojson-dir", () -> tempDir.resolve("items").toString());
        registry.add("indexing.directory", () -> tempDir.resolve("lucene").toString());
        registry.add("indexing.query-max-records", () -> "100");
    }

    private static Path createTempDir() {
        try {
            return Files.createTempDirectory("sodata-data-view-tests");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create temp dir for tests", e);
        }
    }

    @BeforeEach
    void setUp() {
        resetItemsDir();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void returnsDataPageWithMapForExistingIdentifier() throws Exception {
        mockMvc.perform(get("/themepublication/data/ch.so.agi.subunit/xtf.zip"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Datenauswahl (Platzhalter)")))
                .andExpect(content().string(containsString("Webkarte")))
                .andExpect(content().string(containsString("<mapml-viewer")))
                .andExpect(content().string(containsString("map-layer label=\"Hintergrundkarte\"")))
                .andExpect(content().string(containsString(
                        "src=\"/themepublication/data/ch.so.agi.subunit/xtf.zip/subunits.mapml\"")))
                .andExpect(content().string(containsString("ch.so.agi.subunit")))
                .andExpect(content().string(containsString("xtf.zip")));
    }

    @Test
    void returnsMapMlForExistingIdentifierAndFormat() throws Exception {
        Path itemsDir = tempDir.resolve("items");
        Files.createDirectories(itemsDir);
        Files.writeString(itemsDir.resolve("ch.so.agi.subunit.geojson"), """
                {
                  "type": "FeatureCollection",
                  "features": [
                    {
                      "type": "Feature",
                      "id": "subunit-1",
                      "properties": {
                        "identifier": "subunit-1",
                        "title": "Subunit Item 1"
                      },
                      "geometry": {
                        "type": "Polygon",
                        "coordinates": [[[2610000.0,1210000.0],[2610100.0,1210000.0],[2610100.0,1210100.0],[2610000.0,1210100.0],[2610000.0,1210000.0]]]
                      }
                    }
                  ]
                }
                """);

        mockMvc.perform(get("/themepublication/data/ch.so.agi.subunit/xtf.zip/subunits.mapml"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/mapml")))
                .andExpect(content().string(containsString("<mapml-")))
                .andExpect(content().string(containsString("<map-title>Subunit Dataset (Subunits)</map-title>")))
                .andExpect(content().string(containsString("<map-featurecaption>Subunit Item 1</map-featurecaption>")))
                .andExpect(content().string(containsString("class=\"subunit-geometry\"")))
                .andExpect(content().string(containsString("stroke: #1f6fd6")))
                .andExpect(content().string(containsString("stroke-width: 3px")))
                .andExpect(content().string(containsString("fill: #ffffff")))
                .andExpect(content().string(containsString("fill-opacity: 0.1")))
                .andExpect(content().string(containsString(
                        "https://files.example/ch.so.agi.subunit/aktuell/subunit-1.xtf.zip")));
    }

    @Test
    void returnsNotFoundForMissingSubunitGeometryFile() throws Exception {
        mockMvc.perform(get("/themepublication/data/ch.so.agi.subunit/xtf.zip/subunits.mapml"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsBadRequestForUnsupportedFormat() throws Exception {
        Path itemsDir = tempDir.resolve("items");
        Files.createDirectories(itemsDir);
        Files.writeString(itemsDir.resolve("ch.so.agi.subunit.geojson"), """
                {"type":"FeatureCollection","features":[]}
                """);

        mockMvc.perform(get("/themepublication/data/ch.so.agi.subunit/gpkg.zip/subunits.mapml"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownIdentifier() throws Exception {
        mockMvc.perform(get("/themepublication/data/ch.so.agi.unknown/gpkg.zip"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundForUnknownIdentifierMapMl() throws Exception {
        mockMvc.perform(get("/themepublication/data/ch.so.agi.unknown/xtf.zip/subunits.mapml"))
                .andExpect(status().isNotFound());
    }

    private void resetItemsDir() {
        Path itemsDir = tempDir.resolve("items");
        try {
            Files.createDirectories(itemsDir);
            try (var walk = Files.walk(itemsDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .filter(path -> !path.equals(itemsDir))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception e) {
                                throw new IllegalStateException("Failed to clean test items directory", e);
                            }
                        });
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize test items directory", e);
        }
    }
}
