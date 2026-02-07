package ch.so.agi.sodata.controller;

import java.nio.file.Files;
import java.nio.file.Path;

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
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ThemePublicationViewControllerTest {
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
            return Files.createTempDirectory("sodata-view-tests");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create temp dir for tests", e);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void returnsAllResultsAsHtmlFragmentWhenQueryMissing() throws Exception {
        mockMvc.perform(get("/themepublications/fragment"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Alpha Dataset")))
                .andExpect(content().string(containsString("Beta Dataset")))
                .andExpect(content().string(containsString("/themepublications/meta/ch.so.agi.alpha")));
    }

    @Test
    void returnsFilteredHtmlFragmentWhenQueryProvided() throws Exception {
        mockMvc.perform(get("/themepublications/fragment").param("query", "beta"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Beta Dataset")))
                .andExpect(content().string(not(containsString("Alpha Dataset"))));
    }

    @Test
    void rendersSingleFormatBadgesAndMultiFormatSelectFromXml() throws Exception {
        mockMvc.perform(get("/themepublications/fragment"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<option value=\"\">Format auswählen</option>")))
                .andExpect(content().string(containsString("class=\"download-badge download-badge-link\"")))
                .andExpect(content().string(containsString(
                        "href=\"https://files.example/ch.so.agi.alpha/aktuell/ch.so.agi.alpha.gpkg.zip\"")))
                .andExpect(content().string(containsString("download-badge-copy")))
                .andExpect(content().string(containsString("Cloud Optimized GeoTIFF")))
                .andExpect(content().string(containsString(
                        "data-tooltip=\"Link wurde in Zwischenablage kopiert.\"")))
                .andExpect(content().string(containsString(
                        "data-copy-url=\"https://files.example/ch.so.agi.raster/aktuell/ch.so.agi.raster.tif\"")))
                .andExpect(content().string(containsString("data-publication-id=\"ch.so.agi.subunit\"")))
                .andExpect(content().string(containsString("data-download-mode=\"subunit\"")))
                .andExpect(content().string(containsString(
                        "value=\"/themepublication/data/ch.so.agi.subunit/xtf.zip\"")))
                .andExpect(content().string(containsString("data-publication-id=\"ch.so.agi.multi\"")))
                .andExpect(content().string(containsString("data-download-mode=\"download\"")))
                .andExpect(content().string(containsString(
                        "value=\"https://files.example/ch.so.agi.multi/aktuell/ch.so.agi.multi.gpkg.zip\"")))
                .andExpect(content().string(containsString(
                        "value=\"https://files.example/ch.so.agi.multi/aktuell/ch.so.agi.multi.shp.zip\"")));
    }

    @Test
    void returnsBadRequestForInvalidQuery() throws Exception {
        mockMvc.perform(get("/themepublications/fragment").param("query", "***"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Search term did not contain valid tokens.")));
    }

    @Test
    void returnsMetadataPageForExistingIdentifier() throws Exception {
        mockMvc.perform(get("/themepublications/meta/ch.so.agi.alpha"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Datenbeschreibung")))
                .andExpect(content().string(containsString("Alpha Dataset")))
                .andExpect(content().string(containsString("alpha_id")))
                .andExpect(content().string(containsString("Pflichtattribut")));
    }

    @Test
    void returnsNotFoundForUnknownMetadataIdentifier() throws Exception {
        mockMvc.perform(get("/themepublications/meta/ch.so.agi.unknown"))
                .andExpect(status().isNotFound());
    }
}
