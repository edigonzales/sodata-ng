package ch.so.agi.sodata.controller;

import java.util.List;

import ch.so.agi.sodata.domain.ThemePublication;
import ch.so.agi.sodata.service.InvalidLuceneQueryException;
import ch.so.agi.sodata.service.LuceneSearcherException;
import ch.so.agi.sodata.service.ThemePublicationIndexService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/themepublications")
public class ThemePublicationViewController {
    private final ThemePublicationIndexService indexService;

    public ThemePublicationViewController(ThemePublicationIndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView findThemePublicationsFragment(
            @RequestParam(name = "query", required = false) String query
    ) throws InvalidLuceneQueryException, LuceneSearcherException {
        List<ThemePublication> publications;
        if (query == null || query.isBlank()) {
            publications = indexService.findAllSortedByTitle();
        } else {
            publications = indexService.search(query);
        }

        ModelAndView modelAndView = new ModelAndView("themepublications-fragment");
        modelAndView.addObject("publications", publications);
        return modelAndView;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidLuceneQueryException.class)
    public String handleInvalidQuery(InvalidLuceneQueryException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(LuceneSearcherException.class)
    public String handleLuceneError(LuceneSearcherException ex) {
        return ex.getMessage();
    }
}
