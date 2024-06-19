package com.cyberintech.vrisk.server.service.dashboards.powerbi;

import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration class
 */
@Component
@Getter
@ToString
public class PowerBIConfig {

	// Set this to true, to show debug statements in console
	private boolean DEBUG = false;

	//	Two possible Authentication methods:
	//	- For authentication with master user credential choose MasterUser as AuthenticationType.
	//	- For authentication with app secret choose ServicePrincipal as AuthenticationType.
	//	More details here: https://aka.ms/EmbedServicePrincipal
	@Value("${analytics.powerbi.authenticationType:MasterUser}")
	private String authenticationType;

	// Enter MasterUser credentials
	@Value("${analytics.powerbi.pbiUsername:powerbi@risk-q.com}")
	private String pbiUsername;

	@Value("${analytics.powerbi.pbiPassword:OBISsolutions9999!}")
	private String pbiPassword;

	//	DO NOT CHANGE
	@Value("${analytics.powerbi.authorityUrl:https://login.microsoftonline.com/}")
	private String authorityUrl;

	@Value("${analytics.powerbi.scopeBase:https://analysis.windows.net/powerbi/api/.default}")
	private String scopeBase;

	@Value("${analytics.powerbi.tenantId:fc371555-8bd2-4c98-9e42-e82be54ddb36}")
	private String tenantId;

	@Value("${analytics.powerbi.subscriptionId:39cf4421-39df-4262-a32f-cb1baaf5555a}")
	private String subscriptionId;

	@Value("${analytics.powerbi.clientId:b0ff74e0-818d-4b3e-8756-5f1c5ec5b35e}")
	private String clientId;

	@Value("${analytics.powerbi.appSecret:rmT8Q~lkUT5a2vnBGD6tY9n34OuVPjKzqLXVRc1Q}")
	private String clientSecret;

	@Value("${analytics.powerbi.resourceGroup:PowerBI_Reports}")
	private String resourceGroup;

	@Value("${analytics.powerbi.capacity:none}")
	private String capacityName;

	@Value("${analytics.powerbi.capacityExpirationMinutes:30}")
	private Integer capacityExpirationMinutes;

}
