package com.cyberintech.vrisk.server.service.azure;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager;
import com.azure.resourcemanager.powerbidedicated.models.DedicatedCapacity;
import com.azure.resourcemanager.powerbidedicated.models.State;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.PowerBIConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * PowerBI Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-24
 */
@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class PowerBIAdminService {

	private final PowerBIConfig powerBIConfig;

	/**
	 * Calculates PowerBI capacity Expiration Date
	 */
	public Date calculatePowerBIEmbedCapacityExpirationDate() {
		return LocalDateTime.now().plusMinutes(powerBIConfig.getCapacityExpirationMinutes()).toDate();
	}

	/**
	 * Start PowerBI capacity
	 */
	public boolean startPowerBIEmbedCapacity() {
		boolean result = false;
		Optional<DedicatedCapacity> powerBICapacityOpt = getDedicatedCapacity();
		if (powerBICapacityOpt.isPresent()) {
			DedicatedCapacity powerBICapacity = powerBICapacityOpt.get();
			if (State.PAUSED.equals(powerBICapacity.state()) || State.PAUSING.equals(powerBICapacity.state()) || State.SUSPENDED.equals(powerBICapacity.state()) || State.SUSPENDING.equals(powerBICapacity.state())) {
				powerBICapacity.resume();
				log.info("## Starting PowerBI Embed environment: " + powerBIConfig.getCapacityName());
			} else {
				log.info("## PowerBI Embed environment is not PAUSED/SUSPENDED, skipping: " + powerBIConfig.getCapacityName());
			}
			result = true;

		}


		return result;
	}

	/**
	 * Suspend PowerBI capacity
	 */
	public boolean suspendPowerBIEmbedCapacity() {
		boolean result = false;
		Optional<DedicatedCapacity> powerBICapacityOpt = getDedicatedCapacity();
		if (powerBICapacityOpt.isPresent()) {
			DedicatedCapacity powerBICapacity = powerBICapacityOpt.get();
			if (State.SUCCEEDED.equals(powerBICapacity.state()) || State.RESUMING.equals(powerBICapacity.state()) || State.PREPARING.equals(powerBICapacity.state())) {
				powerBICapacity.suspend();
				log.info("## Suspending PowerBI Embed environment: " + powerBIConfig.getCapacityName());
				result = true;
			} else if (State.PAUSED.equals(powerBICapacity.state())) {
				log.info("## PowerBI Embed environment is PAUSED, skipping: " + powerBIConfig.getCapacityName());
				result = true;
			}
		}

		return result;
	}

	@NotNull
	private Optional<DedicatedCapacity> getDedicatedCapacity() {
		AzureProfile profile = new AzureProfile(powerBIConfig.getTenantId(), powerBIConfig.getSubscriptionId(), AzureEnvironment.AZURE);
		TokenCredential credential = new ClientSecretCredentialBuilder()
			.authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
			.tenantId(powerBIConfig.getTenantId())
			.clientId(powerBIConfig.getClientId()).clientSecret(powerBIConfig.getClientSecret())
			.build();
		// TokenCredential credential = new EnvironmentCredentialBuilder().authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint()).build();
		PowerBIDedicatedManager manager = PowerBIDedicatedManager.authenticate(credential, profile);

		PagedIterable<DedicatedCapacity> availableCapacitiesIterator = manager.capacities().listByResourceGroup(powerBIConfig.getResourceGroup(), Context.NONE);
		List<DedicatedCapacity> availableCapacities = availableCapacitiesIterator.stream().toList();

		Optional<DedicatedCapacity> powerBICapacityOpt = availableCapacities.stream().filter(dedicatedCapacity -> dedicatedCapacity.name().equalsIgnoreCase(powerBIConfig.getCapacityName())).findFirst();
		return powerBICapacityOpt;
	}
}
