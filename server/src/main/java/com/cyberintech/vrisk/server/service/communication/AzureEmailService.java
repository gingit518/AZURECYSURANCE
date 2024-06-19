package com.cyberintech.vrisk.server.service.communication;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.cyberintech.vrisk.server.model.dto.document.EmailTemplateDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.UserAssignedSystem;
import com.cyberintech.vrisk.server.model.jpa.entity.UserAssignedVendor;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Email send Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-08-30
 */
@Slf4j
@Component
@Profile("azure")
public class AzureEmailService implements EmailService {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private EmailTemplateService emailTemplateService;

	@Value("${cloud.azure.communication.email.connectionString:}")
	private String azureEmailConnectionString;

	@Value("${cloud.azure.communication.email.from:}")
	private String emailFrom;

	private EmailClient emailClient;

	@PostConstruct
	private void postConstruct() {
		log.info("## Trying to initialize Azure Communication Services");
		emailClient = new EmailClientBuilder().connectionString(azureEmailConnectionString).buildClient();
		log.info("## Azure Communication Services initialized");
	}

	/**
	 * Send user registration email
	 *
	 * @return Answer Weights List
	 */
	public void sendUserRegistrationEmail(Users user) {

		EmailTemplateDTO emailTemplate = emailTemplateService.getUserRegistrationEmailTemplate(user);

		try {
			// Send Message for User and HTML Content
			sendUserMessage(user, emailTemplate.getSubject(), emailTemplate.getHtmlContent());
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
			// Send Message for User and HTML Content
			sendUserMessage(user, emailTemplate.getSubject(), emailTemplate.getHtmlContent());
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

		final EmailTemplateDTO emailTemplate = emailTemplateService.getUserAssignmentTemplate(userAssignedItem);
		final Users user = userAssignedItem.getUser();

		CompletableFuture.runAsync(() -> {
			try {
				// Send Message for User and HTML Content
				sendUserMessage(user, emailTemplate.getSubject(), emailTemplate.getHtmlContent());
			} catch (MessagingException | UnsupportedEncodingException e) {
				log.warn(e.getMessage(), e);
				throw new InternalServerErrorException(MessageFormat.format("Failed to send system assignment email to [{0}]", user.getEmail()), ApplicationExceptionCodes.ASSIGNED_SYSTEM_EMAIL_FAILED);
			}
		});
	}

	/**
	 * Send email notification for User when new Vendor Assigned
	 *
	 * @param userAssignedItem
	 */
	public void sendUserAssignment(UserAssignedVendor userAssignedItem) {

		final EmailTemplateDTO emailTemplate = emailTemplateService.getUserAssignmentTemplate(userAssignedItem);
		final Users user = userAssignedItem.getUser();

		CompletableFuture.runAsync(() -> {
			try {
				// Send Message for User and HTML Content
				sendUserMessage(user, emailTemplate.getSubject(), emailTemplate.getHtmlContent());
			} catch (MessagingException | UnsupportedEncodingException e) {
				log.warn(e.getMessage(), e);
				throw new InternalServerErrorException(MessageFormat.format("Failed to send vendor assignment email to [{0}]", user.getEmail()), ApplicationExceptionCodes.ASSIGNED_VENDOR_EMAIL_FAILED);
			}
		});
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
	private Boolean sendUserMessage(Users user, String subject, String htmlContent) throws UnsupportedEncodingException, MessagingException {
		Boolean result = false;

		EmailMessage message = new EmailMessage()
			.setSenderAddress(emailFrom)
			.setToRecipients(new EmailAddress(user.getEmail()).setDisplayName(user.getFullName()))
			.setSubject(subject)
			.setBodyHtml(htmlContent);

		try
		{

			log.info(String.format("#### Trying to send email from Azure: %s, %s", user.getEmail(), subject));
			SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message, null);

			PollResponse<EmailSendResult> pollResponse = null;

			Duration POLLER_WAIT_TIME = Duration.ofSeconds(1);
			Duration timeElapsed = Duration.ofSeconds(0);

			while (pollResponse == null
				|| pollResponse.getStatus() == LongRunningOperationStatus.NOT_STARTED
				|| pollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {

				pollResponse = poller.poll();
				log.info("Email send poller status: " + pollResponse.getStatus());

				Thread.sleep(POLLER_WAIT_TIME.toMillis());
				timeElapsed = timeElapsed.plus(POLLER_WAIT_TIME);

				if (timeElapsed.compareTo(POLLER_WAIT_TIME.multipliedBy(18)) >= 0)
				{
					throw new RuntimeException("Polling timed out.");
				}
			}

			if (poller.getFinalResult().getStatus() == EmailSendStatus.SUCCEEDED) {
				log.info(String.format("Successfully sent the email (operation id: %s)", poller.getFinalResult().getId()));
			} else {
				throw new RuntimeException(poller.getFinalResult().getError().getMessage());
			}
		} catch (Exception exception) {
			System.out.println(exception.getMessage());
		}

		return result;
	}

}
