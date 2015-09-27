package net.ko.internet;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;

public class KEmail {
	protected String to;
	protected String from;
	protected String subject;
	protected String text;
	protected KServer mailServer;
	protected Properties properties;
	protected MimeMessage message;
	protected List<String> files;

	public KEmail() {
		properties = System.getProperties();
		mailServer = new KServer();
		files = new ArrayList<>();
	}

	private void setContent(boolean isHTML) throws MessagingException {
		if (isHTML)
			message.setContent(text, "text/html");
		else
			message.setText(text);
	}

	private void setMultiPartContent() throws MessagingException {
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(text);

		Multipart multipart = new MimeMultipart();

		multipart.addBodyPart(messageBodyPart);

		for (String file : files) {
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(file);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(file);
			multipart.addBodyPart(messageBodyPart);
		}
		message.setContent(multipart);
	}

	public void createMessage() {
		properties.setProperty("mail.smtp.host", mailServer.getHost());
		if (mailServer.isAuthentification()) {
			properties.setProperty("mail.user", mailServer.getUserName());
			properties.setProperty("mail.password", mailServer.getPassword());
		}
		Session session = Session.getDefaultInstance(properties);
		message = new MimeMessage(session);
	}

	public boolean send(boolean isHTML) {
		boolean result = false;
		if (message != null) {
			try {
				message.setFrom(new InternetAddress(from));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
				message.setSubject(subject);
				if (files.size() > 0)
					setMultiPartContent();
				else
					setContent(isHTML);
				Transport.send(message);
				KDebugConsole.print("message envoyé à " + to, "EMAIL", "KEmail.send");
			} catch (MessagingException e) {
				Ko.klogger().log(Level.SEVERE, "", e);
				result = false;
			}
		} else {
			Ko.klogger().log(Level.SEVERE, "Vous devez appeler la méthode createMessage, le message n'est pas créé");
		}
		return result;
	}

	public boolean send() {
		return this.send(false);
	}

	public void addRecipients(RecipientType type, String[] strAddresses) throws MessagingException {
		if (messageExists()) {
			Address[] addresses = new Address[strAddresses.length];
			for (int i = 0; i < strAddresses.length; i++) {
				addresses[i] = new InternetAddress(strAddresses[i]);
			}
			message.addRecipients(type, addresses);
		}
	}

	public void addRecipients(RecipientType type, List<String> strAddresses) throws MessagingException {
		if (messageExists()) {
			Address[] addresses = new Address[strAddresses.size()];
			for (int i = 0; i < strAddresses.size(); i++) {
				addresses[i] = new InternetAddress(strAddresses.get(i));
			}
			message.addRecipients(type, addresses);
		}
	}

	public void addRecipient(RecipientType type, String strAddress) throws MessagingException {
		message.addRecipient(type, new InternetAddress(strAddress));
	}

	public KServer getMailServer() {
		return mailServer;
	}

	public void setMailServer(KServer mailServer) {
		this.mailServer = mailServer;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public MimeMessage getMessage() {
		return message;
	}

	public boolean messageExists() {
		boolean result = message != null;
		if (!result)
			Ko.klogger().log(Level.SEVERE, "Vous devez appeler la méthode createMessage, le message n'est pas créé");
		return result;
	}
}
