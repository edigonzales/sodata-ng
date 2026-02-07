<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />

    <title>Geodatenbezug • Kanton Solothurn</title>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/styles/reset.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/styles/fonts.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/styles/tokens.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
    <link rel="stylesheet" href="/sodata.css">

    <script type="module" src="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/index.js"></script>
    <script src="https://unpkg.com/htmx.org@2.0.8"></script>
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
        <so-breadcrumb-item isCurrentPage>Datenbezug</so-breadcrumb-item>
    </so-breadcrumb>
</header>

<main class="so-main so-main--wide">
    <h1 class="so-page-title">Geodaten Kanton Solothurn</h1>

    <div class="lead-text">
        <p>Geodaten vom Kanton Solothurn können kostenlos heruntergeladen werden. Die Vektordaten sowie die Rasterdaten werden in vordefinierten Formaten und Gebieten (Kanton, Gemeinde oder andere) angeboten. Bei der Gebietseinteilung Gemeinde oder andere kann der Benutzer das gewünschte Gebiet selber wählen. In <a href="https://so.ch/verwaltung/bau-und-justizdepartement/amt-fuer-geoinformation/geoportal/geodienste/datenbezug/" target="_blank" rel="noopener noreferrer">unserer Hilfe</a> finden Sie weitere Informationen zu alternativen Bezugsmöglichkeiten.</p>
        <p>Rasterdaten werden als <a href="https://www.cogeo.org/" target="_blank" rel="noopener noreferrer">Cloud Optimized GeoTIFF</a> angeboten. Damit entfällt das mühsame Herunterladen einzelner Kacheln. Beim Klicken auf den Button wird der Link in die Zwischenablage kopiert und kann anschliessend in den gängigen GIS-Werkzeugen als Rasterdatenquelle eingefügt werden.</p>
    </div>

    <div class="search-container">
        <span class="search-icon" aria-hidden="true">
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="1.2rem"
                 height="1.2rem"
                 fill="#333"
                 viewBox="0 0 16 16">
                <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001q.044.06.098.115l3.85
                     3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1 1 0 0
                     0-.115-.1zM12 6.5a5.5 5.5 0 1
                     1-11 0 5.5 5.5 0 0 1 11 0"/>
            </svg>
        </span>

        <input id="search-input" class="search-input"
               type="text"
               name="query"
               hx-get="/themepublications/fragment"
               hx-target="#results"
               hx-trigger="input changed delay:300ms"
               hx-swap="innerHTML"
               autocomplete="off"
               placeholder="Suchbegriff" autofocus />

        <button type="button"
                class="clear-button"
                aria-label="Suchfeld leeren"
                hx-on:click="document.querySelector('#search-input').value = ''; htmx.trigger('#search-input', 'input');">
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="1.2rem"
                 height="1.2rem"
                 fill="#333"
                 viewBox="0 0 16 16">
                <path d="M4.646 4.646a.5.5 0 0 1 .708
                     0L8 7.293l2.646-2.647a.5.5 0 0
                     1 .708.708L8.707 8l2.647
                     2.646a.5.5 0 0 1-.708.708L8
                     8.707l-2.646 2.647a.5.5 0 0
                     1-.708-.708L7.293 8 4.646
                     5.354a.5.5 0 0 1 0-.708"/>
            </svg>
        </button>
    </div>

    <div id="results"
         class="results"
         hx-get="/themepublications/fragment"
         hx-trigger="load"
         hx-swap="innerHTML">
    </div>
</main>

<footer>
</footer>
</body>
</html>
