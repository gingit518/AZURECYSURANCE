package com.cyberintech.vrisk.server.integration.bigid.client.auth;

import com.cyberintech.vrisk.server.integration.bigid.batch.BigIdCacheCustomizer;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationSecurityCertificates;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationSecurityCertificatesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;

import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
public class BigIdAuthorizationTokenProvider {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private OrganizationSecurityCertificatesRepository certificatesRepository;

	/**
	 * Obtaine new authorization token from BigId.
	 * 
	 * @param  organizationId Organization identifier.
	 * @return                The token if it could be obtained. null otherwise.
	 */
	public String getToken(Long organizationId) {
		// Find token in cache
		AuthorizationToken token = getCache().get(organizationId);

		Date now = new Date();
		if (token != null && now.before(token.getExpiredAt())) {
			log.debug("BigID authorization token for organization [{}] found at the cache", organizationId);
			return token.getValue();
		}

		// Obtain new token from BigID
		Optional<OrganizationSecurityCertificates> certificateOpt = certificatesRepository
			.findFirstByOrganizationIdAndCertificateTypeAndIsActive(organizationId,
				ExternalAnalyticsType.BIG_ID, true);

		if (certificateOpt.isEmpty()) {
			log.warn("No BigID configuration for organization [{}]", organizationId);
			return null;
		}

		OrganizationSecurityCertificates certificate = certificateOpt.get();
		BigidConfigurationProperties configurationProperties = new BigidConfigurationProperties(
			certificate.getBaseUrl(),
			certificate.getCliendId(), certificate.getClientSecret());
		AuthenticationClient authenticationClient = new AuthenticationClient(configurationProperties);
		AuthorizationToken newToken = authenticationClient.getToken();

		if (newToken != null) {
			// Put new token to cache
			log.debug("BigID authorization token for organization [{}] obtained from BigID", organizationId);
			getCache().put(organizationId, newToken);
			return newToken.getValue();
		}
		log.warn("BigID authorization token for organization [{}] NOT obtained", organizationId);
		return null;
	}

	public void evictToken(Long organizationId) {
		getCache().remove(organizationId);
	}

	private Cache<Long, AuthorizationToken> getCache() {
		Cache cache = cacheManager.getCache(BigIdCacheCustomizer.ACCESS_TOKEN_CACHE);
		return cache;
	}

}
