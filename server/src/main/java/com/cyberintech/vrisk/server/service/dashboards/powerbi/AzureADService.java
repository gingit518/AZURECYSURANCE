package com.cyberintech.vrisk.server.service.dashboards.powerbi;

import com.microsoft.aad.msal4j.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

/**
 * Service to authenticate using MSAL
 */
@Service
@Slf4j
public class AzureADService {

	@Autowired
	private PowerBIConfig powerBIConfig;

	/**
	 * Acquires access token for the based on config values
	 * @return AccessToken
	 */
	/*
	public String getAccessToken() throws MalformedURLException, InterruptedException, ExecutionException {
		return getAccessToken(powerBIConfig.getClientId());
	}
	*/

	/**
	 * Acquires access token for the based on config values
	 * @return AccessToken
	 */
	public String getAccessToken(String clientId) throws MalformedURLException, InterruptedException, ExecutionException {

		if (powerBIConfig.getAuthenticationType().equalsIgnoreCase("MasterUser")) {
			return getAccessTokenUsingMasterUser(clientId, powerBIConfig.getPbiUsername(), powerBIConfig.getPbiPassword());
		} else if (powerBIConfig.getAuthenticationType().equalsIgnoreCase("ServicePrincipal")) {
			// Check if Tenant Id is empty
			if (powerBIConfig.getTenantId().isEmpty()) {
				throw new RuntimeException("Tenant Id is empty");
			}
			return getAccessTokenUsingServicePrincipal(clientId, powerBIConfig.getTenantId(), powerBIConfig.getClientSecret());
		} else {
			// Authentication Type is none of the above
			throw new RuntimeException("Invalid authentication type: " + powerBIConfig.getAuthenticationType());
		}
	}

	/**
	 * Acquires access token for the given clientId and app secret
	 *
	 * @param clientId
	 * @param tenantId
	 * @param appSecret
	 * @return AccessToken
	 */
	private String getAccessTokenUsingServicePrincipal(String clientId, String tenantId, String appSecret) throws MalformedURLException, InterruptedException, ExecutionException {

		// Build Confidential Client App
		ConfidentialClientApplication app = ConfidentialClientApplication.builder(
					clientId,
					ClientCredentialFactory.createFromSecret(appSecret))
					.authority(powerBIConfig.getAuthorityUrl() + tenantId)
					.build();

		ClientCredentialParameters clientCreds = ClientCredentialParameters.builder(Collections.singleton(powerBIConfig.getScopeBase())).build();

		// Acquire new AAD token
		IAuthenticationResult result = app.acquireToken(clientCreds).get();

		// Return access token if token is acquired successfully
		if (result != null && result.accessToken() != null && !result.accessToken().isEmpty()) {
			log.info("[PowerBI] Authenticated with Service Principal mode");

			return result.accessToken();
		} else {
			log.error("[PowerBI] Failed to authenticate with Service Principal mode");

			return null;
		}
	}

	/**
	 * Acquires access token for the given clientId and user credentials
	 * @param clientId
	 * @param username
	 * @param password
	 * @return AccessToken
	 */
	private String getAccessTokenUsingMasterUser(String clientId, String username, String password) throws MalformedURLException, InterruptedException, ExecutionException {

		// Build Public Client App
		PublicClientApplication app = PublicClientApplication.builder(clientId)
				.authority(powerBIConfig.getAuthorityUrl() + powerBIConfig.getTenantId())	// Use authorityUrl+tenantId if this doesn't work
				// .authority(powerBIConfig.getAuthorityUrl() + "organizations")	// Use authorityUrl+tenantId if this doesn't work
				.build();

		UserNamePasswordParameters userCreds = UserNamePasswordParameters.builder(
				new HashSet<>(Arrays.asList(powerBIConfig.getScopeBase())),
				username,
				password.toCharArray()).tenant(powerBIConfig.getTenantId()).build();

		log.error("Client ID: [" + clientId + "], Authority: [" + powerBIConfig.getAuthorityUrl() + powerBIConfig.getTenantId() + "]");
		log.error("PowerBI Config: [" + powerBIConfig.toString() + "]");

		// Acquire new AAD token
		IAuthenticationResult result = app.acquireToken(userCreds).get();

		// Return access token if token is acquired successfully
		if (result != null && result.accessToken() != null && !result.accessToken().isEmpty()) {
			log.info("[PowerBI] Authenticated with MasterUser mode");

			return result.accessToken();
		} else {
			log.error("[PowerBI] Failed to authenticate with MasterUser mode");

			return null;
		}
	}
}
