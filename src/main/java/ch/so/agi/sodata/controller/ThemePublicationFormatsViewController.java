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
@RequestMapping("/themepublication/formats")
public class ThemePublicationFormatsViewController {
    private final ThemePublicationIndexService indexService;

    public ThemePublicationFormatsViewController(ThemePublicationIndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping(value = "/{identifier}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView themePublicationFormats(@PathVariable("identifier") String identifier)
            throws LuceneSearcherException {
        Optional<ThemePublication> publication = indexService.findByIdentifier(identifier);
        if (publication.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Theme publication not found.");
        }

        ModelAndView modelAndView = new ModelAndView("themepublication-formats");
        modelAndView.addObject("publication", publication.get());
        return modelAndView;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(LuceneSearcherException.class)
    public String handleLuceneError(LuceneSearcherException ex) {
        return ex.getMessage();
    }
}
