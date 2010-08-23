package org.hackystat.sensorbase.mailer;

import java.util.Date;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.hackystat.sensorbase.server.ServerProperties.SMTP_HOST_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_EMAIL_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.SMTP_SERVER_USER;
import static org.hackystat.sensorbase.server.ServerProperties.TEST_INSTALL_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;

/**
 * Provides a wrapper for SensorBase email services. Use the singleton instance
 * to send emails: Mailer.getInstance().send(). To aid in testing, no emails are
 * sent if the Hackystat server is a test installation, or the user email is in
 * hackystat test domain.
 * 
 * @author Philip M. Johnson
 */
public class Mailer {

    /** Administrator's email. */
    private String adminEmail;

    /**
     * If this SensorBase exists purely for testing, should be 'true' or
     * 'false'.
     */
    private String testInstall;

    /** The domain for test users, such as "hackystat.org". */
    private String testDomain;

    /** Session object. */
    private Session session;

    /** Mailer object. */
    private static Mailer mailer;

    /** The singleton instance maintaining an Email session. */
    private Mailer() {
        Properties props = new Properties();
        props.put("mail.smtp.host", System.getProperty(SMTP_HOST_KEY));

        /* If a username / password combination is defined for the smtp server,
         * let's create the authenticator object
         */
        String smtpServerUser =  System.getProperty(SMTP_SERVER_USER);
        if (smtpServerUser == null
                || "".equals(smtpServerUser)) {
            props.put("mail.smtp.auth", "false");
            this.session = Session.getInstance(props);
        } 
        else {
            // set the corresponding property to TRUE, otherwise, authentication
            // is not used
            props.put("mail.smtp.auth", "true");
            SmtpAuthenticator smtpAuthenticator = new SmtpAuthenticator();
            this.session = Session.getInstance(props, smtpAuthenticator);
        }
        this.adminEmail = "Hackystat Admin <"
                + System.getProperty(ADMIN_EMAIL_KEY) + ">";
        this.testInstall = System.getProperty(TEST_INSTALL_KEY).toLowerCase();
        this.testDomain = System.getProperty(TEST_DOMAIN_KEY);
    }

    /**
     * Returns the singleton instance of Mailer, creating it if necesssary.
     * 
     * @return The singleton Mailer instance.
     */
    public static Mailer getInstance() {
        if (mailer == null) {
            Mailer.mailer = new Mailer();
        }
        return Mailer.mailer;
    }

    /**
     * Attempts to send an email. To aid in testing, no emails are sent if the
     * Hackystat server is a test installation, or the user email is in
     * hackystat test domain. Returns false if the send fails.
     * 
     * @param fromAddr
     *            The email address from
     * @param toAddr
     *            The email address to send to.
     * @param subject
     *            The subject of the email.
     * @param body
     *            The email body.
     * @return True if no error occurred during send, false otherwise
     */
    public boolean send(String fromAddr, String toAddr, String subject,
            String body) {
        if (this.testInstall.equals("true") || toAddr.endsWith(this.testDomain)) {
            return true;
        }
        try {
            Message msg = new MimeMessage(this.session);
            InternetAddress adminAddress = new InternetAddress(fromAddr);
            InternetAddress[] adminAddressArray = { adminAddress };
            InternetAddress userAddress = new InternetAddress(toAddr);

            msg.setFrom(adminAddress);
            msg.setReplyTo(adminAddressArray);
            msg.setRecipient(Message.RecipientType.TO, userAddress);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(body);

            Transport.send(msg);
            return true;
        } 
        catch (MessagingException mex) {
            Logger logger = LogManager.getLogManager().getLogger(
                    "org.hackystat.sensorbase");
            logger.warning("Mail failure to: " + toAddr + "\n" + mex);
            return false;
        }

    }

    /**
     * Attempts to send an email. To aid in testing, no emails are sent if the
     * Hackystat server is a test installation, or the user email is in
     * hackystat test domain. Returns false if the send fails.
     * 
     * @param toAddr
     *            The email address to send to.
     * @param subject
     *            The subject of the email.
     * @param body
     *            The email body.
     * @return True if no error occurred during send, false otherwise
     */
    public boolean send(String toAddr, String subject, String body) {
        return this.send(this.adminEmail, toAddr, subject, body);
    }
}
