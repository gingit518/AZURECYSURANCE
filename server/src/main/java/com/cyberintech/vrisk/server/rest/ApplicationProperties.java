package com.cyberintech.vrisk.server.rest;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Simple implementation of the Application properties helper. Used in API calls, etc.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-17
 */
@Getter
public class ApplicationProperties {

	@Value("${build.version}")
	private String buildVersion;

	@Value("${build.artifactId}")
	private String buildArtifact;

	@Value("${vrisk.ui.url}")
	private String uiUrl;

	@Value("${vrisk.admin-ui.url}")
	private String adminUiUrl;

	@Value("${vrisk.api.url}")
	private String apiUrl;

	@Autowired
	private Environment environment;

	@Getter
	@Setter
	@Value("${vrisk.documents.public-access-permitted:true}")
	private boolean isPublicAccessPermitted;

	public boolean isEmailNotificationsEnabled() {
		boolean result = false;

		if ("true".equalsIgnoreCase(environment.getProperty("vrisk.notifications.email.enabled"))) {
			result = true;
		}

		return result;
	}

	public String getEmailMessageFromAddress() {
		String result = "Do Not Reply <noreply@cyberinnovativetech.com>";

		if (environment.containsProperty("vrisk.notifications.email.from")) {
			result = environment.getProperty("vrisk.notifications.email.from");
		}

		return result;
	}

	public String buildExportFileName(String baseName, String organizationCode, String extension) {
		return buildExportFileName(baseName, organizationCode, null, extension);
	}

	public String buildExportFileName(String baseName, String organizationCode, String postfix, String extension) {
		String result = "ValuRisQ";

		if (StringUtils.isNotEmpty(baseName)) {
			result += "_" + baseName;
		}

		if (StringUtils.isNotEmpty(organizationCode)) {
			result += "." + organizationCode;
		}

		if (StringUtils.isNotEmpty(postfix)) {
			result += "." + postfix;
		}

		if (StringUtils.isNotEmpty(extension)) {
			result += "." + extension;
		}

		return result;
	}

	/**
	 * Get Hostname for the UI
	 *
	 * @return
	 */
	public String getUiHostname() {
		String hostname = "";

		URI uiUri = getUiUri();
		if (uiUri != null) {
			hostname = uiUri.getHost();
		}

		return hostname;
	}

	public URI getUiUri() {
		try {
			URI result = new URI(getUiUrl());

			return result;
		} catch (URISyntaxException e) {
			return null;
		}
	}

}
