package com.midas.consulting.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class TemplateLoader {

    private static final Logger logger = LoggerFactory.getLogger(TemplateLoader.class);

    public static String loadTemplate(String templateName) {
        try {
            // Try to load from classpath first
            ClassPathResource resource = new ClassPathResource("email-templates/" + templateName);
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                }
            }

            // Fallback to default templates if file not found
            return getDefaultTemplate(templateName);

        } catch (Exception e) {
            logger.warn("Failed to load template {}, using default: {}", templateName, e.getMessage());
            return getDefaultTemplate(templateName);
        }
    }

    private static String getDefaultTemplate(String templateName) {
        switch (templateName) {
            case "password-reset.html":
                return getPasswordResetTemplate();
            case "welcome-user.html":
                return getWelcomeUserTemplate();
            case "application-received.html":
                return getApplicationReceivedTemplate();
            case "interview-invitation.html":
                return getInterviewInvitationTemplate();
            default:
                return getBasicTemplate();
        }
    }

    private static String getPasswordResetTemplate() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Password Reset</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <div style=\"text-align: center; margin-bottom: 30px;\">" +
                "            <img src=\"{{logoUrl}}\" alt=\"{{tenantName}}\" style=\"max-height: 60px;\">" +
                "        </div>" +
                "        <h2 style=\"color: #2c3e50;\">Password Reset Request</h2>" +
                "        <p>Hello {{userName}},</p>" +
                "        <p>We received a request to reset your password. Click the button below to reset it:</p>" +
                "        <div style=\"text-align: center; margin: 30px 0;\">" +
                "            <a href=\"{{resetLink}}\" style=\"background-color: #3498db; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;\">Reset Password</a>" +
                "        </div>" +
                "        <p>If you didn't request this password reset, please ignore this email.</p>" +
                "        <p>This link will expire in 24 hours for security reasons.</p>" +
                "        <hr style=\"margin: 30px 0; border: none; border-top: 1px solid #eee;\">" +
                "        <p style=\"font-size: 12px; color: #666;\">{{tenantName}} - {{currentYear}}</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private static String getWelcomeUserTemplate() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Welcome</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <div style=\"text-align: center; margin-bottom: 30px;\">" +
                "            <img src=\"{{logoUrl}}\" alt=\"{{tenantName}}\" style=\"max-height: 60px;\">" +
                "        </div>" +
                "        <h2 style=\"color: #27ae60;\">Welcome to {{tenantName}}!</h2>" +
                "        <p>Hello {{userName}},</p>" +
                "        <p>Your account has been created successfully. Here are your login details:</p>" +
                "        <div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;\">" +
                "            <p><strong>Role:</strong> {{userRole}}</p>" +
                "            <p><strong>Temporary Password:</strong> {{tempPassword}}</p>" +
                "            <p><strong>Login URL:</strong> <a href=\"{{loginUrl}}\">{{loginUrl}}</a></p>" +
                "        </div>" +
                "        <p>Please log in and change your password on first access.</p>" +
                "        <div style=\"text-align: center; margin: 30px 0;\">" +
                "            <a href=\"{{loginUrl}}\" style=\"background-color: #27ae60; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;\">Login Now</a>" +
                "        </div>" +
                "        <hr style=\"margin: 30px 0; border: none; border-top: 1px solid #eee;\">" +
                "        <p style=\"font-size: 12px; color: #666;\">{{tenantName}} - {{currentYear}}</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private static String getApplicationReceivedTemplate() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Application Received</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <div style=\"text-align: center; margin-bottom: 30px;\">" +
                "            <img src=\"{{logoUrl}}\" alt=\"{{tenantName}}\" style=\"max-height: 60px;\">" +
                "        </div>" +
                "        <h2 style=\"color: #2980b9;\">Application Received</h2>" +
                "        <p>Dear {{candidateName}},</p>" +
                "        <p>Thank you for your interest in the <strong>{{jobTitle}}</strong> position at {{tenantName}}.</p>" +
                "        <p>We have successfully received your application (ID: {{applicationId}}) and our team will review it carefully.</p>" +
                "        <div style=\"background-color: #e8f4f8; padding: 20px; border-radius: 5px; margin: 20px 0;\">" +
                "            <h3 style=\"color: #2980b9; margin-top: 0;\">What's Next?</h3>" +
                "            <p>• Our hiring team will review your application</p>" +
                "            <p>• We'll contact you within 5-7 business days</p>" +
                "            <p>• Keep an eye on your email for updates</p>" +
                "        </div>" +
                "        <p>If you have any questions, please don't hesitate to contact us.</p>" +
                "        <hr style=\"margin: 30px 0; border: none; border-top: 1px solid #eee;\">" +
                "        <p style=\"font-size: 12px; color: #666;\">{{tenantName}} - {{currentYear}}</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private static String getInterviewInvitationTemplate() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Interview Invitation</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <div style=\"text-align: center; margin-bottom: 30px;\">" +
                "            <img src=\"{{logoUrl}}\" alt=\"{{tenantName}}\" style=\"max-height: 60px;\">" +
                "        </div>" +
                "        <h2 style=\"color: #e74c3c;\">Interview Invitation</h2>" +
                "        <p>Dear {{candidateName}},</p>" +
                "        <p>Congratulations! We would like to invite you for an interview for the <strong>{{jobTitle}}</strong> position.</p>" +
                "        <div style=\"background-color: #fdf2e9; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #e74c3c;\">" +
                "            <h3 style=\"color: #e74c3c; margin-top: 0;\">Interview Details</h3>" +
                "            <p><strong>Date:</strong> {{interviewDate}}</p>" +
                "            <p><strong>Time:</strong> {{interviewTime}}</p>" +
                "            <p><strong>Duration:</strong> {{duration}}</p>" +
                "            <p><strong>Type:</strong> {{interviewType}}</p>" +
                "            <p><strong>Location:</strong> {{location}}</p>" +
                "            <p><strong>Interviewer:</strong> {{interviewerName}}</p>" +
                "        </div>" +
                "        <div style=\"text-align: center; margin: 30px 0;\">" +
                "            <a href=\"{{confirmationLink}}\" style=\"background-color: #27ae60; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin-right: 10px;\">Confirm Interview</a>" +
                "            <a href=\"{{rescheduleLink}}\" style=\"background-color: #f39c12; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;\">Reschedule</a>" +
                "        </div>" +
                "        <p><strong>Instructions:</strong></p>" +
                "        <p>{{instructions}}</p>" +
                "        <hr style=\"margin: 30px 0; border: none; border-top: 1px solid #eee;\">" +
                "        <p style=\"font-size: 12px; color: #666;\">{{tenantName}} - {{currentYear}}</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private static String getBasicTemplate() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>{{subject}}</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <div style=\"text-align: center; margin-bottom: 30px;\">" +
                "            <img src=\"{{logoUrl}}\" alt=\"{{tenantName}}\" style=\"max-height: 60px;\">" +
                "        </div>" +
                "        <h2>{{subject}}</h2>" +
                "        <p>{{content}}</p>" +
                "        <hr style=\"margin: 30px 0; border: none; border-top: 1px solid #eee;\">" +
                "        <p style=\"font-size: 12px; color: #666;\">{{tenantName}} - {{currentYear}}</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}

//package com.midas.consulting.util;
//
//import org.springframework.stereotype.Component;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.stream.Collectors;
//
//@Component
//public class TemplateLoader {
//
//        public static String loadTemplate(String fileName) {
//            try (InputStream is = TemplateLoader.class.getClassLoader().getResourceAsStream("email-templates/" + fileName);
//                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
//
//                return reader.lines().collect(Collectors.joining("\n"));
//            } catch (IOException | NullPointerException e) {
//                throw new RuntimeException("Failed to load template: " + fileName, e);
//            }
//        }
//    }