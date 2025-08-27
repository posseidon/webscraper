package hu.elte.inf.projects.webscraper.controller;

import hu.elte.inf.projects.webscraper.service.web.WebScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/scrape")
public class WebScraperController {

    private final WebScraperService webScraperService;

    public WebScraperController(WebScraperService webScraperService){
        this.webScraperService = webScraperService;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<InputStreamResource>> scrapeWebsite(
            @RequestParam String url,
            @RequestParam(defaultValue = "scraped_content") String fileName) {
        
        return webScraperService.scrape(url).thenApply(pdfBytes -> {
            if (pdfBytes.length == 0) {
                return ResponseEntity.badRequest().build();
            }
            
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        });
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<InputStreamResource>> scrapeWebsiteHtml(
            @RequestParam String url,
            @RequestParam(defaultValue = "scraped_content") String fileName) {
        
        return webScraperService.scrapeHtml(url).thenApply(pdfBytes -> {
            if (pdfBytes.length == 0) {
                return ResponseEntity.badRequest().build();
            }
            
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        });
    }
}