@param ch.so.agi.sodata.domain.ThemePublication publication
@param String format

<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />

    <title>Datenbezug • ${publication.title()} • ${format} • Kanton Solothurn</title>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/styles/reset.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/styles/fonts.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/styles/tokens.css" />
    <link rel="stylesheet" href="/sodata.css">

    <script type="module" src="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/index.js"></script>
    <script type="module" src="https://cdn.jsdelivr.net/npm/@maps4html/mapml@latest/dist/mapml.js"></script>
</head>

<body class="so-page">
<header class="so-page-header">
    <so-header
            active-section="Verwaltung"
            site-name="Kanton Solothurn">
    </so-header>

    <so-breadcrumb class="so-page-breadcrumb">
        <so-breadcrumb-item href="https://so.ch">so.ch</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/">Verwaltung</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/bau-und-justizdepartement/">Bau- und Justizdepartement</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/bau-und-justizdepartement/amt-fuer-geoinformation/">Amt für Geoinformation</so-breadcrumb-item>
        <so-breadcrumb-item href="/">Datenbezug</so-breadcrumb-item>
        <so-breadcrumb-item isCurrentPage>${publication.title()} (Datenauswahl)</so-breadcrumb-item>
    </so-breadcrumb>
</header>

<main class="so-main so-main--narrow">
    <h1 class="so-page-title">${publication.title()}</h1>

    <section class="meta-section">
        <h2 class="meta-section-title">Datenauswahl (Platzhalter)</h2>
        <p class="meta-value">Die Logik fuer Datensaetze mit Subunits wird hier als naechstes implementiert.</p>
        <p class="meta-value">Datensatz-Identifier: <strong>${publication.identifier()}</strong></p>
        <p class="meta-value">Gewaehltes Format: <strong>${format}</strong></p>
    </section>

    <section class="meta-section webmap-section">
        <h2 class="meta-section-title">Webkarte</h2>
        <p class="meta-value">Vorschaukarte fuer die Datenauswahl (MapML, erster Layer Hintergrundkarte).</p>
        <mapml-viewer class="webmap-viewer" projection="OSMTILE" lat="47.2943" lon="7.6511" zoom="10" controls>
            <map-layer label="Hintergrundkarte" checked>
                <map-extent label="Plan für das Grundbuch" units="OSMTILE" opacity="0.66" checked hidden>
                    <map-input name="w" type="width"></map-input>
                    <map-input name="h" type="height"></map-input>
                    <map-input name="z" type="zoom" value="24" min="17" max="24" step="1"></map-input>
                    <map-input name="xmin" type="location" units="pcrs" position="top-left" axis="easting" min="783975" max="926967"></map-input>
                    <map-input name="ymin" type="location" units="pcrs" position="bottom-left" axis="northing" min="5945675" max="6034311"></map-input>
                    <map-input name="xmax" type="location" units="pcrs" position="top-right" axis="easting" min="783975" max="926967"></map-input>
                    <map-input name="ymax" type="location" units="pcrs" position="top-left" axis="northing" min="5945675" max="6034311"></map-input>
                    <map-link rel="image" tref="https://geo.so.ch/wms?REQUEST=GetMap&crs=EPSG:3857&SERVICE=WMS&BBOX={xmin},{ymin},{xmax},{ymax}&LAYERS=ch.so.agi.hintergrundkarte_sw&FORMAT=image/png&WIDTH={w}&HEIGHT={h}&STYLES=&TRANSPARENT=FALSE&version=1.3.0"></map-link>
                </map-extent>
                <map-extent label="Landeskarte" units="OSMTILE" opacity="0.66" checked hidden>
                    <map-input name="z" type="zoom" min="0" max="16"></map-input>
                    <map-input name="x" type="location" units="tilematrix" axis="column" min="0" max="262144"></map-input>
                    <map-input name="y" type="location" units="tilematrix" axis="row" min="0" max="262144"></map-input>
                    <map-link rel="tile" tref="https://wmts.geo.admin.ch/1.0.0/ch.swisstopo.pixelkarte-grau/default/current/3857/{z}/{x}/{y}.jpeg"></map-link>
                </map-extent>
            </map-layer>
            <map-layer label="${publication.title()} (Subunits)"
                       src="/themepublication/data/${publication.identifier()}/${format}/subunits.mapml"
                       checked>
            </map-layer>
        </mapml-viewer>
    </section>
</main>
</body>
</html>
