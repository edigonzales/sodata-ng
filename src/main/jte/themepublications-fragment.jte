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
            <select class="download-select" aria-label="Daten herunterladen">
                <option>INTERLIS</option>
                <option>GeoPackage</option>
                <option>Shapefile</option>
                <option>DXF</option>
            </select>
        </div>
    </div>
@endfor
</div>
@endif
