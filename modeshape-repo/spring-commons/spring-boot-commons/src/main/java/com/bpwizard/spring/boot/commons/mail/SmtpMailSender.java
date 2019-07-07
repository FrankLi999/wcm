package com.bpwizard.spring.boot.commons.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;


/**
 * An SMTP mail sender, which sends mails
 * using an injected JavaMailSender.
 * 
 * @author Sanjay Patel
 *
 */
public class SmtpMailSender implements MailSender<SpringMailData> {
	
	private static final Logger log = LogManager.getLogger(SmtpMailSender.class);

	private final JavaMailSender javaMailSender;
	
	public SmtpMailSender(JavaMailSender javaMailSender) {
		
		this.javaMailSender = javaMailSender;
		log.info("Created");
	}


	/**
	 * Sends a mail using a MimeMessageHelper
	 */
	@Override
	@Async
	public void send(SpringMailData mail) {
		
		log.info("Sending SMTP mail from thread " + Thread.currentThread().getName()); // toString gives more info    	

		// create a mime-message
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper;

		try {
			
			// create a helper
			helper = new MimeMessageHelper(message, true);
			// set the attributes
			helper.setSubject(mail.getSubject());
			helper.setTo(mail.getTo());
			helper.setText(mail.getBody(), true); // true indicates html
			// continue using helper object for more functionalities like adding attachments, etc.  
			
		} catch (MessagingException e) {

			throw new RuntimeException(e);
		}
		
		//send the mail
		javaMailSender.send(message);		
		log.info("Sent SMTP mail from thread " + Thread.currentThread().getName());    	
	}

}
