package com.midas.consulting.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
    // Convert PDF to plain text
    public static String convertPdfToText(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    // Convert Word to plain text
//    public static String convertWordToText(String filePath) throws IOException {
//        try (FileInputStream fis = new FileInputStream(filePath);
//             XWPFDocument document = new XWPFDocument(fis)) {
//            StringBuilder text = new StringBuilder();
//            List<XWPFParagraph> paragraphs = document.getParagraphs();
//            for (XWPFParagraph paragraph : paragraphs) {
//                text.append(paragraph.getText()).append("\n");
//            }
//            return text.toString();
//        }
//    }
    public static String convertWordToText(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            StringBuilder text = new StringBuilder();

            // Extract headers
            for (XWPFHeader header : document.getHeaderList()) {
                text.append(header.getText()).append("\n");
            }

            // Extract paragraphs
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }

            // Extract tables
            List<XWPFTable> tables = document.getTables();
            for (XWPFTable table : tables) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        text.append(cell.getText()).append("\t");
                    }
                    text.append("\n");
                }
                text.append("\n");
            }

            // Extract footers
            for (XWPFFooter footer : document.getFooterList()) {
                text.append(footer.getText()).append("\n");
            }

            return text.toString();
        }
    }
    public static String convertDocToText(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }



    public static String extractEmail(String text) {
        // Regular expression for email addresses
        String emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(text);

        // Regular expression for phone numbers
        String phoneRegex = "\\b\\d{10}\\b|\\+?\\d{1,4}?[\\s-]?\\(?\\d{1,3}?\\)?[\\s-]?\\d{1,4}[\\s-]?\\d{1,4}[\\s-]?\\d{1,9}\\b";
        Pattern phonePattern = Pattern.compile(phoneRegex);
//        Matcher phoneMatcher = phonePattern.matcher(text);

        // Extract and print email addresses
        System.out.println("Email addresses found:");
        String matchingEmails= "";
        while (emailMatcher.find()) {
            matchingEmails+= emailMatcher.group() +",";
        }
    return     matchingEmails;
//        // Extract and print phone numbers
//        System.out.println("Phone numbers found:");
//        while (phoneMatcher.find()) {
//            System.out.println(phoneMatcher.group());
//        }
    }

}
