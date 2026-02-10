package ch.so.agi.sodata.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import ch.so.agi.sodata.config.AppProperties;
import ch.so.agi.sodata.domain.FileFormat;
import ch.so.agi.sodata.domain.ThemePublication;
import ch.so.agi.sodata.service.LuceneSearcherException;
import ch.so.agi.sodata.service.SubunitMapMlService;
import ch.so.agi.sodata.service.ThemePublicationIndexService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/themepublication/data")
public class ThemePublicationDataViewController {
    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z0-9._-]+$");

    private final ThemePublicationIndexService indexService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final SubunitMapMlService subunitMapMlService;

    public ThemePublicationDataViewController(
            ThemePublicationIndexService indexService,
            AppProperties appProperties,
            ObjectMapper objectMapper,
            SubunitMapMlService subunitMapMlService
    ) {
        this.indexService = indexService;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.subunitMapMlService = subunitMapMlService;
    }

    @GetMapping(value = "/{identifier}/{format:.+}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView themePublicationData(
            @PathVariable("identifier") String identifier,
            @PathVariable("format") String format
    ) throws LuceneSearcherException {
        Optional<ThemePublication> publication = indexService.findByIdentifier(identifier);
        if (publication.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Theme publication not found.");
        }

        ModelAndView modelAndView = new ModelAndView("themepublication-data");
        modelAndView.addObject("publication", publication.get());
        modelAndView.addObject("format", format);
        return modelAndView;
    }

    @ResponseBody
    @GetMapping(value = "/{identifier}/{format:.+}/subunits.mapml", produces = "text/mapml;charset=UTF-8")
    public ResponseEntity<String> themePublicationSubunitsMapMl(
            @PathVariable("identifier") String identifier,
            @PathVariable("format") String format
    ) throws LuceneSearcherException {
        ThemePublication publication = indexService.findByIdentifier(identifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Theme publication not found."));

        if (!isFormatSupported(publication, format)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format not supported for theme publication.");
        }

        Path subunitPath = resolveSubunitPath(identifier);
        if (subunitPath == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subunit geometry file not found.");
        }

        JsonNode featureCollection;
        try {
            featureCollection = objectMapper.readTree(subunitPath.toFile());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read subunit geometry file.", e);
        }

        String mapml = subunitMapMlService.toMapMl(publication, format, featureCollection);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/mapml;charset=UTF-8"))
                .body(mapml);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(LuceneSearcherException.class)
    public String handleLuceneError(LuceneSearcherException ex) {
        return ex.getMessage();
    }

    private boolean isFormatSupported(ThemePublication publication, String format) {
        if (publication.fileFormats() == null || publication.fileFormats().isEmpty() || format == null || format.isBlank()) {
            return false;
        }
        String normalizedFormat = format.trim().toLowerCase(Locale.ROOT);
        return publication.fileFormats().stream()
                .filter(Objects::nonNull)
                .map(FileFormat::abbreviation)
                .filter(Objects::nonNull)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .anyMatch(normalizedFormat::equals);
    }

    private Path resolveSubunitPath(String identifier) {
        if (identifier == null || identifier.isBlank() || !SAFE_NAME.matcher(identifier).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid identifier.");
        }

        Path dir = Path.of(appProperties.itemsGeojsonDir()).normalize();
        Path geojson = dir.resolve(identifier + ".geojson").normalize();
        Path json = dir.resolve(identifier + ".json").normalize();

        if (!geojson.startsWith(dir) || !json.startsWith(dir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid identifier.");
        }

        if (Files.exists(geojson) && Files.isRegularFile(geojson)) {
            return geojson;
        }
        if (Files.exists(json) && Files.isRegularFile(json)) {
            return json;
        }
        return null;
    }
}
