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
</main>
</body>
</html>
