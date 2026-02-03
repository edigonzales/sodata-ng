package ch.so.agi.sodata.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ThemePublication(
        String identifier,
        String model,
        String title,
        String shortDescription,
        Boolean hasSubunits,
        LocalDate lastPublishingDate,
        LocalDate secondToLastPublishingDate,
        Office owner,
        Office servicer,
        String furtherInformation,
        String downloadHostUrl,
        String previewUrl,
        List<String> keywords,
        List<String> synonyms,
        List<FileFormat> fileFormats,
        List<TableInfo> tablesInfo,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String licence,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        Bbox bbox,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        WgcPreviewLayer wgcPreviewLayer,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        List<Item> items,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        List<Service> services
) {
    public ThemePublication {
        if (hasSubunits == null) {
            hasSubunits = false;
        }
    }
}
