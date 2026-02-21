package com.cyberintech.vrisk.server.service.communication;

import com.cyberintech.vrisk.server.model.dto.document.EmailTemplateDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.UserAssignedSystem;
import com.cyberintech.vrisk.server.model.jpa.entity.UserAssignedVendor;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

/**
 * Email send Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-08-30
 */
@Slf4j
@Component
@Profile("!azure")
public class SMTPEmailService implements EmailService {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private EmailTemplateService emailTemplateService;

	@Lazy
	@Autowired(required = false)
	private JavaMailSender emailSender;

	/**
	 * Send user registration email
	 *
	 * @return Answer Weights List
	 */
	public void sendUserRegistrationEmail(Users user) {

		EmailTemplateDTO emailTemplate = emailTemplateService.getUserRegistrationEmailTemplate(user);

		try {
			// Prepare message using a Spring helper
			final MimeMessage mimeMessage = getMimeMessage(user, emailTemplate.getSubject(), emailTemplate.getHtmlContent());
			emailSender.send(mimeMessage);
		} catch (MessagingException | UnsupportedEncodingException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException(MessageFormat.format("Failed to send user registration email to [{0}]", user.getEmail()), ApplicationExceptionCodes.USER_REGISTRATION_EMAIL_FAILED);
		}

	}

	/**
	 * Send password reset email
	 *
	 * @return Answer Weights List
	 */
	public void sendResetPasswordEmail(Users user) {

		EmailTemplateDTO emailTemplate = emailTemplateService.getResetPasswordEmailTemplate(user);

		try {
			// Prepare message using a Spring helper
			final MimeMessage mimeMessage = getMimeMessage(user, emailTemplate.getSubject(), emailTemplate.getHtmlContent());
			emailSender.send(mimeMessage);
		} catch (MessagingException | UnsupportedEncodingException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException(MessageFormat.format("Failed to send reset password email to [{0}]", user.getEmail()), ApplicationExceptionCodes.RESET_PASSWORD_LINK_EMAIL_FAILED);
		}

	}

	/**
	 * Send email notification for User when new System Assigned
	 *
	 * @param userAssignedItem
	 */
	public void sendUserAssignment(UserAssignedSystem userAssignedItem) {

		EmailTemplateDTO emailTemplate = emailTemplateService.getUserAssignmentTemplate(userAssignedItem);
		Users user = userAssignedItem.getUser();

		try {
			// Prepare MIME Message for User and HTML Content
			final MimeMessage mimeMessage = getMimeMessage(user, emailTemplate.getSubject(), emailTemplate.getHtmlContent());
			emailSender.send(mimeMessage);
		} catch (MessagingException | UnsupportedEncodingException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException(MessageFormat.format("Failed to send system assignment email to [{0}]", user.getEmail()), ApplicationExceptionCodes.ASSIGNED_SYSTEM_EMAIL_FAILED);
		}
	}

	/**
	 * Send email notification for User when new Vendor Assigned
	 *
	 * @param userAssignedItem
	 */
	public void sendUserAssignment(UserAssignedVendor userAssignedItem) {

		EmailTemplateDTO emailTemplate = emailTemplateService.getUserAssignmentTemplate(userAssignedItem);
		Users user = userAssignedItem.getUser();

		try {
			// Prepare MIME Message for User and HTML Content
			final MimeMessage mimeMessage = getMimeMessage(user, emailTemplate.getSubject(), emailTemplate.getHtmlContent());
			emailSender.send(mimeMessage);
		} catch (MessagingException | UnsupportedEncodingException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException(MessageFormat.format("Failed to send vendor assignment email to [{0}]", user.getEmail()), ApplicationExceptionCodes.ASSIGNED_VENDOR_EMAIL_FAILED);
		}
	}

	/**
	 * Build MIME Message for User and HTML content
	 *
	 * @param user
	 * @param subject
	 * @param htmlContent
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	private MimeMessage getMimeMessage(Users user, String subject, String htmlContent) throws UnsupportedEncodingException, MessagingException {
		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = emailSender.createMimeMessage();

		InternetAddress fromAddress = new InternetAddress(applicationProperties.getEmailMessageFromAddress());
		InternetAddress toAddress = new InternetAddress(user.getEmail(), user.getFullName());

		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
		message.setSubject(subject);
		message.setFrom(fromAddress);
		message.setReplyTo(fromAddress);
		message.setTo(toAddress);
		message.setText(htmlContent, true);
		return mimeMessage;
	}

}
