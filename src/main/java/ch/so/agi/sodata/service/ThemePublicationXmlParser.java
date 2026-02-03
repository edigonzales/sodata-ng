package ch.so.agi.sodata.service;

import ch.so.agi.sodata.config.AppProperties;
import ch.so.agi.sodata.domain.Bbox;
import ch.so.agi.sodata.domain.FileFormat;
import ch.so.agi.sodata.domain.Item;
import ch.so.agi.sodata.domain.Layer;
import ch.so.agi.sodata.domain.Office;
import ch.so.agi.sodata.domain.Service;
import ch.so.agi.sodata.domain.TableInfo;
import ch.so.agi.sodata.domain.ThemePublication;
import ch.so.agi.sodata.domain.WgcPreviewLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@org.springframework.stereotype.Service
public class ThemePublicationXmlParser {
    private static final Logger log = LoggerFactory.getLogger(ThemePublicationXmlParser.class);

    private final AppProperties appProperties;

    public ThemePublicationXmlParser(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public List<ThemePublication> loadThemePublications() throws IOException {
        Path configPath = Path.of(appProperties.configFile());
        if (!Files.exists(configPath)) {
            log.warn("Config file does not exist: {}", configPath.toAbsolutePath());
            return Collections.emptyList();
        }

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            Element root = document.getDocumentElement();
            List<Element> publications = childElements(root, "themePublication");
            List<ThemePublication> results = new ArrayList<>(publications.size());
            for (Element publicationElement : publications) {
                results.add(parseThemePublication(publicationElement));
            }
            return results;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse XML config: " + configPath, e);
        }
    }

    private ThemePublication parseThemePublication(Element element) {
        String identifier = text(element, "identifier");
        String model = text(element, "model");
        String title = text(element, "title");
        String shortDescription = text(element, "shortDescription");
        Boolean hasSubunits = parseBoolean(text(element, "hasSubunits"));
        LocalDate lastPublishingDate = parseDate(text(element, "lastPublishingDate"));
        LocalDate secondToLastPublishingDate = parseDate(text(element, "secondToLastPublishingDate"));
        Office owner = parseOffice(childElement(element, "owner"));
        Office servicer = parseOffice(childElement(element, "servicer"));
        String furtherInformation = text(element, "furtherInformation");
        String downloadHostUrl = text(element, "downloadHostUrl");
        String previewUrl = text(element, "previewUrl");
        List<String> keywords = textList(element, "keywords", "keyword");
        List<String> synonyms = textList(element, "synonyms", "synonym");
        List<FileFormat> fileFormats = parseFileFormats(childElement(element, "fileFormats"));
        List<TableInfo> tablesInfo = parseTableInfo(childElement(element, "tablesInfo"));
        String licence = text(element, "licence");
        Bbox bbox = parseBbox(childElement(element, "bbox"));
        WgcPreviewLayer wgcPreviewLayer = parseWgcPreviewLayer(childElement(element, "wgcPreviewLayer"));
        List<Item> items = parseItems(childElement(element, "items"));
        List<Service> services = parseServices(childElement(element, "services"));

        return new ThemePublication(
                identifier,
                model,
                title,
                shortDescription,
                hasSubunits,
                lastPublishingDate,
                secondToLastPublishingDate,
                owner,
                servicer,
                furtherInformation,
                downloadHostUrl,
                previewUrl,
                keywords,
                synonyms,
                fileFormats,
                tablesInfo,
                licence,
                bbox,
                wgcPreviewLayer,
                items,
                services
        );
    }

    private Office parseOffice(Element element) {
        if (element == null) {
            return null;
        }
        return new Office(
                text(element, "agencyName"),
                text(element, "abbreviation"),
                text(element, "division"),
                text(element, "officeAtWeb"),
                text(element, "email"),
                text(element, "phone")
        );
    }

    private Bbox parseBbox(Element element) {
        if (element == null) {
            return null;
        }
        return new Bbox(
                parseDouble(text(element, "left")),
                parseDouble(text(element, "bottom")),
                parseDouble(text(element, "right")),
                parseDouble(text(element, "top"))
        );
    }

    private WgcPreviewLayer parseWgcPreviewLayer(Element element) {
        if (element == null) {
            return null;
        }
        return new WgcPreviewLayer(text(element, "identifier"), text(element, "title"));
    }

    private List<FileFormat> parseFileFormats(Element wrapper) {
        if (wrapper == null) {
            return null;
        }
        List<FileFormat> formats = new ArrayList<>();
        for (Element element : childElements(wrapper, "fileFormat")) {
            formats.add(new FileFormat(
                    text(element, "name"),
                    text(element, "mimetype"),
                    text(element, "abbreviation")
            ));
        }
        return formats.isEmpty() ? null : formats;
    }

    private List<TableInfo> parseTableInfo(Element wrapper) {
        if (wrapper == null) {
            return null;
        }
        List<TableInfo> tables = new ArrayList<>();
        for (Element element : childElements(wrapper, "tableInfo")) {
            tables.add(new TableInfo(
                    text(element, "sqlName"),
                    text(element, "title"),
                    text(element, "shortDescription")
            ));
        }
        return tables.isEmpty() ? null : tables;
    }

    private List<Item> parseItems(Element wrapper) {
        if (wrapper == null) {
            return null;
        }
        List<Item> items = new ArrayList<>();
        for (Element element : childElements(wrapper, "item")) {
            items.add(new Item(
                    text(element, "identifier"),
                    text(element, "title"),
                    parseDate(text(element, "lastPublishingDate")),
                    parseDate(text(element, "secondToLastPublishingDate")),
                    parseBbox(childElement(element, "bbox")),
                    text(element, "geometry")
            ));
        }
        return items.isEmpty() ? null : items;
    }

    private List<Service> parseServices(Element wrapper) {
        if (wrapper == null) {
            return null;
        }
        List<Service> services = new ArrayList<>();
        for (Element element : childElements(wrapper, "service")) {
            services.add(new Service(
                    text(element, "endpoint"),
                    text(element, "type"),
                    parseLayers(childElement(element, "layers"))
            ));
        }
        return services.isEmpty() ? null : services;
    }

    private List<Layer> parseLayers(Element wrapper) {
        if (wrapper == null) {
            return null;
        }
        List<Layer> layers = new ArrayList<>();
        for (Element element : childElements(wrapper, "layer")) {
            layers.add(new Layer(
                    text(element, "identifier"),
                    text(element, "title")
            ));
        }
        return layers.isEmpty() ? null : layers;
    }

    private List<String> textList(Element parent, String wrapperTag, String itemTag) {
        Element wrapper = childElement(parent, wrapperTag);
        if (wrapper == null) {
            return null;
        }
        List<String> values = new ArrayList<>();
        for (Element element : childElements(wrapper, itemTag)) {
            String value = element.getTextContent();
            if (value != null && !value.isBlank()) {
                values.add(value.trim());
            }
        }
        return values.isEmpty() ? null : values;
    }

    private String text(Element parent, String tag) {
        Element element = childElement(parent, tag);
        if (element == null) {
            return null;
        }
        String value = element.getTextContent();
        return value == null ? null : value.trim();
    }

    private Element childElement(Element parent, String tag) {
        if (parent == null) {
            return null;
        }
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && tag.equals(element.getTagName())) {
                return element;
            }
        }
        return null;
    }

    private List<Element> childElements(Element parent, String tag) {
        if (parent == null) {
            return Collections.emptyList();
        }
        List<Element> elements = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && tag.equals(element.getTagName())) {
                elements.add(element);
            }
        }
        return elements;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        return Double.parseDouble(value.trim());
    }
}
