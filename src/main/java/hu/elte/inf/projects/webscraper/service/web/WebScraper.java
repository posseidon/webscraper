package hu.elte.inf.projects.webscraper.service.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class WebScraper {

    private final ExecutorService executorService;

    public WebScraper() {
        int threadPoolSize = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public String scrape(String url) {
        try {
            Document document = Jsoup.connect(url)
                    .timeout(5000)
                    .get();
            return document.text();
        } catch (Exception e) {
            System.err.println("Error scraping URL: " + url + " - " + e.getMessage());
            return "";
        }
    }

    public Document scrapeDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .timeout(5000)
                    .get();
        } catch (Exception e) {
            System.err.println("Error scraping URL: " + url + " - " + e.getMessage());
            return null;
        }
    }

    public CompletableFuture<String> scrapeAsync(String url) {
        return CompletableFuture.supplyAsync(() -> scrape(url), executorService);
    }

    public CompletableFuture<Document> scrapeDocumentAsync(String url) {
        return CompletableFuture.supplyAsync(() -> scrapeDocument(url), executorService);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}