@param java.util.List<ch.so.agi.sodata.domain.ThemePublication> publications

@if(publications == null || publications.isEmpty())
<p class="results-empty">Keine Treffer gefunden.</p>
@else
<div class="publications-grid-header">
    <div>Thema</div>
    <div>Publikationsdatum</div>
    <div>Metadaten</div>
    <div>Daten herunterladen</div>
</div>
<div class="publications-grid-body">
@for(ch.so.agi.sodata.domain.ThemePublication publication : publications)
    <div class="publications-row">
        <div class="publications-cell publications-theme" data-label="Thema">${publication.title()}</div>
        <div class="publications-cell publications-date" data-label="Publikationsdatum">
            @if(publication.lastPublishingDate() != null)
            ${publication.lastPublishingDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}
            @else
            -
            @endif
        </div>
        <div class="publications-cell" data-label="Metadaten">
            <a class="meta-link" href="/themepublications/meta/${publication.identifier()}" target="_blank" rel="noopener noreferrer" aria-label="Metadaten">
                <i class="bi bi-info-circle"></i>
            </a>
        </div>
        <div class="publications-cell" data-label="Daten herunterladen">
            @if(publication.fileFormats() == null || publication.fileFormats().isEmpty())
            -
            @else
                @if(Boolean.TRUE.equals(publication.hasSubunits()))
                    <select class="download-select"
                            data-publication-id="${publication.identifier()}"
                            data-download-mode="subunit"
                            aria-label="Daten herunterladen">
                        <option value="">Format auswählen</option>
                        @for(ch.so.agi.sodata.domain.FileFormat fileFormat : publication.fileFormats())
                        <option value="/themepublication/data/${publication.identifier()}/${fileFormat.abbreviation()}">
                            @if(fileFormat.name() != null && !fileFormat.name().isBlank())
                            ${fileFormat.name()}
                            @else
                            ${fileFormat.abbreviation()}
                            @endif
                        </option>
                        @endfor
                    </select>
                @else
                    @if(publication.model() == null || publication.model().isBlank())
                        @if(publication.fileFormats().size() == 1)
                        <button type="button"
                                class="download-badge download-badge-copy"
                                data-copy-url="${publication.downloadHostUrl()}/${publication.identifier()}/aktuell/${publication.identifier()}.${publication.fileFormats().get(0).abbreviation()}"
                                data-tooltip="Link wurde in Zwischenablage kopiert."
                                aria-label="Cloud Optimized GeoTIFF (Link in Zwischenablage kopieren)">
                            Cloud Optimized GeoTIFF
                        </button>
                        @else
                        <select class="download-select"
                                data-publication-id="${publication.identifier()}"
                                data-download-mode="clipboard"
                                aria-label="Daten herunterladen">
                            <option value="">Format auswählen</option>
                            @for(ch.so.agi.sodata.domain.FileFormat fileFormat : publication.fileFormats())
                            <option value="${publication.downloadHostUrl()}/${publication.identifier()}/aktuell/${publication.identifier()}.${fileFormat.abbreviation()}">
                                @if(fileFormat.name() != null && !fileFormat.name().isBlank())
                                ${fileFormat.name()}
                                @else
                                ${fileFormat.abbreviation()}
                                @endif
                            </option>
                            @endfor
                        </select>
                        @endif
                    @else
                        @if(publication.fileFormats().size() == 1)
                        <a class="download-badge download-badge-link"
                           href="${publication.downloadHostUrl()}/${publication.identifier()}/aktuell/${publication.identifier()}.${publication.fileFormats().get(0).abbreviation()}"
                           target="_blank"
                           rel="noopener noreferrer">
                            @if(publication.fileFormats().get(0).name() != null && !publication.fileFormats().get(0).name().isBlank())
                            ${publication.fileFormats().get(0).name()}
                            @else
                            ${publication.fileFormats().get(0).abbreviation()}
                            @endif
                        </a>
                        @else
                        <select class="download-select"
                                data-publication-id="${publication.identifier()}"
                                data-download-mode="download"
                                aria-label="Daten herunterladen">
                            <option value="">Format auswählen</option>
                            @for(ch.so.agi.sodata.domain.FileFormat fileFormat : publication.fileFormats())
                            <option value="${publication.downloadHostUrl()}/${publication.identifier()}/aktuell/${publication.identifier()}.${fileFormat.abbreviation()}">
                                @if(fileFormat.name() != null && !fileFormat.name().isBlank())
                                ${fileFormat.name()}
                                @else
                                ${fileFormat.abbreviation()}
                                @endif
                            </option>
                            @endfor
                        </select>
                        @endif
                    @endif
                @endif
            @endif
        </div>
    </div>
@endfor
</div>
@endif
