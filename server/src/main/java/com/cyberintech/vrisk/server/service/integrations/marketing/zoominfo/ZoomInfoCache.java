package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo;

import com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cache Storage Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-01
 */
@Service
@Slf4j
public class ZoomInfoCache {

	public static final String ZOOM_INFO_AUTH = "ZOOM_INFO_AUTH";
	public static final String ZOOM_INFO = "ZOOM_INFO";

	@Lazy
	@Autowired
	private ZoomInfoService zoomInfoService;

	@Cacheable(cacheNames = "AUTHORIZATIONS", key = "T(com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.ZoomInfoCache).ZOOM_INFO_AUTH", unless="#result == null")
	public String getJWTAccessToken() {
		log.info("!!!! Trying to load Zoom Info JWT token");
		String result = zoomInfoService.getAccessToken();
		log.info(String.format("!!!! Obtained JWT token: %s", result));

		return result;
	}

	@Cacheable(cacheNames = ZOOM_INFO, key = "#key")
	public OrganizationZoomInfoExtendedDetails getOrganization(Long key) {
		OrganizationEnrichItem enrichItem = zoomInfoService.enrichOrganizations(new OrganizationSearchFilter(key));
		OrganizationZoomInfoExtendedDetails result = OrganizationZoomInfoExtendedDetails.of(enrichItem);

		// Set OrgChart Items
		List<OrgChartEnrichItem> orgChartItems = zoomInfoService.enrichOrgChart(String.valueOf(key));
		result.setOrgChartItems(orgChartItems);

		// Get subsidiaries
		OrganizationHierarchyEnrichItem organizationHierarchy = zoomInfoService.enrichOrganizationHierarchy(new OrganizationSearchFilter(key));
		if (organizationHierarchy != null && organizationHierarchy.getFamilyTree() != null) {
			result.setSubsidiaries(organizationHierarchy.getFamilyTree());
		}

		return result;
	}

	@CachePut(cacheNames = ZOOM_INFO, key = "#value.id")
	public OrganizationZoomInfoExtendedDetails putOrganization(OrganizationZoomInfoExtendedDetails value) {
		return value;
	}

	@CacheEvict(cacheNames = ZOOM_INFO, key = "#key")
	public void removeOrganization(Long key) {
	}

}
