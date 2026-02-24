package com.midas.consulting;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ResumeEmailExtractor {

    public static void main(String[] args) {
        // ✅ Change this path to your target folder
        String rootFolderPath = "C:\\Users\\dell\\Downloads\\Downloads Folder\\Downloads Folder";

        try (Stream<Path> paths = Files.walk(Paths.get(rootFolderPath))) {
            long fileCount = paths
                    .filter(Files::isRegularFile)  // Count only files (not directories)
                    .count();

            System.out.println("✅ Total number of files: " + fileCount);
        } catch (IOException e) {
            System.err.println("❌ Error accessing folder: " + e.getMessage());
        }
    }
    public static void main1(String[] args) throws IOException {
        String folderPath = "C:\\Users\\dell\\Downloads\\Downloads Folder\\Downloads Folder";
        String csvOutputPath = "emails_output.csv";

        List<String[]> emailData = new ArrayList<>();
        emailData.add(new String[]{"Filename", "Email"});

        // Recursive file walk through all subdirectories
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
                            for (String email : emails) {
                                emailData.add(new String[]{path.getFileName().toString(), email});
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Error reading file: " + path + " - " + e.getMessage());
                    }
                });

        saveToCSV(csvOutputPath, emailData);
        System.out.println("✅ Emails saved to: " + csvOutputPath);
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

    private static void saveToCSV(String filePath, List<String[]> rows) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (String[] row : rows) {
                writer.println(String.join(",", row));
            }
        } catch (IOException e) {
            System.err.println("❌ Error writing to CSV: " + e.getMessage());
        }
    }
}
