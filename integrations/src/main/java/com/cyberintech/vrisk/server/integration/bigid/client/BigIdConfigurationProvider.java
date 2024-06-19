package com.cyberintech.vrisk.server.integration.bigid.client;

import com.cyberintech.vrisk.server.integration.bigid.batch.BigIdCacheCustomizer;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationSecurityCertificates;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationSecurityCertificatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;

import java.util.Optional;

@Component
public class BigIdConfigurationProvider {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private OrganizationSecurityCertificatesRepository certificatesRepository;

	private Cache<Long, BigidConfigurationProperties> getCache() {
		Cache<Long, BigidConfigurationProperties> cache = cacheManager.getCache(BigIdCacheCustomizer.CONFIGURATION_PROPERTIES_CACHE);
		return cache;
	}

	public BigidConfigurationProperties getConfigurationProperties(Long organizationId) {
		if (organizationId == null) {
			return null;
		}
		BigidConfigurationProperties configurationProperties = getCache().get(organizationId);
		if (configurationProperties == null) {
			// Read configuration properties from the database
			Optional<OrganizationSecurityCertificates> certificateOpt = certificatesRepository
				.findFirstByOrganizationIdAndCertificateTypeAndIsActive(organizationId, ExternalAnalyticsType.BIG_ID,
					true);
			if (certificateOpt.isEmpty()) {
				return null;
			}
			OrganizationSecurityCertificates certificate = certificateOpt.get();
			configurationProperties = new BigidConfigurationProperties(certificate.getBaseUrl(),
				certificate.getCliendId(), certificate.getClientSecret());
			getCache().put(organizationId, configurationProperties);
		}
		return configurationProperties;
	}

	public String getBaseServiceUrl(Long organizationId) {
		BigidConfigurationProperties configurationProperties = getConfigurationProperties(organizationId);
		if (configurationProperties != null) {
			return configurationProperties.getBaseServiceUrl();
		}
		return null;
	}

}
