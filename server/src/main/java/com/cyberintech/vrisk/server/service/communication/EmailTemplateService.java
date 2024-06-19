package com.cyberintech.vrisk.server.service.communication;

import com.cyberintech.vrisk.server.model.dto.document.EmailTemplateDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationEmailTemplateType;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationEmailTemplateRepository;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.service.UserPasswordResetLinksService;
import com.cyberintech.vrisk.server.service.UserService;
import com.cyberintech.vrisk.server.service.dashboards.DashboardsConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Email Template Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-08-29
 */
@Service
public class EmailTemplateService {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	@Qualifier("templateEngineContent")
	private TemplateEngine templateEngineContent;

	@Autowired
	@Qualifier("templateEngineFile")
	private TemplateEngine templateEngineFile;

	@Autowired
	private UserPasswordResetLinksService userPasswordResetLinksService;

	@Autowired
	private OrganizationEmailTemplateRepository organizationEmailTemplateRepository;

	/**
	 * Get user registration email
	 *
	 * @return
	 */
	public EmailTemplateDTO getUserRegistrationEmailTemplate(Users user) {

		UserPasswordResetLinks linkDetails = userPasswordResetLinksService.create(user, 259200l);

		String fullName = StringUtils.isEmpty(user.getFullName()) ? user.buildFullName() : user.getFullName();
		final Context ctx = new Context(Locale.ENGLISH);
		ctx.setVariable("name", fullName);
		ctx.setVariable("accountName", user.getEmail());
		ctx.setVariable("resetPasswordLink", userPasswordResetLinksService.getLinkUrl(linkDetails));
		String htmlContent = "";
		String subject = "";

		if (UserService.hasRole(RoleType.VENDOR_EMPLOYEE.role(), user)) {
			Optional<OrganizationEmailTemplates> templateOptional = organizationEmailTemplateRepository.findByOrganizationIdAndType(user.getOrganization().getId(), OrganizationEmailTemplateType.VENDOR_EMPLOYEE_INVITATION);

			if (templateOptional.isPresent()) {
				OrganizationEmailTemplates template = templateOptional.get();
				ctx.setVariable("organizationName", user.getOrganization().getName());

				htmlContent = this.templateEngineContent.process(template.getContent(), ctx);
				subject = template.getSubject();

			} else {
				htmlContent = this.templateEngineFile.process("user-registration-email.html", ctx);
				subject = "User registration on ValuRisQ";
			}

		} else {
			htmlContent = this.templateEngineFile.process("user-registration-email.html", ctx);
			subject = "User registration on ValuRisQ";

		}

		return new EmailTemplateDTO(subject, htmlContent);
	}

	/**
	 * Get password reset email
	 *
	 * @return Answer Weights List
	 */
	public EmailTemplateDTO getResetPasswordEmailTemplate(Users user) {

		UserPasswordResetLinks linkDetails = userPasswordResetLinksService.create(user);

		final Context ctx = new Context(Locale.ENGLISH);
		ctx.setVariable("name", user.getFullName());
		ctx.setVariable("resetPasswordLink", userPasswordResetLinksService.getLinkUrl(linkDetails));
		final String htmlContent = this.templateEngineFile.process("forgot-password-email.html", ctx);
		final String subject = "Password Restore on ValuRisQ";

		return new EmailTemplateDTO(subject, htmlContent);
	}

	/**
	 * Get email notification for User when new System Assigned
	 *
	 * @param userAssignedItem
	 */
	public EmailTemplateDTO getUserAssignmentTemplate(UserAssignedSystem userAssignedItem) {

		Users user = userAssignedItem.getUser();

		final Context ctx = new Context(Locale.ENGLISH);
		ctx.setVariable("name", user.getFullName());
		ctx.setVariable("systemName", userAssignedItem.getSystem().getName());
		ctx.setVariable("isDataExfiltration", true);
		ctx.setVariable("dataExfiltrationLink", applicationProperties.getUiUrl() + "/private/data-exfiltration/list?filter=" + URLEncoder.encode(userAssignedItem.getSystem().getName(), Charset.defaultCharset()).replaceAll("\\+", "%20"));
		ctx.setVariable("impactLink", applicationProperties.getUiUrl() + DashboardsConfig.buildSystemRiskQualQuestionsUrl(userAssignedItem.getSystem().getId(), "impact"));
		ctx.setVariable("likelihoodLink", applicationProperties.getUiUrl() + DashboardsConfig.buildSystemRiskQualQuestionsUrl(userAssignedItem.getSystem().getId(), "likelihood"));
		final String htmlContent = this.templateEngineFile.process("reassign-system-email.html", ctx);
		final String subject = "You are assigned to the system on ValuRisQ";

		return new EmailTemplateDTO(subject, htmlContent);
	}

	/**
	 * Send email notification for User when new Vendor Assigned
	 *
	 * @param userAssignedItem
	 */
	public EmailTemplateDTO getUserAssignmentTemplate(UserAssignedVendor userAssignedItem) {
		Users user = userAssignedItem.getUser();

		final Context ctx = new Context(Locale.ENGLISH);
		ctx.setVariable("name", user.getFullName());
		ctx.setVariable("vendorName", userAssignedItem.getVendor().getName());
		ctx.setVariable("impactLink", applicationProperties.getUiUrl() + DashboardsConfig.buildVendorRiskQualQuestionsUrl(userAssignedItem.getVendor().getId(), "impact"));
		ctx.setVariable("likelihoodLink", applicationProperties.getUiUrl() + DashboardsConfig.buildVendorRiskQualQuestionsUrl(userAssignedItem.getVendor().getId(), "likelihood"));
		ctx.setVariable("isCloudVendor", userAssignedItem.getVendor().getIsCloudVendor());
		if (Boolean.TRUE.equals(userAssignedItem.getVendor().getIsCloudVendor())) {
			ctx.setVariable("cloudImpactLink", applicationProperties.getUiUrl() + DashboardsConfig.buildCloudRiskQualQuestionsUrl(userAssignedItem.getVendor().getId(), "impact"));
			ctx.setVariable("cloudLikelihoodLink", applicationProperties.getUiUrl() + DashboardsConfig.buildCloudRiskQualQuestionsUrl(userAssignedItem.getVendor().getId(), "likelihood"));
		}
		final String htmlContent = this.templateEngineFile.process("reassign-vendor-email.html", ctx);
		final String subject = "You are assigned to the vendor/cloud on ValuRisQ";

		return new EmailTemplateDTO(subject, htmlContent);
	}

}
