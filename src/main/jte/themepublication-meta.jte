@param ch.so.agi.sodata.domain.ThemePublication publication

<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />

    <title>Datenbeschreibung • ${publication.title()} • Kanton Solothurn</title>

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
        <so-breadcrumb-item isCurrentPage>${publication.title()} (Datenbeschreibung)</so-breadcrumb-item>
    </so-breadcrumb>
</header>

<main class="so-main so-main--narrow meta-page">
    <h1 class="so-page-title">${publication.title()}</h1>

    <section class="meta-section">
        <h2 class="meta-section-title">Beschreibung</h2>
        @if(publication.shortDescription() != null && !publication.shortDescription().isBlank())
        <div class="meta-description">$unsafe{publication.shortDescription()}</div>
        @endif

        @if(publication.furtherInformation() != null && !publication.furtherInformation().isBlank())
        <p class="meta-further-information">
            Weiterführende Informationen:
            <a href="${publication.furtherInformation()}" target="_blank" rel="noopener noreferrer">${publication.furtherInformation()}</a>
        </p>
        @endif
    </section>

    <section class="meta-section">
        <h2 class="meta-section-title">Datum der letzten Publikation</h2>
        <p class="meta-value">
            @if(publication.lastPublishingDate() != null)
            ${publication.lastPublishingDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}
            @else
            -
            @endif
        </p>
    </section>

    <section class="meta-section">
        <h2 class="meta-section-title">Datenmodell</h2>
        <p class="meta-value">
            @if(publication.model() != null && !publication.model().isBlank())
            ${publication.model()}
            @else
            -
            @endif
        </p>
    </section>

    <section class="meta-section">
        <h2 class="meta-section-title">Lizenz</h2>
        <p class="meta-value"><a href="https://files.geo.so.ch/nutzungsbedingungen.html" target="_blank" rel="noopener noreferrer">https://files.geo.so.ch/nutzungsbedingungen.html</a></p>
    </section>

    <section class="meta-section">
        <h2 class="meta-section-title">Kontakt (fachlich)</h2>
        @if(publication.owner() != null)
        <div class="meta-contact">
            @if(publication.owner().agencyName() != null && !publication.owner().agencyName().isBlank())
            <p class="meta-contact-line">${publication.owner().agencyName()}</p>
            @endif
            @if(publication.owner().phone() != null && !publication.owner().phone().isBlank())
            <p class="meta-contact-line">Telefon ${publication.owner().phone()}</p>
            @endif
            @if(publication.owner().email() != null && !publication.owner().email().isBlank())
            <p class="meta-contact-line"><a href="${publication.owner().email()}">${publication.owner().email().replace("mailto:", "")}</a></p>
            @endif
            @if(publication.owner().officeAtWeb() != null && !publication.owner().officeAtWeb().isBlank())
            <p class="meta-contact-line"><a href="${publication.owner().officeAtWeb()}" target="_blank" rel="noopener noreferrer">${publication.owner().officeAtWeb().replaceFirst("^https?://", "")}</a></p>
            @endif
        </div>
        @else
        <p class="meta-value">-</p>
        @endif
    </section>

    <section class="meta-section">
        <h2 class="meta-section-title">Kontakt (technisch)</h2>
        @if(publication.servicer() != null)
        <div class="meta-contact">
            @if(publication.servicer().agencyName() != null && !publication.servicer().agencyName().isBlank())
            <p class="meta-contact-line">${publication.servicer().agencyName()}</p>
            @endif
            @if(publication.servicer().phone() != null && !publication.servicer().phone().isBlank())
            <p class="meta-contact-line">Telefon ${publication.servicer().phone()}</p>
            @endif
            @if(publication.servicer().email() != null && !publication.servicer().email().isBlank())
            <p class="meta-contact-line"><a href="${publication.servicer().email()}">${publication.servicer().email().replace("mailto:", "")}</a></p>
            @endif
            @if(publication.servicer().officeAtWeb() != null && !publication.servicer().officeAtWeb().isBlank())
            <p class="meta-contact-line"><a href="${publication.servicer().officeAtWeb()}" target="_blank" rel="noopener noreferrer">${publication.servicer().officeAtWeb().replaceFirst("^https?://", "")}</a></p>
            @endif
        </div>
        @else
        <p class="meta-value">-</p>
        @endif
    </section>

    <section class="meta-section">
        <h2 class="meta-section-title">Inhalt</h2>

        @if(publication.tablesInfo() != null && !publication.tablesInfo().isEmpty())
        @for(ch.so.agi.sodata.domain.TableInfo tableInfo : publication.tablesInfo())
        <div class="meta-layer-block">
            <h3 class="meta-layer-title">
                <span>${tableInfo.sqlName() != null ? tableInfo.sqlName() : "-"}</span>
                (${tableInfo.title() != null ? tableInfo.title() : "-"})
            </h3>

            @if(tableInfo.shortDescription() != null && !tableInfo.shortDescription().isBlank())
            <p class="meta-layer-description">$unsafe{tableInfo.shortDescription()}</p>
            @endif

            @if(tableInfo.attributesInfo() != null && !tableInfo.attributesInfo().isEmpty())
            <table class="meta-attributes-table">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Beschreibung</th>
                    <th>Datentyp</th>
                    <th>Pflichtattribut</th>
                </tr>
                </thead>
                <tbody>
                @for(ch.so.agi.sodata.domain.AttributeInfo attributeInfo : tableInfo.attributesInfo())
                <tr>
                    <td>${attributeInfo.name() != null ? attributeInfo.name() : "-"}</td>
                    <td>
                        @if(attributeInfo.shortDescription() != null && !attributeInfo.shortDescription().isBlank())
                        $unsafe{attributeInfo.shortDescription()}
                        @else
                        -
                        @endif
                    </td>
                    <td>${attributeInfo.datatype() != null ? attributeInfo.datatype() : "-"}</td>
                    <td>${Boolean.TRUE.equals(attributeInfo.mandatory()) ? "ja" : "nein"}</td>
                </tr>
                @endfor
                </tbody>
            </table>
            @else
            <p class="meta-value">Keine Attributinformationen vorhanden.</p>
            @endif
        </div>
        @endfor
        @else
        <p class="meta-value">Keine Tabelleninformationen vorhanden.</p>
        @endif
    </section>
</main>
</body>
</html>
