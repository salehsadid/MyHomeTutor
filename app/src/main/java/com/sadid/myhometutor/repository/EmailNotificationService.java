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
                message.setContent(body, "text/html; charset=utf-8");
                
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
            "<a href='intent://login#Intent;scheme=myhometutor;package=com.sadid.myhometutor;end' class='button'>Login Now</a>" +
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
        String emailSubject = "❌ Account Status Update - MyHomeTutor";
        String emailBody = buildPremiumEmailTemplate(
            "#f44336",
            "Account Application Update",
            "📋 Application Status",
            "Dear " + userType + ",",
            "We regret to inform you that your MyHomeTutor account application could not be approved at this time.",
            new String[][]{
                {"Status", "❌ Not Approved"},
                {"Reason", "Did not meet requirements"}
            },
            "If you believe this is an error or have questions, please contact our support team for clarification.",
            "Contact Support",
            "We appreciate your interest in MyHomeTutor."
        );
        
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
     * Send connection approved notification to tutor (when both student AND admin approve)
     */
    public void sendConnectionApprovedToTutor(String tutorEmail, String tutorName, String studentName, String subject) {
        String emailSubject = "🎉 Connection Approved - MyHomeTutor";
        String emailBody = buildPremiumEmailTemplate(
            "#4CAF50",
            "Connection Approved!",
            "🎉 Congratulations!",
            "Dear " + tutorName + ",",
            "Your application has been approved by both the student and admin! You now have an active connection.",
            new String[][]{
                {"Student Name", studentName},
                {"Subject", subject},
                {"Status", "✅ Approved"}
            },
            "You can now view the student's contact information in your Connections section and coordinate your tutoring sessions.",
            "View Connection Details",
            "This is great news! Start planning your lessons."
        );
        sendEmailAsync(tutorEmail, emailSubject, emailBody, null);
    }

    /**
     * Send account banned notification
     */
    public void sendAccountBannedNotification(String userEmail, String userName, String reason) {
        String emailSubject = "⚠️ Account Suspended - MyHomeTutor";
        String emailBody = buildPremiumEmailTemplate(
            "#f44336",
            "Account Suspended",
            "⚠️ Important Notice",
            "Dear " + userName + ",",
            "Your MyHomeTutor account has been temporarily suspended.",
            new String[][]{
                {"Reason", reason != null ? reason : "Policy violation"},
                {"Status", "🚫 Suspended"},
                {"Action Required", "Contact support"}
            },
            "If you believe this is a mistake, please contact our support team immediately to resolve this issue.",
            "Contact Support",
            "We take account security seriously."
        );
        sendEmailAsync(userEmail, emailSubject, emailBody, null);
    }

    /**
     * Send account unbanned notification
     */
    public void sendAccountUnbannedNotification(String userEmail, String userName) {
        String emailSubject = "✅ Account Restored - MyHomeTutor";
        String emailBody = buildPremiumEmailTemplate(
            "#4CAF50",
            "Account Restored",
            "✅ Good News!",
            "Dear " + userName + ",",
            "Your MyHomeTutor account has been restored and you can now access all features again.",
            new String[][]{
                {"Status", "✅ Active"},
                {"Access", "Full access restored"}
            },
            "Thank you for your patience. You can now log in and continue using MyHomeTutor.",
            "Login to Account",
            "Welcome back to MyHomeTutor!"
        );
        sendEmailAsync(userEmail, emailSubject, emailBody, null);
    }

    /**
     * Send account deleted notification
     */
    public void sendAccountDeletedNotification(String userEmail, String userName) {
        String emailSubject = "Account Deletion Confirmation - MyHomeTutor";
        String emailBody = buildPremiumEmailTemplate(
            "#607D8B",
            "Account Deleted",
            "👋 Goodbye",
            "Dear " + userName + ",",
            "Your MyHomeTutor account has been permanently deleted as requested.",
            new String[][]{
                {"Status", "Deleted"},
                {"Data", "All personal data removed"},
                {"Effective", "Immediate"}
            },
            "All your data has been removed from our system. If this was done by mistake, you'll need to create a new account. We're sorry to see you go!",
            null,
            "Thank you for being part of MyHomeTutor."
        );
        sendEmailAsync(userEmail, emailSubject, emailBody, null);
    }

    /**
     * Send password changed notification
     */
    public void sendPasswordChangedNotification(String userEmail, String userName) {
        String emailSubject = "🔐 Password Changed - MyHomeTutor";
        String emailBody = buildPremiumEmailTemplate(
            "#FF9800",
            "Password Changed",
            "🔐 Security Alert",
            "Dear " + userName + ",",
            "Your MyHomeTutor account password was recently changed.",
            new String[][]{
                {"Changed On", new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(new java.util.Date())},
                {"Action", "Password Updated"}
            },
            "<strong style='color: #f44336;'>⚠️ If this was you, you can safely ignore this email.</strong><br><br>" +
            "<strong style='color: #f44336;'>⚠️ If you did NOT change your password, your account may be compromised!</strong> " +
            "Please reset your password immediately and contact support.",
            "Reset Password",
            "Keep your account secure. Never share your password."
        );
        sendEmailAsync(userEmail, emailSubject, emailBody, null);
    }

    /**
     * Send tutor application notification to student (when tutor applies for student's post)
     */
    public void sendTutorAppliedNotification(String studentEmail, String studentName, String tutorName, String subject) {
        String emailSubject = "📚 New Tutor Application - MyHomeTutor";
        String emailBody = buildPremiumEmailTemplate(
            "#2196F3",
            "New Application Received",
            "📚 New Application",
            "Dear " + studentName + ",",
            "You have received a new tutor application for your tuition post!",
            new String[][]{
                {"Tutor Name", tutorName},
                {"Subject", subject},
                {"Status", "⏳ Pending Your Review"}
            },
            "Please review this application and decide whether to accept or reject it. Log in to view the tutor's profile and credentials.",
            "Review Application",
            "Respond to applications promptly for best results!"
        );
        sendEmailAsync(studentEmail, emailSubject, emailBody, null);
    }

    /**
     * Send admin digest notification (every 12 hours)
     */
    public void sendAdminDigestNotification(String adminEmail, int newRegistrations, int newPosts, int newConnections) {
        String emailSubject = "📊 Admin Digest - MyHomeTutor";
        String emailBody = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }" +
            ".container { max-width: 650px; margin: 20px auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }" +
            ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }" +
            ".header h1 { margin: 0; font-size: 28px; font-weight: 600; }" +
            ".header p { margin: 10px 0 0 0; opacity: 0.9; }" +
            ".content { padding: 40px 30px; }" +
            ".stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 30px 0; }" +
            ".stat-card { background: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; border-radius: 8px; text-align: center; }" +
            ".stat-number { font-size: 36px; font-weight: bold; color: #667eea; margin: 0; }" +
            ".stat-label { color: #6c757d; font-size: 14px; margin-top: 5px; }" +
            ".info-section { background: #e7f3ff; border-left: 4px solid #2196F3; padding: 20px; margin: 20px 0; border-radius: 5px; }" +
            ".button { display: inline-block; padding: 14px 35px; margin: 25px 0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; border-radius: 25px; font-weight: 600; transition: transform 0.2s; }" +
            ".button:hover { transform: translateY(-2px); }" +
            ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 13px; border-top: 1px solid #dee2e6; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>📊 Admin Digest</h1>" +
            "<p>MyHomeTutor Platform Statistics</p>" +
            "<p style='font-size: 12px; margin-top: 10px;'>" + new java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy HH:mm").format(new java.util.Date()) + "</p>" +
            "</div>" +
            "<div class='content'>" +
            "<p style='font-size: 16px; color: #495057;'><strong>Hello Admin,</strong></p>" +
            "<p style='color: #6c757d;'>Here's your bi-hourly summary of platform activity:</p>" +
            "<div class='stats-grid'>" +
            "<div class='stat-card'>" +
            "<p class='stat-number'>" + newRegistrations + "</p>" +
            "<p class='stat-label'>👥 New Registrations</p>" +
            "</div>" +
            "<div class='stat-card'>" +
            "<p class='stat-number'>" + newPosts + "</p>" +
            "<p class='stat-label'>📝 Post Applications</p>" +
            "</div>" +
            "<div class='stat-card'>" +
            "<p class='stat-number'>" + newConnections + "</p>" +
            "<p class='stat-label'>🤝 Connection Apps</p>" +
            "</div>" +
            "</div>" +
            "<div class='info-section'>" +
            "<strong>📌 Quick Summary:</strong><br>" +
            "• <strong>" + newRegistrations + "</strong> new user registration(s) awaiting approval<br>" +
            "• <strong>" + newPosts + "</strong> new tuition post(s) pending review<br>" +
            "• <strong>" + newConnections + "</strong> connection application(s) requiring admin approval" +
            "</div>" +
            "<p style='color: #6c757d;'>Please review and take necessary actions to keep the platform running smoothly.</p>" +
            "<center>" +
            "<a href='#' class='button'>Go to Admin Dashboard</a>" +
            "</center>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>This is an automated digest sent every 12 hours.<br>" +
            "MyHomeTutor Administration Panel</p>" +
            "<p style='margin-top: 10px;'>© " + java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + " MyHomeTutor. All rights reserved.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
        sendEmailAsync(adminEmail, emailSubject, emailBody, null);
    }

    /**
     * Build premium HTML email template
     */
    private String buildPremiumEmailTemplate(String themeColor, String headerTitle, String greeting, 
                                            String salutation, String mainMessage, String[][] infoItems,
                                            String additionalInfo, String buttonText, String footerNote) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }");
        html.append(".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, ").append(themeColor).append(" 0%, ").append(adjustColor(themeColor)).append(" 100%); color: white; padding: 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 26px; font-weight: 600; }");
        html.append(".content { padding: 35px 30px; }");
        html.append(".greeting { font-size: 24px; color: ").append(themeColor).append("; text-align: center; margin: 20px 0; font-weight: 600; }");
        html.append(".info-box { background-color: ").append(lightenColor(themeColor)).append("; border-left: 4px solid ").append(themeColor).append("; padding: 20px; margin: 25px 0; border-radius: 5px; }");
        html.append(".info-box table { width: 100%; border-collapse: collapse; }");
        html.append(".info-box td { padding: 8px 0; }");
        html.append(".info-box td:first-child { font-weight: 600; color: #495057; width: 40%; }");
        html.append(".button { display: inline-block; padding: 14px 35px; margin: 25px 0; background: linear-gradient(135deg, ").append(themeColor).append(" 0%, ").append(adjustColor(themeColor)).append(" 100%); color: white; text-decoration: none; border-radius: 25px; font-weight: 600; transition: transform 0.2s; }");
        html.append(".button:hover { transform: translateY(-2px); box-shadow: 0 4px 8px rgba(0,0,0,0.15); }");
        html.append(".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 13px; border-top: 1px solid #dee2e6; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>").append(headerTitle).append("</h1>");
        html.append("</div>");
        html.append("<div class='content'>");
        html.append("<p class='greeting'>").append(greeting).append("</p>");
        html.append("<p style='color: #495057; font-size: 15px;'>").append(salutation).append("</p>");
        html.append("<p style='color: #6c757d; line-height: 1.8;'>").append(mainMessage).append("</p>");
        
        if (infoItems != null && infoItems.length > 0) {
            html.append("<div class='info-box'>");
            html.append("<table>");
            for (String[] item : infoItems) {
                html.append("<tr><td>").append(item[0]).append(":</td><td>").append(item[1]).append("</td></tr>");
            }
            html.append("</table>");
            html.append("</div>");
        }
        
        if (additionalInfo != null) {
            html.append("<p style='color: #6c757d; line-height: 1.8; margin-top: 20px;'>").append(additionalInfo).append("</p>");
        }
        
        if (buttonText != null) {
            html.append("<center>");
            html.append("<a href='#' class='button'>").append(buttonText).append("</a>");
            html.append("</center>");
        }
        
        html.append("<p style='color: #495057; margin-top: 30px;'>Best regards,<br><strong>MyHomeTutor Team</strong></p>");
        html.append("</div>");
        html.append("<div class='footer'>");
        html.append("<p>").append(footerNote != null ? footerNote : "This is an automated message from MyHomeTutor.").append("</p>");
        html.append("<p style='margin-top: 10px;'>© ").append(String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))).append(" MyHomeTutor. All rights reserved.</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * Adjust color for gradient
     */
    private String adjustColor(String hexColor) {
        // Simple darkening by reducing each RGB component
        if (hexColor.startsWith("#") && hexColor.length() == 7) {
            try {
                int r = Integer.parseInt(hexColor.substring(1, 3), 16);
                int g = Integer.parseInt(hexColor.substring(3, 5), 16);
                int b = Integer.parseInt(hexColor.substring(5, 7), 16);
                
                r = Math.max(0, r - 30);
                g = Math.max(0, g - 30);
                b = Math.max(0, b - 30);
                
                return String.format("#%02x%02x%02x", r, g, b);
            } catch (Exception e) {
                return hexColor;
            }
        }
        return hexColor;
    }

    /**
     * Lighten color for info boxes
     */
    private String lightenColor(String hexColor) {
        if (hexColor.startsWith("#") && hexColor.length() == 7) {
            try {
                int r = Integer.parseInt(hexColor.substring(1, 3), 16);
                int g = Integer.parseInt(hexColor.substring(3, 5), 16);
                int b = Integer.parseInt(hexColor.substring(5, 7), 16);
                
                r = Math.min(255, r + 220);
                g = Math.min(255, g + 220);
                b = Math.min(255, b + 220);
                
                return String.format("#%02x%02x%02x", r, g, b);
            } catch (Exception e) {
                return "#f0f0f0";
            }
        }
        return "#f0f0f0";
    }

    /**
     * Shutdown executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
