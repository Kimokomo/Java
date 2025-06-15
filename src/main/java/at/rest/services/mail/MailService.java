package at.rest.services.mail;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Logger;

@ApplicationScoped
public class MailService {

    private static final Logger logger = Logger.getLogger(MailService.class.getName());
    private Properties mailProps = new Properties();
    private Session mailSession;

    public MailService() {
        String env = System.getProperty("app.env", "dev");
        logger.info("ENV beim Start: " + env);
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("mail-" + env + ".properties")) {
            if (input == null) {
                throw new RuntimeException("Mail-Konfigurationsdatei mail-" + env + ".properties nicht gefunden.");
            }
            mailProps.load(input);
        } catch (IOException e) {
            logger.warning("Fehler beim Laden der Mail-Konfiguration");
            throw new RuntimeException("Fehler beim Laden der Mail-Konfiguration", e);
        }

        // Session einmal initialisieren und speichern
        Properties sessionProps = new Properties();
        sessionProps.put("mail.smtp.auth", "true");
        sessionProps.put("mail.smtp.starttls.enable", "true");
        sessionProps.put("mail.smtp.host", mailProps.getProperty("mail.smtp.host"));
        sessionProps.put("mail.smtp.port", mailProps.getProperty("mail.smtp.port"));

        final String username = mailProps.getProperty("mail.username");
        final String password = mailProps.getProperty("mail.password");

        mailSession = Session.getInstance(sessionProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public boolean sendConfirmationEmail(String recipient, String token) {
        String baseUrl = mailProps.getProperty("app.confirmation.url");
        if (baseUrl == null) {
            throw new RuntimeException("Property app.confirmation.url nicht gefunden!");
        }

        String confirmationLink = baseUrl + token;
        String subject = "Willkommen bei SliceIt – Bitte bestätige deine Registrierung";
        String htmlContent = loadTemplate("confirmation-email.html", confirmationLink);
        return sendEmail(recipient, subject, htmlContent);
    }

    public boolean sendForgotPasswordEmail(String recipient, String token) {
        String baseUrl = mailProps.getProperty("app.forgotpassword.url");
        if (baseUrl == null) {
            throw new RuntimeException("Property app.forgotpassword.url nicht gefunden!");
        }

        String resetLink = baseUrl + token;
        String subject = "Passwort zurücksetzen – SliceIt";
        String htmlContent = loadTemplate("forgot-password-email.html", resetLink);

        return sendEmail(recipient, subject, htmlContent);
    }

    public boolean sendEmail(String recipient, String subject, String htmlContent) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(mailProps.getProperty("mail.username")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            Transport.send(message);
            logger.info("E-Mail gesendet an: " + recipient);
            return true;
        } catch (MessagingException e) {
            logger.severe("Fehler beim Senden der E-Mail: " + e.getMessage());
            return false;
        }
    }

    public boolean isValidEmailSyntax(String input) {
        try {
            InternetAddress emailAddr = new InternetAddress(input);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }

    private String loadTemplate(String templateName, String link) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/" + templateName)) {
            if (inputStream == null) {
                throw new RuntimeException("Template nicht gefunden: " + templateName);
            }
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return content.replace("{{link}}", link);
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden des Templates: " + templateName, e);
        }
    }
}



