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
                message.setSubject("MyHomeTutor Verification OTP");
                message.setText("Hello,\n\nYour OTP for MyHomeTutor registration is: " + otp + "\n\nPlease do not share this code with anyone.");

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
}

