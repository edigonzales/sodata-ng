package ch.so.agi.sodata.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SubunitGeoJsonControllerTest {

    private static final Path tempDir = createTempDir();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        Path xmlPath = Path.of("src/test/resources/datasearch-test.xml").toAbsolutePath();
        registry.add("app.config-file", xmlPath::toString);
        registry.add("app.items-geojson-dir", () -> tempDir.toString());
        registry.add("indexing.directory", () -> tempDir.resolve("lucene").toString());
        registry.add("indexing.query-max-records", () -> "100");
    }

    private static Path createTempDir() {
        try {
            return Files.createTempDirectory("sodata-subunit-geojson");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create temp dir for tests", e);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void returnsGeoJsonForJsonRequest() throws Exception {
        Path geoJsonPath = tempDir.resolve("ch.so.afu.abbaustellen.geojson");
        Files.writeString(geoJsonPath, "{\"type\":\"FeatureCollection\",\"features\":[]}");

        mockMvc.perform(get("/subunits/ch.so.afu.abbaustellen.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/geo+json"))
                .andExpect(content().json("{\"type\":\"FeatureCollection\",\"features\":[]}"));
    }

    @Test
    void returnsGeoJsonForGeoJsonRequest() throws Exception {
        Path geoJsonPath = tempDir.resolve("ch.so.afu.abbaustellen.geojson");
        Files.writeString(geoJsonPath, "{\"type\":\"FeatureCollection\",\"features\":[]}");

        mockMvc.perform(get("/subunits/ch.so.afu.abbaustellen.geojson"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/geo+json"))
                .andExpect(content().json("{\"type\":\"FeatureCollection\",\"features\":[]}"));
    }

    @Test
    void returnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(get("/subunits/missing.json"))
                .andExpect(status().isNotFound());
    }
}
