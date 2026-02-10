package ch.so.agi.sodata.service;

import ch.so.agi.sodata.config.AppProperties;
import ch.so.agi.sodata.domain.ThemePublication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ItemsMapMlWriterTest {
    private static final Pattern FIRST_COORDINATE_PATTERN =
            Pattern.compile("<map-coordinates>\\s*([0-9.+\\-Ee]+)\\s+([0-9.+\\-Ee]+)");

    @TempDir
    Path tempDir;

    @Test
    void writesMapMlForItems() throws Exception {
        Path xmlPath = Path.of("src/test/resources/datasearch-test.xml").toAbsolutePath();
        AppProperties appProperties = new AppProperties(xmlPath.toString(), tempDir.toString());
        ThemePublicationXmlParser parser = new ThemePublicationXmlParser(appProperties);
        List<ThemePublication> publications = parser.loadThemePublications();

        ItemsMapMlWriter writer = new ItemsMapMlWriter(appProperties, new SubunitMapMlService());
        writer.writeMapMlFiles(publications);

        Path mapMlPath = tempDir.resolve("ch.so.agi.alpha.gpkg.zip.mapml");
        assertThat(Files.exists(mapMlPath)).isTrue();
        assertThat(Files.exists(tempDir.resolve("ch.so.agi.alpha.geojson"))).isFalse();

        String mapml = Files.readString(mapMlPath);
        assertThat(mapml).contains("<mapml-");
        assertThat(mapml).contains("<map-featurecaption>Alpha Item</map-featurecaption>");
        assertThat(mapml).contains("https://files.example/ch.so.agi.alpha/aktuell/alpha-1.gpkg.zip");

        Matcher matcher = FIRST_COORDINATE_PATTERN.matcher(mapml);
        assertThat(matcher.find()).isTrue();
        double x = Double.parseDouble(matcher.group(1));
        double y = Double.parseDouble(matcher.group(2));

        assertThat(x).isCloseTo(828064.77, within(0.5));
        assertThat(y).isCloseTo(5934093.19, within(0.5));
        assertThat(y).isBetween(5_930_000.0, 5_940_000.0);
    }
}
