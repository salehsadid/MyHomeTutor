package com.sadid.myhometutor.repository;

import android.os.Handler;
import android.os.Looper;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Email Notification Service
 * Sends email notifications for various events
 */
public class EmailNotificationService {
    
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "myhometutor.manager@gmail.com"; // Configure this
    private static final String APP_PASSWORD = "tkzp tojf dmoj sbhc"; // Configure this
    
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    public interface EmailCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
    
    public EmailNotificationService() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Send email asynchronously
     */
    private void sendEmailAsync(String toEmail, String subject, String body, EmailCallback callback) {
        executorService.execute(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                    }
                });
                
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setText(body);
                
                Transport.send(message);
                
                // Success callback on main thread
                if (callback != null) {
                    mainHandler.post(callback::onSuccess);
                }
                
            } catch (MessagingException e) {
                // Failure callback on main thread
                if (callback != null) {
                    mainHandler.post(() -> callback.onFailure(e));
                }
            }
        });
    }
    
    /**
     * Send tutor application notification to student
     */
    public void sendTutorApplicationNotification(String studentEmail, String tutorName, String subject) {
        String emailSubject = "New Tutor Application - MyHomeTutor";
        String emailBody = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
            ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
            ".content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }" +
            ".info-box { background-color: #e8f5e9; padding: 15px; margin: 20px 0; border-left: 4px solid #4CAF50; }" +
            ".button { display: inline-block; padding: 12px 30px; margin: 20px 0; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; }" +
            ".footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h2>New Tutor Application</h2>" +
            "</div>" +
            "<div class='content'>" +
            "<p>Dear Student,</p>" +
            "<p>You have received a new tutor application on MyHomeTutor!</p>" +
            "<div class='info-box'>" +
            "<strong>📚 Application Details:</strong><br>" +
            "<strong>Tutor Name:</strong> " + tutorName + "<br>" +
            "<strong>Subject:</strong> " + subject +
            "</div>" +
            "<p>Please review this application and take appropriate action.</p>" +
            "<center>" +
            "<a href='#' class='button'>Review Application</a>" +
            "</center>" +
            "<p>Best regards,<br><strong>MyHomeTutor Team</strong></p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>This is an automated message from MyHomeTutor. Please do not reply to this email.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
        
        sendEmailAsync(studentEmail, emailSubject, emailBody, new EmailCallback() {
            @Override
            public void onSuccess() {
                // Optional: Log success
            }
            
            @Override
            public void onFailure(Exception e) {
                // Optional: Log failure
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Send application accepted notification to tutor
     */
    public void sendApplicationAcceptedNotification(String tutorEmail, String studentName, String subject) {
        String emailSubject = "Application Accepted - MyHomeTutor";
        String emailBody = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
            ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
            ".content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }" +
            ".success-box { background-color: #e8f5e9; padding: 15px; margin: 20px 0; border-left: 4px solid #4CAF50; }" +
            ".congratulations { font-size: 24px; color: #4CAF50; text-align: center; margin: 20px 0; }" +
            ".button { display: inline-block; padding: 12px 30px; margin: 20px 0; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; }" +
            ".footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h2>Application Accepted!</h2>" +
            "</div>" +
            "<div class='content'>" +
            "<p class='congratulations'>🎉 Congratulations!</p>" +
            "<p>Dear Tutor,</p>" +
            "<p>We are pleased to inform you that your application has been <strong>accepted</strong>!</p>" +
            "<div class='success-box'>" +
            "<strong>📋 Application Details:</strong><br>" +
            "<strong>Student Name:</strong> " + studentName + "<br>" +
            "<strong>Subject:</strong> " + subject +
            "</div>" +
            "<p>You can now view the student's contact information in your connections section.</p>" +
            "<center>" +
            "<a href='#' class='button'>View Connection</a>" +
            "</center>" +
            "<p>Best regards,<br><strong>MyHomeTutor Team</strong></p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>This is an automated message from MyHomeTutor. Please do not reply to this email.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
        
        sendEmailAsync(tutorEmail, emailSubject, emailBody, null);
    }
    
    /**
     * Send application rejected notification to tutor
     */
    public void sendApplicationRejectedNotification(String tutorEmail, String subject) {
        String emailSubject = "Application Update - MyHomeTutor";
        String emailBody = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
            ".header { background-color: #f44336; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
            ".content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }" +
            ".info-box { background-color: #ffebee; padding: 15px; margin: 20px 0; border-left: 4px solid #f44336; }" +
            ".button { display: inline-block; padding: 12px 30px; margin: 20px 0; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; }" +
            ".footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h2>Application Update</h2>" +
            "</div>" +
            "<div class='content'>" +
            "<p>Dear Tutor,</p>" +
            "<p>Thank you for your interest in teaching on MyHomeTutor.</p>" +
            "<div class='info-box'>" +
            "<strong>📋 Application Status:</strong><br>" +
            "<strong>Subject:</strong> " + subject + "<br>" +
            "<strong>Status:</strong> Not Accepted" +
            "</div>" +
            "<p>We regret to inform you that your application was not successful this time. However, don't be discouraged! There are many more opportunities available on MyHomeTutor.</p>" +
            "<center>" +
            "<a href='#' class='button'>Browse More Opportunities</a>" +
            "</center>" +
            "<p>Best regards,<br><strong>MyHomeTutor Team</strong></p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>This is an automated message from MyHomeTutor. Please do not reply to this email.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
        
        sendEmailAsync(tutorEmail, emailSubject, emailBody, null);
    }
    
    /**
     * Send account approved notification
     */
    public void sendAccountApprovedNotification(String userEmail, String userType) {
        String emailSubject = "Account Approved - MyHomeTutor";
        String emailBody = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
            ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
            ".content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }" +
            ".success-box { background-color: #e8f5e9; padding: 15px; margin: 20px 0; border-left: 4px solid #4CAF50; }" +
            ".congratulations { font-size: 24px; color: #4CAF50; text-align: center; margin: 20px 0; }" +
            ".button { display: inline-block; padding: 12px 30px; margin: 20px 0; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; }" +
            ".footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h2>Account Approved!</h2>" +
            "</div>" +
            "<div class='content'>" +
            "<p class='congratulations'>🎉 Welcome to MyHomeTutor!</p>" +
            "<p>Dear " + userType + ",</p>" +
            "<p>Great news! Your MyHomeTutor account has been <strong>approved</strong> by our admin team.</p>" +
            "<div class='success-box'>" +
            "<strong>✅ What's Next?</strong><br>" +
            "• Log in to your account<br>" +
            "• Complete your profile<br>" +
            "• Start connecting with " + (userType.equalsIgnoreCase("Student") ? "tutors" : "students") +
            "</div>" +
            "<p>You can now access all features of the platform and begin your learning journey!</p>" +
            "<center>" +
            "<a href='#' class='button'>Login Now</a>" +
            "</center>" +
            "<p>Best regards,<br><strong>MyHomeTutor Team</strong></p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>This is an automated message from MyHomeTutor. Please do not reply to this email.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
        
        sendEmailAsync(userEmail, emailSubject, emailBody, null);
    }
    
    /**
     * Send account rejected notification
     */
    public void sendAccountRejectedNotification(String userEmail, String userType) {
        String emailSubject = "Account Status Update - MyHomeTutor";
        String emailBody = "Dear " + userType + ",\n\n" +
                "We regret to inform you that your MyHomeTutor account application could not be approved at this time.\n\n" +
                "If you believe this is an error, please contact our support team.\n\n" +
                "Best regards,\n" +
                "MyHomeTutor Team";
        
        sendEmailAsync(userEmail, emailSubject, emailBody, null);
    }
    
    /**
     * Send post approved notification to student
     */
    public void sendPostApprovedNotification(String studentEmail, String subject) {
        String emailSubject = "Tuition Post Approved - MyHomeTutor";
        String emailBody = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
            ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
            ".content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }" +
            ".success-box { background-color: #e8f5e9; padding: 15px; margin: 20px 0; border-left: 4px solid #4CAF50; }" +
            ".congratulations { font-size: 24px; color: #4CAF50; text-align: center; margin: 20px 0; }" +
            ".button { display: inline-block; padding: 12px 30px; margin: 20px 0; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; }" +
            ".footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h2>Tuition Post Approved!</h2>" +
            "</div>" +
            "<div class='content'>" +
            "<p class='congratulations'>🎉 Great News!</p>" +
            "<p>Dear Student,</p>" +
            "<p>Your tuition post has been <strong>approved</strong> by our admin team and is now live!</p>" +
            "<div class='success-box'>" +
            "<strong>📚 Post Details:</strong><br>" +
            "<strong>Subject:</strong> " + subject + "<br>" +
            "<strong>Status:</strong> Active and Visible to Tutors" +
            "</div>" +
            "<p>Your post is now visible to qualified tutors on the platform. You may start receiving applications soon!</p>" +
            "<center>" +
            "<a href='#' class='button'>View My Post</a>" +
            "</center>" +
            "<p>Best regards,<br><strong>MyHomeTutor Team</strong></p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>This is an automated message from MyHomeTutor. Please do not reply to this email.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
        
        sendEmailAsync(studentEmail, emailSubject, emailBody, null);
    }
    
    /**
     * Shutdown executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
