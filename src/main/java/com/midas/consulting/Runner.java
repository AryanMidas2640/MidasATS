package com.midas.consulting;

        import org.apache.pdfbox.pdmodel.PDDocument;
        import org.apache.pdfbox.text.PDFTextStripper;
        import org.apache.poi.hwpf.HWPFDocument;
        import org.apache.poi.hwpf.extractor.WordExtractor;
        import org.apache.poi.xwpf.usermodel.XWPFDocument;
        import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

        import java.io.*;
        import java.nio.file.*;
        import java.util.*;
        import java.util.regex.*;

public class Runner {

    public static void main(String[] args) throws IOException {
        String folderPath = "C:\\Users\\Admin 1\\Downloads\\Downloads Folder\\Downloads Folder";

        Files.walk(Paths.get(folderPath))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String fileName = path.toString().toLowerCase();
                    try {
                        String text = "";
                        if (fileName.endsWith(".pdf")) {
                            text = extractTextFromPDF(path.toFile());
                        } else if (fileName.endsWith(".doc")) {
                            text = extractTextFromDOC(path.toFile());
                        } else if (fileName.endsWith(".docx")) {
                            text = extractTextFromDOCX(path.toFile());
                        }

                        if (!text.isEmpty()) {
                            List<String> emails = extractEmails(text);
                            System.out.println("File: " + path);
                            emails.forEach(email -> System.out.println("  Email: " + email));
                        }
                    } catch (Exception e) {
                        System.err.println("Error reading file " + path + ": " + e.getMessage());
                    }
                });
    }

    private static String extractTextFromPDF(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static String extractTextFromDOC(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument doc = new HWPFDocument(fis)) {
            return new WordExtractor(doc).getText();
        }
    }

    private static String extractTextFromDOCX(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument docx = new XWPFDocument(fis)) {
            return new XWPFWordExtractor(docx).getText();
        }
    }

    private static List<String> extractEmails(String text) {
        List<String> emails = new ArrayList<>();
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,6}");
        Matcher matcher = emailPattern.matcher(text);
        while (matcher.find()) {
            emails.add(matcher.group());
        }
        return emails;
    }
}
