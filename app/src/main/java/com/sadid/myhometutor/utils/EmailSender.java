package com.sadid.myhometutor.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    // TODO: Replace with your actual Gmail address
    private static final String SENDER_EMAIL = "myhometutor.manager@gmail.com";
    // TODO: Replace with your generated App Password (NOT your login password)
    // Go to Google Account -> Security -> 2-Step Verification -> App Passwords
    private static final String SENDER_PASSWORD = "tkzp tojf dmoj sbhc";

    public static void sendOTP(Context context, String recipientEmail, String otp) {
        // Use ExecutorService to run network operations in the background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Configure SMTP properties for Gmail
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                // Create a session with authentication
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

                // Create the email message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("MyHomeTutor - Verify Your Account");
                String emailBody = "<!DOCTYPE html>" +
                        "<html>" +
                        "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                        "<h2 style='color: #4CAF50; text-align: center;'>MyHomeTutor</h2>" +
                        "<h3 style='color: #333;'>Verify Your Account</h3>" +
                        "<p style='color: #666; line-height: 1.6;'>Hello,</p>" +
                        "<p style='color: #666; line-height: 1.6;'>Thank you for registering with MyHomeTutor. Please use the following One-Time Password (OTP) to complete your registration:</p>" +
                        "<div style='background-color: #f0f0f0; padding: 20px; text-align: center; margin: 20px 0; border-radius: 5px;'>" +
                        "<h1 style='color: #4CAF50; margin: 0; letter-spacing: 5px;'>" + otp + "</h1>" +
                        "</div>" +
                        "<p style='color: #666; line-height: 1.6;'><strong>Important:</strong> This OTP is valid for 10 minutes and should not be shared with anyone.</p>" +
                        "<p style='color: #666; line-height: 1.6;'>If you didn't request this verification code, please ignore this email.</p>" +
                        "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                        "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 MyHomeTutor. All rights reserved.</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>";
                message.setContent(emailBody, "text/html; charset=utf-8");

                // Send the email
                Transport.send(message);

                // Show success message on UI thread
                handler.post(() -> Toast.makeText(context, "OTP sent to " + recipientEmail, Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                // Show error message on UI thread
                handler.post(() -> Toast.makeText(context, "Failed to send email: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    /**
     * Send a generic email (for admin notifications, etc.)
     * This method doesn't show Toast messages
     */
    public static void sendEmail(String recipientEmail, String subject, String body) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                // Configure SMTP properties for Gmail
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                // Create a session with authentication
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

                // Create the email message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setContent(body, "text/html; charset=utf-8");

                // Send the email
                Transport.send(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static String getPremiumEmailTemplate(String title, String messageContent) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f6f9fc;'>" +
                "    <table border='0' cellpadding='0' cellspacing='0' width='100%' style='background-color: #f6f9fc; padding: 20px;'>" +
                "        <tr>" +
                "            <td align='center'>" +
                "                <table border='0' cellpadding='0' cellspacing='0' width='600' style='background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);'>" +
                "                    <!-- Header -->" +
                "                    <tr>" +
                "                        <td bgcolor='#306cce' align='center' style='padding: 30px 20px;'>" +
                "                            <h1 style='color: #ffffff; margin: 0; font-size: 24px; font-weight: bold;'>MyHomeTutor</h1>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- Content -->" +
                "                    <tr>" +
                "                        <td style='padding: 40px 30px;'>" +
                "                            <h2 style='color: #333333; margin-top: 0; margin-bottom: 20px; font-size: 20px;'>" + title + "</h2>" +
                "                            <p style='color: #555555; font-size: 16px; line-height: 1.6; margin-bottom: 30px;'>" +
                "                                " + messageContent +
                "                            </p>" +
                "                            <div style='background-color: #f8f9fa; border-left: 4px solid #306cce; padding: 15px; margin-top: 20px;'>" +
                "                                <p style='color: #666666; margin: 0; font-size: 14px; font-style: italic;'>Current Time: " + new java.util.Date().toString() + "</p>" +
                "                            </div>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- Footer -->" +
                "                    <tr>" +
                "                        <td bgcolor='#f8f9fa' style='padding: 20px; text-align: center; border-top: 1px solid #eeeeee;'>" +
                "                            <p style='color: #999999; font-size: 12px; margin: 0;'>© 2026 MyHomeTutor. All rights reserved.</p>" +
                "                            <p style='color: #999999; font-size: 12px; margin: 5px 0 0 0;'>Dhaka, Bangladesh</p>" +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </td>" +
                "        </tr>" +
                "    </table>" +
                "</body>" +
                "</html>";
    }
}

