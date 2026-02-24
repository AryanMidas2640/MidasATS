package com.midas.consulting.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DocumentConverter {

    // Method to convert .doc to text
//    public static String docToText(String filePath) throws IOException {
//        FileInputStream fis = new FileInputStream(new File(filePath));
//        HWPFDocument document = new HWPFDocument(fis);
//        WordExtractor extractor = new WordExtractor(document);
//        String text = extractor.getText();
//        extractor.close();
//        return text;
//    }

    // Method to convert .docx to text
    public static String docxToText(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(new File(filePath));
        XWPFDocument document = new XWPFDocument(fis);
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);
        String text = extractor.getText();
        extractor.close();
        return text;
    }

    // Method to convert .pdf to text
    public static String pdfToText(String filePath) throws IOException {
        PDDocument document = PDDocument.load(new File(filePath));
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        document.close();
        return text;
    }

    public static void main(String[] args) {
//        try {
//            // Convert .doc file to text
//            String docText = docToText("path_to_your_doc_file.doc");
//            System.out.println(".doc text:\n" + docText);
//
//            // Convert .docx file to text
//            String docxText = docxToText("path_to_your_docx_file.docx");
//            System.out.println(".docx text:\n" + docxText);
//
//            // Convert .pdf file to text
//            String pdfText = pdfToText("path_to_your_pdf_file.pdf");
//            System.out.println(".pdf text:\n" + pdfText);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
