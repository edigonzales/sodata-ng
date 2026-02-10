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
    private final ItemsMapMlWriter itemsMapMlWriter;
    private final ThemePublicationIndexService indexService;

    public ThemePublicationBootstrap(
            ThemePublicationXmlParser xmlParser,
            ItemsMapMlWriter itemsMapMlWriter,
            ThemePublicationIndexService indexService
    ) {
        this.xmlParser = xmlParser;
        this.itemsMapMlWriter = itemsMapMlWriter;
        this.indexService = indexService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<ThemePublication> publications = xmlParser.loadThemePublications();
        log.info("Loaded {} theme publications from XML.", publications.size());
        itemsMapMlWriter.writeMapMlFiles(publications);
        indexService.rebuildIndex(publications);
    }
}
