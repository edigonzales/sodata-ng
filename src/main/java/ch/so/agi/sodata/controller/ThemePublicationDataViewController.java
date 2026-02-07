package ch.so.agi.sodata.controller;

import java.util.Optional;

import ch.so.agi.sodata.domain.ThemePublication;
import ch.so.agi.sodata.service.LuceneSearcherException;
import ch.so.agi.sodata.service.ThemePublicationIndexService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/themepublication/data")
public class ThemePublicationDataViewController {
    private final ThemePublicationIndexService indexService;

    public ThemePublicationDataViewController(ThemePublicationIndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping(value = "/{identifier}/{format:.+}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView themePublicationData(
            @PathVariable("identifier") String identifier,
            @PathVariable("format") String format
    ) throws LuceneSearcherException {
        Optional<ThemePublication> publication = indexService.findByIdentifier(identifier);
        if (publication.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Theme publication not found.");
        }

        ModelAndView modelAndView = new ModelAndView("themepublication-data");
        modelAndView.addObject("publication", publication.get());
        modelAndView.addObject("format", format);
        return modelAndView;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(LuceneSearcherException.class)
    public String handleLuceneError(LuceneSearcherException ex) {
        return ex.getMessage();
    }
}
