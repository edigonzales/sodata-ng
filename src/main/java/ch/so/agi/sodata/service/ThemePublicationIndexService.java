package ch.so.agi.sodata.service;

import ch.so.agi.sodata.config.IndexingProperties;
import ch.so.agi.sodata.domain.ThemePublication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import jakarta.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class ThemePublicationIndexService implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(ThemePublicationIndexService.class);

    private final Directory directory;
    private final Analyzer analyzer;
    private final ObjectMapper objectMapper;
    private final IndexingProperties indexingProperties;

    public ThemePublicationIndexService(IndexingProperties indexingProperties, ObjectMapper objectMapper) throws IOException {
        this.indexingProperties = indexingProperties;
        this.objectMapper = objectMapper;
        Path indexPath = Path.of(indexingProperties.directory());
        Files.createDirectories(indexPath);
        this.directory = new NIOFSDirectory(indexPath);
        this.analyzer = new StandardAnalyzer();
    }

    public void rebuildIndex(List<ThemePublication> publications) throws IOException {
        if (publications == null) {
            publications = Collections.emptyList();
        }

        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        try (IndexWriter writer = new IndexWriter(directory, writerConfig)) {
            for (ThemePublication publication : publications) {
                if (publication.identifier() == null || publication.identifier().isBlank()) {
                    log.warn("Skipping theme publication without identifier");
                    continue;
                }
                Document document = toDocument(publication);
                writer.updateDocument(new Term("id", lower(publication.identifier())), document);
            }
            writer.commit();
            log.info("{} files indexed.", writer.getDocStats().numDocs);
        }
    }

    public List<ThemePublication> findAllSortedByTitle() throws LuceneSearcherException {
        return searchInternal(new MatchAllDocsQuery(), null);
    }

    public List<ThemePublication> search(String searchTerms) throws InvalidLuceneQueryException, LuceneSearcherException {
        if (searchTerms == null || searchTerms.isBlank()) {
            return findAllSortedByTitle();
        }
        return searchInternal(buildQuery(searchTerms), searchTerms);
    }

    private List<ThemePublication> searchInternal(Query query, String searchTerms) throws LuceneSearcherException {
        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            int maxRecords = Math.max(indexingProperties.queryMaxRecords(), 1);
            TopDocs docs;
            if (query instanceof MatchAllDocsQuery) {
                Sort sort = new Sort(new SortField("title_sort", SortField.Type.STRING));
                docs = searcher.search(query, reader.numDocs(), sort);
            } else {
                docs = searcher.search(query, maxRecords);
            }

            List<ThemePublication> results = new ArrayList<>(docs.scoreDocs.length);
            StoredFields storedFields = reader.storedFields();
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                Document document = storedFields.document(scoreDoc.doc);
                String payload = document.get("payload");
                if (payload == null) {
                    continue;
                }
                results.add(objectMapper.readValue(payload, ThemePublication.class));
            }
            if (searchTerms != null) {
                log.debug("Lucene query '{}' returned {} hits.", searchTerms, results.size());
            }
            return results;
        } catch (IOException e) {
            throw new LuceneSearcherException(e.getMessage(), e);
        }
    }

    private Query buildQuery(String searchTerms) throws InvalidLuceneQueryException {
        String[] tokens = searchTerms.trim().split("\\s+");
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

        for (String rawToken : tokens) {
            String token = sanitizeToken(rawToken);
            if (token.isBlank()) {
                continue;
            }
            BooleanQuery.Builder tokenQuery = new BooleanQuery.Builder();
            tokenQuery.add(new BoostQuery(new TermQuery(new Term("id", token)), 200f), BooleanClause.Occur.SHOULD);
            tokenQuery.add(new BoostQuery(new TermQuery(new Term("title", token)), 20f), BooleanClause.Occur.SHOULD);
            tokenQuery.add(boostedWildcard("id", token, 100f), BooleanClause.Occur.SHOULD);
            tokenQuery.add(boostedWildcard("model", token, 2f), BooleanClause.Occur.SHOULD);
            tokenQuery.add(boostedWildcard("title", token, 10f), BooleanClause.Occur.SHOULD);
            tokenQuery.add(boostedWildcard("shortdescription", token, 2f), BooleanClause.Occur.SHOULD);
            tokenQuery.add(boostedWildcard("owner", token, 2f), BooleanClause.Occur.SHOULD);
            tokenQuery.add(boostedWildcard("keywords", token, 2f), BooleanClause.Occur.SHOULD);
            tokenQuery.add(boostedWildcard("synonyms", token, 2f), BooleanClause.Occur.SHOULD);
            queryBuilder.add(tokenQuery.build(), BooleanClause.Occur.MUST);
        }

        BooleanQuery query = queryBuilder.build();
        if (query.clauses().isEmpty()) {
            throw new InvalidLuceneQueryException("Search term did not contain valid tokens.");
        }
        return query;
    }

    private Query boostedWildcard(String field, String token, float boost) {
        Query wildcard = new WildcardQuery(new Term(field, "*" + token + "*"));
        return new BoostQuery(wildcard, boost);
    }

    private Document toDocument(ThemePublication publication) throws JsonProcessingException {
        Document document = new Document();
        document.add(new StringField("id", lower(publication.identifier()), Field.Store.YES));
        addText(document, "model", publication.model());
        addText(document, "title", publication.title());
        addText(document, "shortdescription", publication.shortDescription());

        if (publication.owner() != null) {
            String owner = String.join(" ",
                    safe(publication.owner().agencyName()),
                    safe(publication.owner().abbreviation()),
                    safe(publication.owner().division()));
            addText(document, "owner", owner);
        }

        if (publication.keywords() != null) {
            addText(document, "keywords", String.join(" ", publication.keywords()));
        }
        if (publication.synonyms() != null) {
            addText(document, "synonyms", String.join(" ", publication.synonyms()));
        }

        if (publication.title() != null) {
            document.add(new SortedDocValuesField("title_sort", new BytesRef(lower(publication.title()))));
        }

        document.add(new StoredField("payload", objectMapper.writeValueAsString(publication)));
        return document;
    }

    private void addText(Document document, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        document.add(new TextField(field, lower(value), Field.Store.NO));
    }

    private String lower(String value) {
        if (value == null) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String sanitizeToken(String token) {
        if (token == null) {
            return "";
        }
        String cleaned = token.replaceAll("[*?]", "");
        return cleaned.toLowerCase(Locale.ROOT).trim();
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        directory.close();
        analyzer.close();
    }
}
