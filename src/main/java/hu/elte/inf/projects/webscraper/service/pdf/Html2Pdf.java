package hu.elte.inf.projects.webscraper.service.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class Html2Pdf {

    public byte[] convertToPdf(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // Wrap the content in a proper HTML document if it's not already
            String fullHtmlContent = htmlContent;
            if (!htmlContent.toLowerCase().contains("<html")) {
                fullHtmlContent = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
                    + "body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; } "
                    + "h1, h2, h3 { color: #333; } "
                    + "p { margin: 10px 0; } "
                    + "table { border-collapse: collapse; width: 100%; margin: 10px 0; } "
                    + "th, td { border: 1px solid #ddd; padding: 8px; text-align: left; } "
                    + "th { background-color: #f2f2f2; }"
                    + "</style></head><body>" + htmlContent + "</body></html>";
            }

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(fullHtmlContent, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (IOException e) {
            System.err.println("Error creating PDF from HTML: " + e.getMessage());
            return new byte[0];
        } catch (Exception e) {
            System.err.println("Unexpected error creating PDF from HTML: " + e.getMessage());
            return new byte[0];
        }
    }
}