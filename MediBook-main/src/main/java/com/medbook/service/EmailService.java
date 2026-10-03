package com.medbook.service;

import com.medbook.entity.Appointment;
import com.medbook.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(appointment.getPatient().getEmail());
            helper.setSubject("Appointment Confirmation - MedBook");
            helper.setFrom("MecerGroup4@gmail.com");
            
            String htmlContent = generateAppointmentConfirmationTemplate(appointment);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Failed to send appointment confirmation email: " + e.getMessage());
        }
    }

    public void sendAppointmentReminder(Appointment appointment) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(appointment.getPatient().getEmail());
        message.setSubject("Appointment Reminder - MedBook");
        
        String emailBody = String.format(
            "Dear %s,\n\n" +
            "This is a reminder for your upcoming appointment:\n\n" +
            "Doctor: Dr. %s %s\n" +
            "Date & Time: %s\n" +
            "Location: MedBook Hospital\n\n" +
            "Please arrive 15 minutes before your scheduled time.\n\n" +
            "Best regards,\n" +
            "MedBook Team",
            appointment.getPatient().getFullName(),
            appointment.getDoctor().getUser().getFirstName(),
            appointment.getDoctor().getUser().getLastName(),
            appointment.getAppointmentDateTime().toString()
        );
        
        message.setText(emailBody);
        message.setFrom("MecerGroup4@gmail.com");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send reminder email: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(User user) {
        try {
            System.out.println("=== EMAIL SERVICE DEBUG ===");
            System.out.println("Attempting to send welcome email to: " + user.getEmail());
            System.out.println("Mail sender configured: " + (mailSender != null ? "YES" : "NO"));
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to MedBook!");
            helper.setFrom("MecerGroup4@gmail.com");
            
            String htmlContent = generateWelcomeEmailTemplate(user.getFullName());
            helper.setText(htmlContent, true);
            
            System.out.println("Email content prepared, attempting to send...");
            mailSender.send(mimeMessage);
            System.out.println("✅ Welcome email sent successfully to: " + user.getEmail());
            System.out.println("=== EMAIL SERVICE DEBUG END ===");
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send welcome email to " + user.getEmail() + ": " + e.getMessage());
            System.err.println("Error details: " + e.getClass().getSimpleName());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending welcome email to " + user.getEmail() + ": " + e.getMessage());
            System.err.println("Error details: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    public void sendPasswordResetEmail(User user, String resetToken, String resetUrl) {
        try {
            System.out.println("=== PASSWORD RESET EMAIL DEBUG ===");
            System.out.println("Attempting to send password reset email to: " + user.getEmail());
            System.out.println("Mail sender configured: " + (mailSender != null ? "YES" : "NO"));
            System.out.println("Reset URL: " + resetUrl);
            
            if (mailSender == null) {
                System.err.println("❌ MailSender is null! Email configuration issue.");
                return;
            }
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset Request - MedBook");
            helper.setFrom("MecerGroup4@gmail.com");
            
            String htmlContent = generatePasswordResetEmailTemplate(user.getFullName(), resetUrl);
            helper.setText(htmlContent, true);
            
            System.out.println("Password reset email content prepared, attempting to send...");
            System.out.println("Email details - To: " + user.getEmail() + ", From: MecerGroup4@gmail.com");
            
            mailSender.send(mimeMessage);
            System.out.println("✅ Password reset email sent successfully to: " + user.getEmail());
            System.out.println("=== PASSWORD RESET EMAIL DEBUG END ===");
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send password reset email to " + user.getEmail() + ": " + e.getMessage());
            System.err.println("Error details: " + e.getClass().getSimpleName());
            System.err.println("Full error: " + e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending password reset email to " + user.getEmail() + ": " + e.getMessage());
            System.err.println("Error details: " + e.getClass().getSimpleName());
            System.err.println("Full error: " + e.toString());
            e.printStackTrace();
        }
    }

    public void sendPasswordResetConfirmation(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Successfully Reset - MedBook");
        
        String emailBody = String.format(
            "Dear %s,\n\n" +
            "Your password has been successfully reset for your MedBook account.\n\n" +
            "If you did not make this change, please contact our support team immediately.\n\n" +
            "For your security, we recommend:\n" +
            "- Using a strong, unique password\n" +
            "- Not sharing your login credentials\n" +
            "- Logging out from shared devices\n\n" +
            "Best regards,\n" +
            "The MedBook Team",
            user.getFullName()
        );
        
        message.setText(emailBody);
        message.setFrom("MecerGroup4@gmail.com");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send password reset confirmation email: " + e.getMessage());
        }
    }

    private String generatePasswordResetEmailTemplate(String userName, String resetUrl) {
        return "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Password Reset - MedBook</title>" +
                "<style>" +
                    "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f4f4f4; }" +
                    ".email-container { background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                    ".header { background: linear-gradient(135deg, #1e3a8a, #3b82f6); color: white; padding: 30px 20px; text-align: center; }" +
                    ".logo-container { display: flex; align-items: center; justify-content: center; margin-bottom: 15px; }" +
                    ".hospital-icon { font-size: 40px; margin-right: 15px; background: rgba(255, 255, 255, 0.2); border-radius: 50%; width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; border: 2px solid rgba(255, 255, 255, 0.3); }" +
                    ".logo-text { font-size: 28px; font-weight: bold; margin: 0; }" +
                    ".content { padding: 40px 30px; }" +
                    ".greeting { font-size: 18px; color: #1e3a8a; margin-bottom: 20px; }" +
                    ".message { font-size: 16px; margin-bottom: 30px; color: #555; }" +
                    ".reset-button { display: inline-block; background: linear-gradient(135deg, #1e3a8a, #3b82f6); color: white; padding: 18px 35px; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; transition: transform 0.2s ease; font-size: 16px; box-shadow: 0 4px 12px rgba(30, 58, 138, 0.3); border: 2px solid #1e40af; }" +
                    ".reset-button:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(30, 58, 138, 0.4); background: linear-gradient(135deg, #1e40af, #1e3a8a); }" +
                    ".security-notice { background-color: #f8f9fa; border-left: 4px solid #3b82f6; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
                    ".footer { background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef; }" +
                    ".footer-logo { font-size: 20px; font-weight: bold; color: #1e3a8a; margin-bottom: 10px; display: flex; align-items: center; justify-content: center; }" +
                    ".footer-hospital-icon { font-size: 24px; margin-right: 10px; background: #1e3a8a; color: white; border-radius: 50%; width: 40px; height: 40px; display: inline-flex; align-items: center; justify-content: center; border: 2px solid #3b82f6; }" +
                    ".footer-text { color: #6c757d; font-size: 14px; margin-bottom: 10px; }" +
                    ".social-links { margin-top: 15px; }" +
                    ".social-links a { color: #3b82f6; text-decoration: none; margin: 0 10px; }" +
                "</style>" +
            "</head>" +
            "<body>" +
                "<div class=\"email-container\">" +
                    "<div class=\"header\">" +
                        "<div class=\"logo-container\">" +
                            "<div class=\"hospital-icon\">🏥</div>" +
                            "<div class=\"logo-text\">MedBook</div>" +
                        "</div>" +
                        "<p style=\"margin: 0; opacity: 0.9;\">Your Health, Our Priority</p>" +
                    "</div>" +
                    "<div class=\"content\">" +
                        "<div class=\"greeting\">Hello " + userName + ",</div>" +
                        "<div class=\"message\">We received a request to reset your password for your MedBook account. If you made this request, please click the button below to reset your password.</div>" +
                        "<div style=\"text-align: center;\"><a href=\"" + resetUrl + "\" class=\"reset-button\">Reset My Password</a></div>" +
                        "<div class=\"security-notice\">" +
                            "<strong>Security Information:</strong><br>" +
                            "• This link will expire in 24 hours<br>" +
                            "• If you didn't request this reset, please ignore this email<br>" +
                            "• Never share this link with anyone" +
                        "</div>" +
                        "<div class=\"message\">If the button doesn't work, you can copy and paste this link into your browser:<br><a href=\"" + resetUrl + "\" style=\"color: #3b82f6; word-break: break-all;\">" + resetUrl + "</a></div>" +
                    "</div>" +
                    "<div class=\"footer\">" +
                        "<div class=\"footer-logo\">🏥 MedBook</div>" +
                        "<div class=\"footer-text\">Your trusted partner in healthcare<br>Providing comprehensive medical services with expert care</div>" +
                        "<div class=\"footer-text\">📧 info@medbook.com | 📞 +1 (555) 123-4567<br>📍 123 Medical Center Dr, City, State 12345</div>" +
                        "<div class=\"social-links\"><a href=\"#\">Website</a> | <a href=\"#\">Support</a> | <a href=\"#\">Privacy Policy</a></div>" +
                        "<div class=\"footer-text\" style=\"margin-top: 20px; font-size: 12px;\">© 2025 MedBook. All rights reserved.</div>" +
                    "</div>" +
                "</div>" +
            "</body>" +
            "</html>";
    }

    private String generateWelcomeEmailTemplate(String userName) {
        return "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Welcome to MedBook</title>" +
                "<style>" +
                    "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f4f4f4; }" +
                    ".email-container { background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                    ".header { background: linear-gradient(135deg, #3b82f6, #60a5fa); color: white; padding: 30px 20px; text-align: center; }" +
                    ".logo-container { display: flex; align-items: center; justify-content: center; margin-bottom: 15px; }" +
                    ".hospital-icon { font-size: 40px; background: rgba(255, 255, 255, 0.2); border-radius: 50%; width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; border: 2px solid rgba(255, 255, 255, 0.3); margin: 0 auto; }" +
                    ".logo-text { font-size: 28px; font-weight: bold; margin: 0; }" +
                    ".content { padding: 40px 30px; }" +
                    ".greeting { font-size: 18px; color: #3b82f6; margin-bottom: 20px; }" +
                    ".message { font-size: 16px; margin-bottom: 30px; color: #555; }" +
                    ".features { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                    ".feature-item { display: flex; align-items: center; margin-bottom: 15px; font-size: 14px; }" +
                    ".feature-icon { color: #3b82f6; margin-right: 15px; font-size: 18px; }" +
                    ".cta-button { display: inline-block; background: linear-gradient(135deg, #3b82f6, #60a5fa); color: white; padding: 18px 35px; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; transition: transform 0.2s ease; font-size: 16px; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3); border: 2px solid #3b82f6; }" +
                    ".cta-button:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4); background: linear-gradient(135deg, #2563eb, #3b82f6); }" +
                    ".footer { background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef; }" +
                    ".footer-logo { font-size: 20px; font-weight: bold; color: #3b82f6; margin-bottom: 10px; display: flex; align-items: center; justify-content: center; }" +
                    ".footer-hospital-icon { font-size: 24px; margin-right: 10px; background: #3b82f6; color: white; border-radius: 50%; width: 40px; height: 40px; display: inline-flex; align-items: center; justify-content: center; border: 2px solid #60a5fa; }" +
                    ".footer-text { color: #666; font-size: 14px; margin-bottom: 10px; }" +
                    ".social-links { margin-top: 15px; }" +
                    ".social-links a { color: #3b82f6; text-decoration: none; margin: 0 10px; }" +
                "</style>" +
            "</head>" +
            "<body>" +
                "<div class=\"email-container\">" +
                    "<div class=\"header\">" +
                        "<div class=\"hospital-icon\">🏥</div>" +
                        "<div class=\"logo-text\">MedBook</div>" +
                        "<p style=\"margin: 0; opacity: 0.9;\">Your Health, Our Priority</p>" +
                    "</div>" +
                    "<div class=\"content\">" +
                        "<div class=\"greeting\">Welcome to MedBook, " + userName + "!</div>" +
                        "<div class=\"message\">We are thrilled to have you join our community. MedBook is your trusted platform for managing healthcare appointments and connecting with qualified medical professionals.</div>" +
                        "<div class=\"features\">" +
                            "<h3 style=\"color: #3b82f6; margin-top: 0;\">What you can do with MedBook:</h3>" +
                            "<div class=\"feature-item\"><span class=\"feature-icon\">📅</span><span>Book appointments with qualified doctors</span></div>" +
                            "<div class=\"feature-item\"><span class=\"feature-icon\">📋</span><span>View your appointment history</span></div>" +
                            "<div class=\"feature-item\"><span class=\"feature-icon\">🤖</span><span>Get AI-powered doctor recommendations</span></div>" +
                            "<div class=\"feature-item\"><span class=\"feature-icon\">💊</span><span>Manage your medications and prescriptions</span></div>" +
                            "<div class=\"feature-item\"><span class=\"feature-icon\">📧</span><span>Receive email confirmations and reminders</span></div>" +
                        "</div>" +
                        "<div style=\"text-align: center;\"><a href=\"http://localhost:8080/login\" class=\"cta-button\">Get Started Now</a></div>" +
                        "<div class=\"message\">To get started, simply log in to your account using your registered email and password. If you have any questions, feel free to contact our support team.</div>" +
                    "</div>" +
                    "<div class=\"footer\">" +
                        "<div class=\"footer-logo\"><span class=\"footer-hospital-icon\">🏥</span>MedBook</div>" +
                        "<div class=\"footer-text\">Your trusted partner in healthcare<br>Providing comprehensive medical services with expert care</div>" +
                        "<div class=\"footer-text\">📧 info@medbook.com | 📞 +1 (555) 123-4567<br>📍 123 Medical Center Dr, City, State 12345</div>" +
                        "<div class=\"social-links\"><a href=\"#\">Website</a> | <a href=\"#\">Support</a> | <a href=\"#\">Privacy Policy</a></div>" +
                        "<div class=\"footer-text\" style=\"margin-top: 20px; font-size: 12px;\">© 2025 MedBook. All rights reserved.</div>" +
                    "</div>" +
                "</div>" +
            "</body>" +
            "</html>";
    }

    private String generateAppointmentConfirmationTemplate(Appointment appointment) {
        return "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Appointment Confirmation - MedBook</title>" +
                "<style>" +
                    "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f4f4f4; }" +
                    ".email-container { background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                    ".header { background: linear-gradient(135deg, #3b82f6, #60a5fa); color: white; padding: 30px 20px; text-align: center; }" +
                    ".hospital-icon { font-size: 40px; background: rgba(255, 255, 255, 0.2); border-radius: 50%; width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; border: 2px solid rgba(255, 255, 255, 0.3); margin: 0 auto; }" +
                    ".logo-text { font-size: 28px; font-weight: bold; margin: 0; }" +
                    ".content { padding: 40px 30px; }" +
                    ".greeting { font-size: 18px; color: #3b82f6; margin-bottom: 20px; }" +
                    ".message { font-size: 16px; margin-bottom: 30px; color: #555; }" +
                    ".appointment-details { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                    ".detail-row { display: flex; justify-content: space-between; margin-bottom: 10px; padding: 8px 0; border-bottom: 1px solid #e9ecef; }" +
                    ".detail-row:last-child { border-bottom: none; }" +
                    ".detail-label { font-weight: bold; color: #3b82f6; min-width: 120px; }" +
                    ".detail-value { color: #555; text-align: right; flex: 1; }" +
                    ".cta-button { display: inline-block; background: linear-gradient(135deg, #3b82f6, #60a5fa); color: white; padding: 18px 35px; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; transition: transform 0.2s ease; font-size: 16px; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3); border: 2px solid #3b82f6; }" +
                    ".cta-button:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4); background: linear-gradient(135deg, #2563eb, #3b82f6); }" +
                    ".footer { background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef; }" +
                    ".footer-logo { font-size: 20px; font-weight: bold; color: #3b82f6; margin-bottom: 10px; display: flex; align-items: center; justify-content: center; }" +
                    ".footer-hospital-icon { font-size: 24px; margin-right: 10px; background: #3b82f6; color: white; border-radius: 50%; width: 40px; height: 40px; display: inline-flex; align-items: center; justify-content: center; border: 2px solid #60a5fa; }" +
                    ".footer-text { color: #666; font-size: 14px; margin-bottom: 10px; }" +
                    ".social-links { margin-top: 15px; }" +
                    ".social-links a { color: #3b82f6; text-decoration: none; margin: 0 10px; }" +
                "</style>" +
            "</head>" +
            "<body>" +
                "<div class=\"email-container\">" +
                    "<div class=\"header\">" +
                        "<div class=\"hospital-icon\">🏥</div>" +
                        "<div class=\"logo-text\">MedBook</div>" +
                        "<p style=\"margin: 0; opacity: 0.9;\">Your Health, Our Priority</p>" +
                    "</div>" +
                    "<div class=\"content\">" +
                        "<div class=\"greeting\">Hello " + appointment.getPatient().getFullName() + ",</div>" +
                        "<div class=\"message\">Your appointment has been successfully confirmed! We're looking forward to seeing you.</div>" +
                        "<div class=\"appointment-details\">" +
                            "<h3 style=\"color: #3b82f6; margin-top: 0; margin-bottom: 20px;\">📅 Appointment Details</h3>" +
                            "<div class=\"detail-row\"><span class=\"detail-label\">Doctor:</span><span class=\"detail-value\">Dr. " + appointment.getDoctor().getUser().getFirstName() + " " + appointment.getDoctor().getUser().getLastName() + "</span></div>" +
                            "<div class=\"detail-row\"><span class=\"detail-label\">Specialization:</span><span class=\"detail-value\">" + appointment.getDoctor().getSpecialization() + "</span></div>" +
                            "<div class=\"detail-row\"><span class=\"detail-label\">Date & Time:</span><span class=\"detail-value\">" + appointment.getAppointmentDateTime().toString() + "</span></div>" +
                            "<div class=\"detail-row\"><span class=\"detail-label\">Reason:</span><span class=\"detail-value\">" + appointment.getReasonForVisit() + "</span></div>" +
                        "</div>" +
                        "<div class=\"message\">Please arrive 15 minutes before your scheduled appointment time. If you need to reschedule or cancel, please contact us at least 24 hours in advance.</div>" +
                        "<div class=\"message\">If you have any questions or need to make changes to your appointment, please don't hesitate to contact our support team.</div>" +
                        "<div style=\"text-align: center; margin: 30px 0;\"><a href=\"#\" class=\"cta-button\">View My Appointments</a></div>" +
                    "</div>" +
                    "<div class=\"footer\">" +
                        "<div class=\"footer-logo\"><span class=\"footer-hospital-icon\">🏥</span>MedBook</div>" +
                        "<div class=\"footer-text\">Your trusted partner in healthcare<br>Providing comprehensive medical services with expert care</div>" +
                        "<div class=\"footer-text\">📧 info@medbook.com | 📞 +1 (555) 123-4567<br>📍 123 Medical Center Dr, City, State 12345</div>" +
                        "<div class=\"social-links\"><a href=\"#\">Website</a> | <a href=\"#\">Support</a> | <a href=\"#\">Privacy Policy</a></div>" +
                        "<div class=\"footer-text\" style=\"margin-top: 20px; font-size: 12px;\">© 2025 MedBook. All rights reserved.</div>" +
                    "</div>" +
                "</div>" +
            "</body>" +
            "</html>";
    }
}