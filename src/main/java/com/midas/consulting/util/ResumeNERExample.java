//package com.midas.consulting.util;
//
//
//import java.util.List;
//import java.util.Properties;
//
//public class ResumeNERExample {
//
//    public static void main(String[] args) {
//        // Create StanfordCoreNLP object with NER pipeline
//        Properties props = new Properties();
//        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
//        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//
//        // Sample resume text
//        String resumeText = "Experienced Java developer with certifications in AWS, PMP, and Scrum Master.";
//
//        // Create an Annotation with the resume text
//        Annotation document = new Annotation(resumeText);
//
//        // Run all annotators on the resume text
//        pipeline.annotate(document);
//
//        // Iterate over each sentence in the resume text
//        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//        for (CoreMap sentence : sentences) {
//            // Iterate over each token in the sentence
//            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                // Extract named entities (NER)
//                String nerTag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//                if (nerTag.equals("PERSON") || nerTag.equals("ORGANIZATION") || nerTag.equals("LOCATION")) {
//                    // Handle named entities (if needed)
//                    String word = token.get(CoreAnnotations.TextAnnotation.class);
//                    System.out.println("Entity: " + word + ", Type: " + nerTag);
//                }
//            }
//        }
//    }
//}
