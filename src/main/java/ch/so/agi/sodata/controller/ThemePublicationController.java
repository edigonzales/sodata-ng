package ch.so.agi.sodata.controller;

import ch.so.agi.sodata.domain.ThemePublication;
import ch.so.agi.sodata.service.InvalidLuceneQueryException;
import ch.so.agi.sodata.service.LuceneSearcherException;
import ch.so.agi.sodata.service.ThemePublicationIndexService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/themepublications")
public class ThemePublicationController {
    private final ThemePublicationIndexService indexService;

    public ThemePublicationController(ThemePublicationIndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping
    public List<ThemePublication> findThemePublications(
            @RequestParam(name = "query", required = false) String query
    ) throws InvalidLuceneQueryException, LuceneSearcherException {
        if (query == null || query.isBlank()) {
            return indexService.findAllSortedByTitle();
        }
        return indexService.search(query);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidLuceneQueryException.class)
    public String handleInvalidQuery(InvalidLuceneQueryException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler(LuceneSearcherException.class)
    public String handleLuceneError(LuceneSearcherException ex) {
        return ex.getMessage();
    }
}
