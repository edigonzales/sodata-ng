package ch.so.agi.sodata.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/ping")
    public ResponseEntity<String> ping(@RequestHeader Map<String, String> headers, HttpServletRequest request) {
        
        headers.forEach((key, value) -> {
            logger.info(String.format("Header '%s' = %s", key, value));
        });
        
        logger.info("server name: " + request.getServerName());
        logger.info("context path: " + request.getContextPath());
        
        logger.info("ping"); 
        
        return new ResponseEntity<String>("sodata", HttpStatus.OK);
    }
    
    @GetMapping(value = "/pong", produces = "text/html")
    public ModelAndView pong() {
        ModelAndView mav = new ModelAndView("pong");
        mav.addObject("name", "Pong");
        return mav;
    }

}
