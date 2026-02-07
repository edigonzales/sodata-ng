package ch.so.agi.sodata.service;

import ch.so.agi.sodata.config.AppProperties;
import ch.so.agi.sodata.domain.ThemePublication;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThemePublicationXmlParserTest {

    @Test
    void loadThemePublicationsParsesXml() throws Exception {
        Path xmlPath = Path.of("src/test/resources/datasearch-test.xml").toAbsolutePath();
        AppProperties appProperties = new AppProperties(xmlPath.toString(), "build/tmp/items");
        ThemePublicationXmlParser parser = new ThemePublicationXmlParser(appProperties);

        List<ThemePublication> publications = parser.loadThemePublications();

        assertThat(publications).hasSize(2);
        ThemePublication alpha = publications.getFirst();
        assertThat(alpha.identifier()).isEqualTo("ch.so.agi.alpha");
        assertThat(alpha.title()).isEqualTo("Alpha Dataset");
        assertThat(alpha.owner().agencyName()).isEqualTo("Amt Alpha");
        assertThat(alpha.items()).hasSize(1);
        assertThat(alpha.tablesInfo()).hasSize(1);
        assertThat(alpha.tablesInfo().getFirst().attributesInfo()).hasSize(2);
        assertThat(alpha.tablesInfo().getFirst().attributesInfo().getFirst().name()).isEqualTo("alpha_id");
        assertThat(alpha.tablesInfo().getFirst().attributesInfo().getFirst().datatype()).isEqualTo("Integer");
        assertThat(alpha.tablesInfo().getFirst().attributesInfo().getFirst().mandatory()).isTrue();
    }
}
