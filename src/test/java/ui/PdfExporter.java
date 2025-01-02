package ui;

import model.DiaryEntry;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.*;
import java.io.File;
import java.util.List;

public class PdfExporter {

    public static void exportEntriesToPDF(List<DiaryEntry> entries, File outputFile) {
        try (PDDocument document = new PDDocument()) {
            // Load a default font (adjust path as needed or use PDType1Font)
            // Alternatively, you can do: PDType1Font.HELVETICA
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();

            // If you’re using a TTF, load it (example):
            // PDType0Font font = PDType0Font.load(document, new File("fonts/Roboto-Regular.ttf"));
            // contentStream.setFont(font, 12);
            // For demonstration, we’ll just use a built-in font
            contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);

            contentStream.newLineAtOffset(50, 700);

            for (DiaryEntry entry : entries) {
                // Add content
                contentStream.showText("Title: " + entry.getTitle());
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Date: " + entry.getCreatedDate().toString());
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Mood: " + entry.getMood());
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Content: " + entry.getContent());
                contentStream.newLineAtOffset(0, -30);
            }

            contentStream.endText();
            contentStream.close();

            document.save(outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
