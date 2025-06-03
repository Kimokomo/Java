package at.rest.servcie;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

@ApplicationScoped
public class MailService {

    private static final Logger logger = Logger.getLogger(MailService.class.getName());

    private Properties mailProps = new Properties();

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
    }

    public boolean sendConfirmationEmail(String recipient, String token) {

        // E-Mail-Syntax prüfen
        try {
            InternetAddress emailAddr = new InternetAddress(recipient);
            emailAddr.validate();
        } catch (AddressException ex) {
            logger.warning("Ungültige E-Mail-Adresse: " + recipient);
            return false;
        }

        String baseUrl = mailProps.getProperty("app.confirmation.url");
        if (baseUrl == null) {
            throw new RuntimeException("Property app.confirmation.url nicht gefunden!");
        }

        String confirmationLink = baseUrl + token;

        Properties sessionProps = new Properties();
        sessionProps.put("mail.smtp.auth", "true");
        sessionProps.put("mail.smtp.starttls.enable", "true");
        sessionProps.put("mail.smtp.host", mailProps.getProperty("mail.smtp.host"));
        sessionProps.put("mail.smtp.port", mailProps.getProperty("mail.smtp.port"));

        String username = mailProps.getProperty("mail.username");
        String password = mailProps.getProperty("mail.password");

        Session session = Session.getInstance(sessionProps, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("Willkommen bei SliceIt – Bitte bestätige deine Registrierung");

            String htmlContent = """
                    <html>
                    <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
                        <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                            <h2 style="color: #333333;">👋 Willkommen bei <span style="color: #007bff;">SliceIt</span>!</h2>
                            <p style="font-size: 16px; color: #555555;">
                                Schön, dass du dich registriert hast. Bitte bestätige deine E-Mail-Adresse, indem du auf den folgenden Button klickst:
                            </p>
                            <p style="text-align: center;">
                                <a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #007bff; color: #ffffff; text-decoration: none; border-radius: 5px; font-weight: bold;">
                                    Registrierung bestätigen
                                </a>
                            </p>
                            <p style="font-size: 14px; color: #999999; margin-top: 30px;">
                                Falls der Button nicht funktioniert, kannst du auch diesen Link in deinem Browser öffnen:<br>
                                <a href="%s" style="color: #007bff;">%s</a>
                            </p>
                            <p style="font-size: 14px; color: #bbbbbb; margin-top: 40px;">Diese Nachricht wurde automatisch generiert. Bitte nicht darauf antworten.</p>
                        </div>
                    </body>
                    </html>
                    """.formatted(confirmationLink, confirmationLink, confirmationLink);

            message.setContent(htmlContent, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("Bestätigungs-E-Mail gesendet an: " + recipient);
            return true;
        } catch (MessagingException e) {
            logger.severe("Fehler beim Senden der E-Mail: " + e.getMessage());
            return false;
        }
    }
}
