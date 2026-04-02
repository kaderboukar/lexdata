package com.lexdata.auth.security.services;

import com.lexdata.auth.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails transactionnels.
 *
 * <p>
 * En cas d'échec d'envoi (SMTP non disponible en dev), une erreur est loggée
 * mais ne plante pas le flux métier côté appelant (résilience).
 * </p>
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${lexdata.app.mail-from}")
    private String mailFrom;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie un email de réinitialisation de mot de passe.
     *
     * @param user      l'utilisateur qui a demandé la réinitialisation
     * @param resetLink le lien complet incluant le token
     */
    public void sendPasswordResetEmail(User user, String resetLink) {
        String subject = "LexData - Réinitialisation de votre mot de passe";
        String content = buildPasswordResetHtml(user.getFirstName(), resetLink);
        sendHtmlEmail(user.getEmail(), subject, content);
    }

    /**
     * Envoie un email de vérification d'adresse email.
     *
     * @param user       l'utilisateur nouvellement inscrit
     * @param verifyLink le lien complet incluant le token
     */
    public void sendEmailVerificationEmail(User user, String verifyLink) {
        String subject = "LexData - Vérifiez votre adresse email";
        String content = buildEmailVerificationHtml(user.getFirstName(), verifyLink);
        sendHtmlEmail(user.getEmail(), subject, content);
    }

    // ----------------------------------------------------------------
    // Méthode d'envoi générique
    // ----------------------------------------------------------------
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom != null ? mailFrom : "noreply@lexdata.sn");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email envoyé à : {} | Objet : {}", to, subject);
        } catch (MessagingException e) {
            log.error("Échec d'envoi d'email à {} : {}", to, e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Templates HTML inline
    // ----------------------------------------------------------------
    private String buildPasswordResetHtml(String firstName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head><meta charset="UTF-8"/></head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 600px; margin: auto; background: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #1a56db;">🔐 Réinitialisation de mot de passe</h2>
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Nous avons reçu une demande de réinitialisation de votre mot de passe LexData.</p>
                    <p>Cliquez sur le bouton ci-dessous pour choisir un nouveau mot de passe. Ce lien est valable <strong>15 minutes</strong>.</p>
                    <div style="text-align: center; margin: 30px 0;">
                      <a href="%s" target="_blank" rel="noopener noreferrer" style="background-color: #1a56db; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-size: 16px;">
                        Réinitialiser mon mot de passe
                      </a>
                    </div>
                    <p style="color: #666; font-size: 12px;">Si vous n'avez pas demandé cette réinitialisation, ignorez cet email. Votre mot de passe ne sera pas modifié.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin-top: 20px;"/>
                    <p style="color: #999; font-size: 11px;">© LexData — Plateforme Juridique du Sénégal</p>
                  </div>
                </body>
                </html>
                """
                .formatted(firstName, resetLink);
    }

    private String buildEmailVerificationHtml(String firstName, String verifyLink) {
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head><meta charset="UTF-8"/></head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 600px; margin: auto; background: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #057a55;">✅ Bienvenue sur LexData !</h2>
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Merci de vous être inscrit sur la plateforme juridique LexData. Votre compte a été créé avec succès.</p>
                    <p>Pour activer votre compte, cliquez sur le bouton ci-dessous :</p>
                    <div style="text-align: center; margin: 30px 0;">
                      <a href="%s" target="_blank" rel="noopener noreferrer" style="background-color: #057a55; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-size: 16px;">
                        Vérifier mon adresse email
                      </a>
                    </div>
                    <p style="color: #666; font-size: 12px;">Si vous n'avez pas créé de compte sur LexData, ignorez cet email.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin-top: 20px;"/>
                    <p style="color: #999; font-size: 11px;">© LexData — Plateforme Juridique du Sénégal</p>
                  </div>
                </body>
                </html>
                """
                .formatted(firstName, verifyLink);
    }
}
