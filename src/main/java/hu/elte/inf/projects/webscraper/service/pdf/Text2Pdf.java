package hu.elte.inf.projects.webscraper.service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class Text2Pdf {

    private static final int MARGIN = 50;
    private static final int FONT_SIZE = 12;
    private static final int LINE_HEIGHT = 14;

    public byte[] convertToPdf(String text) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            // Load Unicode font
            ClassPathResource fontResource = new ClassPathResource("fonts/DejaVuSans.ttf");
            PDType0Font font;
            
            try (InputStream fontStream = fontResource.getInputStream()) {
                font = PDType0Font.load(document, fontStream);
            }

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);

                // Split text into lines that fit the page width
                String[] lines = text.split("\n");
                float pageWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
                float currentY = page.getMediaBox().getHeight() - MARGIN;

                for (String line : lines) {
                    // Handle long lines by wrapping them
                    String[] wrappedLines = wrapText(line, font, pageWidth);

                    for (String wrappedLine : wrappedLines) {
                        if (currentY < MARGIN) {
                            // Start a new page
                            contentStream.endText();
                            contentStream.close();

                            page = new PDPage();
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            contentStream.beginText();
                            contentStream.setFont(font, FONT_SIZE);
                            contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);
                            currentY = page.getMediaBox().getHeight() - MARGIN;
                        }

                        contentStream.showText(wrappedLine);
                        contentStream.newLineAtOffset(0, -LINE_HEIGHT);
                        currentY -= LINE_HEIGHT;
                    }
                }
                
                contentStream.endText();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            System.err.println("Error creating PDF: " + e.getMessage());
            return new byte[0];
        }
    }

    private String[] wrapText(String text, PDType0Font font, float maxWidth) {
        if (text.isEmpty()) {
            return new String[]{""};
        }

        try {
            float textWidth = font.getStringWidth(text) / 1000 * Text2Pdf.FONT_SIZE;
            if (textWidth <= maxWidth) {
                return new String[]{text};
            }

            // Simple word wrapping
            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder();
            java.util.List<String> lines = new java.util.ArrayList<>();

            for (String word : words) {
                String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
                float testWidth = font.getStringWidth(testLine) / 1000 * Text2Pdf.FONT_SIZE;

                if (testWidth <= maxWidth) {
                    currentLine = new StringBuilder(testLine);
                } else {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        // Single word is too long, just add it
                        lines.add(word);
                    }
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }

            return lines.toArray(new String[0]);

        } catch (IOException e) {
            return new String[]{text};
        }
    }
}