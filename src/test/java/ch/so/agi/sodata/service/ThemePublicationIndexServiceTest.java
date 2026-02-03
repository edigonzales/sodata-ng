package ch.so.agi.sodata.service;

import ch.so.agi.sodata.config.AppProperties;
import ch.so.agi.sodata.config.IndexingProperties;
import ch.so.agi.sodata.domain.ThemePublication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThemePublicationIndexServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void searchFindsMatchingDocuments() throws Exception {
        Path xmlPath = Path.of("src/test/resources/datasearch-test.xml").toAbsolutePath();
        AppProperties appProperties = new AppProperties(xmlPath.toString(), tempDir.resolve("items").toString());
        ThemePublicationXmlParser parser = new ThemePublicationXmlParser(appProperties);
        List<ThemePublication> publications = parser.loadThemePublications();

        IndexingProperties indexingProperties = new IndexingProperties(tempDir.resolve("lucene").toString(), 100);
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ThemePublicationIndexService indexService = new ThemePublicationIndexService(indexingProperties, objectMapper);
        indexService.rebuildIndex(publications);

        List<ThemePublication> results = indexService.search("alpha");
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().identifier()).isEqualTo("ch.so.agi.alpha");

        List<ThemePublication> sorted = indexService.findAllSortedByTitle();
        assertThat(sorted).hasSize(2);
        assertThat(sorted.getFirst().title()).isEqualTo("Alpha Dataset");
    }
}
