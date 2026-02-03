package ch.so.agi.sodata.service;

import ch.so.agi.sodata.domain.ThemePublication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ThemePublicationBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ThemePublicationBootstrap.class);

    private final ThemePublicationXmlParser xmlParser;
    private final ItemsGeoJsonWriter itemsGeoJsonWriter;
    private final ThemePublicationIndexService indexService;

    public ThemePublicationBootstrap(
            ThemePublicationXmlParser xmlParser,
            ItemsGeoJsonWriter itemsGeoJsonWriter,
            ThemePublicationIndexService indexService
    ) {
        this.xmlParser = xmlParser;
        this.itemsGeoJsonWriter = itemsGeoJsonWriter;
        this.indexService = indexService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<ThemePublication> publications = xmlParser.loadThemePublications();
        log.info("Loaded {} theme publications from XML.", publications.size());
        itemsGeoJsonWriter.writeGeoJsonFiles(publications);
        indexService.rebuildIndex(publications);
    }
}
