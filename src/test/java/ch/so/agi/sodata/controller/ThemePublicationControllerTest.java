package ch.so.agi.sodata.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ThemePublicationControllerTest {

    private static final Path tempDir = createTempDir();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        Path xmlPath = Path.of("src/test/resources/datasearch-test.xml").toAbsolutePath();
        registry.add("app.config-file", xmlPath::toString);
        registry.add("app.items-geojson-dir", () -> tempDir.resolve("items").toString());
        registry.add("indexing.directory", () -> tempDir.resolve("lucene").toString());
        registry.add("indexing.query-max-records", () -> "100");
    }

    private static Path createTempDir() {
        try {
            return Files.createTempDirectory("sodata-tests");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create temp dir for tests", e);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void returnsAllSortedByTitleWhenQueryMissing() throws Exception {
        mockMvc.perform(get("/themepublications"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResolvedException()).isNull())
                .andExpect(jsonPath("$[0].title").value("Alpha Dataset"))
                .andExpect(jsonPath("$[1].title").value("Beta Dataset"));
    }

    @Test
    void returnsFilteredResultsWhenQueryProvided() throws Exception {
        mockMvc.perform(get("/themepublications").param("query", "beta"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResolvedException()).isNull())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].identifier").value("ch.so.agi.beta"));
    }
}
