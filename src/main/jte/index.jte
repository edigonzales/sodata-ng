<!DOCTYPE html>
<html lang="en">
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
    <link rel="stylesheet" href="sodata.css">
    
    <script type="module" src="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/index.js"></script>
    <script src="https://unpkg.com/htmx.org@2.0.8"></script>
    
  <style>
    body {
      margin: 0;
      background: var(--so-bg);
      color: var(--so-fg);
      font-family: var(--so-font-family);
    }

    main {
      max-width: 1400px;
      margin: 0 auto;
      padding: 24px;
    }
    
    h2 {
        font-size: 2.25rem;
        font-weight: 900;
    }
    
    p {
        font-size: 1.125rem;
        line-height: 1.5;
        margin: 9px, 0, 18px, 0;
    }
    
    a:hover {
        color: #e01f26;
    }
    
.search-input {
    width: 100%;
    height: 60px;
    background-color: #f5f5f7;
    border-top: 0px solid black;
    border-right: 0px solid black;
    border-bottom: 1px solid black;
    border-left: 0px solid black;
    box-sizing: border-box;
    padding: 5px 5px 0 30px; /* Added left padding for the icon */
    outline: none; /* Remove default blue/orange outline */
    font-size: 1.125rem;
    color: #333;
}

.search-input:focus {
    border-bottom: 1px solid #e01f26;
    outline: none;
}

.input-container {
  position: relative;
  width: 100%;
  margin-top: 60px; 
  
}
.magnifier-icon {
  position: absolute;
  top: 0;
  left: 5px; /* Position inside the field, before the text */
  height: 60px; /* Match input height */
  display: flex;
  align-items: center; /* Vertically center the icon */
  color: #666; /* Icon color */
  font-size: 16px; /* Icon size */
  pointer-events: none; /* Crucial to allow clicks to pass through */
  z-index: 1; /* Ensure icon is above input, if needed */
}

.clear-icon {
  position: absolute;
  top: 0;
  right: 5px; /* Position inside the field, after the text */
  height: 60px; /* Match input height */
  display: flex;
  align-items: center; /* Vertically center the icon */
  color: #666; /* Icon color */
  font-size: 16px; /* Icon size */
  cursor: pointer; /* Change cursor to pointer on hover */
  z-index: 1; /* Ensure icon is above input */
}    

.results {
  margin-top: 48px;
}

.publications-grid-header {
  display: grid;
  grid-template-columns: minmax(0, 2.4fr) minmax(180px, 0.9fr) minmax(120px, 0.5fr) minmax(230px, 1fr);
  border-bottom: 1px solid #DFDFDF;
  padding: 12px 14px;
  font-weight: 900;
        font-size: 1.125rem;
}

.publications-row {
  display: grid;
  grid-template-columns: minmax(0, 2.4fr) minmax(180px, 0.9fr) minmax(120px, 0.5fr) minmax(230px, 1fr);
  border-bottom: 1px solid #DFDFDF;
  padding: 16px 14px;
  align-items: center;
        font-size: 1.125rem;
  
}

/*
.publications-grid-body .publications-row:nth-child(even) {
  background-color: #f3f3f4;
}
*/

.publications-cell {
  padding-right: 12px;
  min-width: 0;
  
}

.publications-theme {
  font-weight: 500;
}



.meta-link {
  color: #3a3a3a;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.meta-link:hover {
  color: #111;
}

.meta-link .bi {
  font-size: 1.8rem;
}

.download-select {
  width: 100%;
  max-width: 280px;
  height: 38px;
  border: 1px solid #c9c9cb;
  border-radius: 6px;
  background-color: #efefef;
  color: #333;
  font-size: 1rem;
  padding: 0 12px;
}

.download-select:focus {
  outline: 2px solid #333;
  outline-offset: 1px;
}

.results-empty {
  color: #555;
  padding: 16px 14px;
}

@media (max-width: 900px) {
  .publications-grid-header {
    display: none;
  }

  .publications-row {
    grid-template-columns: 1fr;
    row-gap: 10px;
  }

  .publications-cell {
    display: grid;
    grid-template-columns: minmax(120px, 34%) 1fr;
    column-gap: 10px;
    align-items: center;
    padding-right: 0;
  }

  .publications-cell::before {
    content: attr(data-label);
    font-weight: 900;
    letter-spacing: 0.04em;
  }

  .download-select {
    max-width: 100%;
  }
}
    
  </style>

</head>

<body>
    <header>
      <so-header
        active-section="Verwaltung"
        site-name="Kanton Solothurn">
      </so-header>
    
      <so-breadcrumb>
        <so-breadcrumb-item href="https://so.ch">so.ch</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/">Verwaltung</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/bau-und-justizdepartement/">Bau- und Justizdepartement</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/bau-und-justizdepartement/amt-fuer-geoinformation/">Amt für Geoinformation</so-breadcrumb-item>
        <so-breadcrumb-item isCurrentPage>Datenbezug</so-breadcrumb-item>
      </so-breadcrumb>
    </header>
    
    <main>
        <h2>Geodaten Kanton Solothurn</h2>
        
        <p>Geodaten vom Kanton Solothurn können kostenlos heruntergeladen werden. Die Vektordaten sowie die Rasterdaten werden in vordefinierten Formaten und Gebieten (Kanton, Gemeinde oder andere) angeboten. Bei der Gebietseinteilung Gemeinde oder andere kann der Benutzer das gewünschte Gebiet selber wählen. In <a href="https://so.ch/verwaltung/bau-und-justizdepartement/amt-fuer-geoinformation/geoportal/geodienste/datenbezug/" target="_blank">unserer Hilfe</a> finden Sie weitere Informationen zu alternativen Bezugsmöglichkeiten.</p>
        <p>Rasterdaten werden als <a href="https://www.cogeo.org/">Cloud Optimized GeoTIFF</a> angeboten. Damit entfällt das mühsame Herunterladen einzelner Kacheln. Beim Klicken auf den Button wird der Link in die Zwischenablage kopiert und kann anschliessend in den gängigen GIS-Werkzeugen als Rasterdatenquelle eingefügt werden.</p>
        
        
        <div class="input-container">
    <span class="magnifier-icon">
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
                placeholder="Suchbegriff" autofocus/>

    <span class="clear-icon"
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

    </span>


        </div>     

        <div id="results"
             class="results"
             hx-get="/themepublications/fragment"
             hx-trigger="load"
             hx-swap="innerHTML">
        </div>
        
    </main>
    
    <footer>
    asdf
    </footer>
</body>
</html>
