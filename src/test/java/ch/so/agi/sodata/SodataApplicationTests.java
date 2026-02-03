package ch.so.agi.sodata;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
class SodataApplicationTests {

    private static final Path tempDir = createTempDir();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        Path xmlPath = Path.of("src/test/resources/datasearch-test.xml").toAbsolutePath();
        registry.add("app.config-file", xmlPath::toString);
        registry.add("app.items-geojson-dir", () -> tempDir.resolve("items").toString());
        registry.add("indexing.directory", () -> tempDir.resolve("lucene").toString());
        registry.add("indexing.query-max-records", () -> "100");
    }

    @Test
    void contextLoads() {
    }

    private static Path createTempDir() {
        try {
            return Files.createTempDirectory("sodata-context");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create temp dir for tests", e);
        }
    }
}
