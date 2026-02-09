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
            <div class="download-badges">
                @for(ch.so.agi.sodata.domain.FileFormat fileFormat : ch.so.agi.sodata.domain.DownloadBadgeFormatSupport.orderedFormats(publication.fileFormats()))
                    @if(Boolean.TRUE.equals(publication.hasSubunits()))
                    <a class="download-badge download-badge-link"
                       href="/themepublication/data/${publication.identifier()}/${fileFormat.abbreviation()}"
                       target="_blank"
                       rel="noopener noreferrer">
                        ${ch.so.agi.sodata.domain.DownloadBadgeFormatSupport.badgeLabel(publication, fileFormat)}
                    </a>
                    @else
                        @if(publication.model() == null || publication.model().isBlank())
                        <span class="download-badge download-badge-copy"
                              role="button"
                              tabindex="0"
                              data-copy-url="${publication.downloadHostUrl()}/${publication.identifier()}/aktuell/${publication.identifier()}.${fileFormat.abbreviation()}"
                              data-tooltip="Link wurde in Zwischenablage kopiert."
                              hx-on:click="if (navigator.clipboard && window.isSecureContext) { navigator.clipboard.writeText(this.dataset.copyUrl); } this.classList.remove('is-copied'); void this.offsetWidth; this.classList.add('is-copied');"
                              hx-on:keydown="if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); this.click(); }"
                              aria-label="Dateilink in Zwischenablage kopieren">
                            ${ch.so.agi.sodata.domain.DownloadBadgeFormatSupport.badgeLabel(publication, fileFormat)}
                        </span>
                        @else
                        <a class="download-badge download-badge-link"
                           href="${publication.downloadHostUrl()}/${publication.identifier()}/aktuell/${publication.identifier()}.${fileFormat.abbreviation()}"
                           target="_blank"
                           rel="noopener noreferrer">
                            ${ch.so.agi.sodata.domain.DownloadBadgeFormatSupport.badgeLabel(publication, fileFormat)}
                        </a>
                        @endif
                    @endif
                @endfor
                @if(ch.so.agi.sodata.domain.DownloadBadgeFormatSupport.containsAbbreviation(publication.fileFormats(), "gpkg.zip"))
                <a class="download-badge download-badge-link"
                   href="/themepublication/formats/${publication.identifier()}"
                   target="_blank"
                   rel="noopener noreferrer">
                    mehr Formate
                </a>
                @endif
            </div>
            @endif
        </div>
    </div>
@endfor
</div>
@endif
