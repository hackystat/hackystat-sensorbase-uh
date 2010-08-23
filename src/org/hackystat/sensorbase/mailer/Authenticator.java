/**
 * 
 */
package org.hackystat.sensorbase.mailer;

import javax.mail.PasswordAuthentication;
import static org.hackystat.sensorbase.server.ServerProperties.SMTP_SERVER_USER;
import static org.hackystat.sensorbase.server.ServerProperties.SMTP_SERVER_PASS;

/**
 * @author Martin Imme
 * 
 *         This class provides access to a username/password combination to the
 *         SMTP Server in order to use SFTP with authentication. The password
 *         has to be stored in the new properties SMTP_SERVER_USER and
 *         SMTP_SERVER_PASS If this property is empty, anonymous access is used.
 * 
 */
public class Authenticator extends javax.mail.Authenticator {

	/**
	 * Constructor to the class
	 */
	public Authenticator() {
		super();
	}

	/**
	 * Created and returns a PasswordAuthentication Object which contains the
	 * username/password combination for the authentication process
	 * 
	 * @return PasswordAuthentication the PasswordAuthentication Object
	 * 
	 * @see javax.mail.Authenticator#getPasswordAuthentication()
	 */
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		PasswordAuthentication passwordAuthentication = new PasswordAuthentication(
				System.getProperty(SMTP_SERVER_USER), 
				System.getProperty(SMTP_SERVER_PASS));

		return passwordAuthentication;
	}

}
