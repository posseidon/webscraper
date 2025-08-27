package hu.elte.inf.projects.webscraper.service.web;

import hu.elte.inf.projects.webscraper.service.pdf.Html2Pdf;
import hu.elte.inf.projects.webscraper.service.pdf.Text2Pdf;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class WebScraperService {

    private final WebScraper webScraper;
    private final Text2Pdf text2Pdf;
    private final Html2Pdf html2Pdf;

    @Autowired
    public WebScraperService(WebScraper webScraper, Text2Pdf text2Pdf, Html2Pdf html2Pdf) {
        this.webScraper = webScraper;
        this.text2Pdf = text2Pdf;
        this.html2Pdf = html2Pdf;
    }

    @Async
    public CompletableFuture<byte[]> scrape(String url) {
        String content = webScraper.scrape(url);
        if (content.isEmpty()) {
            return CompletableFuture.completedFuture(new byte[0]);
        }
        
        byte[] pdfBytes = text2Pdf.convertToPdf(content);
        return CompletableFuture.completedFuture(pdfBytes);
    }

    @Async
    public CompletableFuture<byte[]> scrapeHtml(String url) {
        Document document = webScraper.scrapeDocument(url);
        if (document == null) {
            return CompletableFuture.completedFuture(new byte[0]);
        }
        
        byte[] pdfBytes = html2Pdf.convertToPdf(document.html());
        return CompletableFuture.completedFuture(pdfBytes);
    }
}