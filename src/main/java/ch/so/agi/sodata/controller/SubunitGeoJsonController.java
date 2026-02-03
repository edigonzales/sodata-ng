package ch.so.agi.sodata.controller;

import ch.so.agi.sodata.config.AppProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/subunits")
public class SubunitGeoJsonController {
    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z0-9._-]+$");

    private final AppProperties appProperties;

    public SubunitGeoJsonController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getSubunit(@PathVariable(name = "fileName") String fileName) {
        String baseName = extractBaseName(fileName);
        if (baseName == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Path dir = Path.of(appProperties.itemsGeojsonDir()).normalize();
        Path geojsonPath = dir.resolve(baseName + ".geojson");
        Path jsonPath = dir.resolve(baseName + ".json");

        Path selected = null;
        if (Files.exists(geojsonPath) && Files.isRegularFile(geojsonPath)) {
            selected = geojsonPath;
        } else if (Files.exists(jsonPath) && Files.isRegularFile(jsonPath)) {
            selected = jsonPath;
        }

        if (selected == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Path normalized = selected.normalize();
        if (!normalized.startsWith(dir)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        MediaType mediaType = normalized.getFileName().toString().endsWith(".geojson")
                ? MediaType.parseMediaType("application/geo+json")
                : MediaType.APPLICATION_JSON;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new FileSystemResource(normalized));
    }

    private String extractBaseName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        String baseName = fileName;
        if (baseName.endsWith(".geojson")) {
            baseName = baseName.substring(0, baseName.length() - ".geojson".length());
        } else if (baseName.endsWith(".json")) {
            baseName = baseName.substring(0, baseName.length() - ".json".length());
        }
        if (!SAFE_NAME.matcher(baseName).matches()) {
            return null;
        }
        return baseName;
    }
}
