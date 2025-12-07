package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.dto.external_analytics.ExternalAnalyticsAccessDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsParameterType;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.ExternalAnalyticsRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationSecurityCertificatesRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.UserService;
import com.cyberintech.vrisk.server.util.ClientMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.PrivateKey;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vendor Status Dashboard Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-18
 */
@Service
@Slf4j
public class AnalyticDashboardsService extends DashboardServiceBase {

	public static final Long ANALYTICS_DASHBOARD_OFFSET = 1000000000L;

	private static final ObjectMapper objectMapper;
	static {
		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private ExternalAnalyticsRepository externalAnalyticsRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private OrganizationSecurityCertificatesRepository organizationSecurityCertificatesRepository;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private UserService userService;

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public void buildAnalyticDashboardItemsMenu(List<DashboardRefDTO> items) {
		Long organizationId = organizationService.getCurrentOrganizationId();
		Users user = userService.getCurrentUserEntity();
		List<Long> userRoles = user.getRoles().stream().map(Roles::getId).collect(Collectors.toList());

		// List<ExternalAnalytics> externalAnalyticsItems = externalAnalyticsRepository.findAllByOrganizationId(organizationId);
		List<ExternalAnalytics> externalAnalyticsItems = externalAnalyticsRepository.getListByRolesAndOrganizationId(userRoles, organizationId);
		long powerBITemsCount = externalAnalyticsItems.stream().filter(externalAnalyticsItem -> ExternalAnalyticsType.POWER_BI.equals(externalAnalyticsItem.getExternalAnalyticsType())).count();
		for (ExternalAnalytics externalAnalytics : externalAnalyticsItems) {
			Long analyticsId = ANALYTICS_DASHBOARD_OFFSET + externalAnalytics.getId();
			if (ExternalAnalyticsType.DASHBOARD.equals(externalAnalytics.getExternalAnalyticsType())) {
				Map<String, ExternalAnalyticsParameters> parametersMap = externalAnalytics.buildParametersMap();
				if (parametersMap.containsKey(ExternalAnalyticsParameterType.DASHBOARD_CONFIG_JSON.name())) {
					ExternalAnalyticsParameters sectionParameter = parametersMap.get(ExternalAnalyticsParameterType.DASHBOARD_SECTION_NAME.name());
					DashboardRefDTO dashboardRef = new DashboardRefDTO(analyticsId, externalAnalytics.getName(), externalAnalytics.getDescription(), DashboardType.Dynamic, "fa fa-moon-o", (sectionParameter != null ? sectionParameter.getValue() : null), null);
					items.add(dashboardRef);
				}
				continue;
			}
			DashboardType dashboardType = ExternalAnalyticsType.QLIK.equals(externalAnalytics.getExternalAnalyticsType()) ? DashboardType.Qlik : DashboardType.Analytics;
			String sectionName = ExternalAnalyticsType.POWER_BI.equals(externalAnalytics.getExternalAnalyticsType()) ? (powerBITemsCount > 1 ? "Power BI" : "") : "DASHBOARDS$EXECUTIVE_ANALYTICS";
			DashboardRefDTO dashboardRef = new DashboardRefDTO(analyticsId, externalAnalytics.getName(), externalAnalytics.getDescription(), dashboardType, "fa fa-moon-o", sectionName, null);
			items.add(dashboardRef);
		}
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO checkIsAnalyticsDashboard(Long riskModelId, Long dashboardId, DashboardStateDTO dashboardState) {
		DashboardDTO result = null;

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_EXECUTIVE_ANALYTICS(clientMessage);

		if (dashboardId > ANALYTICS_DASHBOARD_OFFSET) {
			UserDetailsImpl currentUser = userService.getCurrentUser();
			Long userId = currentUser.getUserId();
			Long organizationId = organizationService.getCurrentOrganizationId();
			Long analyticsId = dashboardId - ANALYTICS_DASHBOARD_OFFSET;
			Optional<ExternalAnalytics> externalAnalyticsOptional = externalAnalyticsRepository.findByIdAndOrganizationId(analyticsId, organizationId);

			if (externalAnalyticsOptional.isPresent()) {
				ExternalAnalytics externalAnalytics = externalAnalyticsOptional.get();
				Map<String, String> parametersMap = externalAnalytics.buildParameterValueMap();

				// Load Initial Data
				RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
				Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

				if (ExternalAnalyticsType.DASHBOARD.equals(externalAnalytics.getExternalAnalyticsType())) {

					DashboardDTO dashboard = null;
					String configJsonString = parametersMap.get(ExternalAnalyticsParameterType.DASHBOARD_CONFIG_JSON.name());
					if (StringUtils.isNotBlank(configJsonString)) {
						try {
							dashboard = objectMapper.readValue(configJsonString, DashboardDTO.class);
							dashboard.setId(externalAnalytics.getId());
							dashboard.setName(externalAnalytics.getName());
							dashboard.setDescription(externalAnalytics.getDescription());
							dashboard.setDashboardType(DashboardType.Dynamic);

						} catch (Exception e) {
							log.warn("!! Failed to read JSON for Dashboard config: {}", configJsonString, e);
						}
					}

					if (dashboard == null) {
						dashboard = new DashboardDTO(dashboardId, externalAnalytics.getName(), externalAnalytics.getDescription(), DashboardType.Dynamic);
						// Create Initial Sections
						DashboardSectionDTO section = new DashboardSectionDTO(dashboardId, externalAnalytics.getName(), externalAnalytics.getDescription());
						dashboard.getSections().add(section);
					}

					result = dashboard;
				} else {
					DashboardDTO dashboard = new DashboardDTO(dashboardId, externalAnalytics.getName(), externalAnalytics.getDescription(), DashboardType.Analytics);
					result = dashboard;

					// Create Initial Sections
					// DashboardSectionDTO section = new DashboardSectionDTO(35200L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$ITEM_DESCRIPTION));
					DashboardSectionDTO section = new DashboardSectionDTO(dashboardId, externalAnalytics.getName(), externalAnalytics.getDescription());
					dashboard.getSections().add(section);

					// Create breadcrumbs
					section.setBreadcrumbs(breadcrumbsTop.extend(externalAnalytics.getName(), externalAnalytics.getName(), "").getBreadcrumbs());

					// Add download button
					// DashboardItemDTO downloadButton = buildDownloadButtonDashboardItemDTO(riskModelId, DashboardsConfig.DASHBOARD_VENDOR_STATUS_REPORT, 45701L);
					// section.getDashboardItems().add(downloadButton);

					String sessionId = DigestUtils.md5Hex(currentUser.getUsername() + "_" + applicationProperties.getUiHostname());
					String frameSource = "";
					if (parametersMap.containsKey(ExternalAnalyticsParameterType.EMBED_URL.name())) {
						frameSource = parametersMap.get(ExternalAnalyticsParameterType.EMBED_URL.name());
					} else {
						frameSource = MessageFormat.format(
							"https://{0}/single/?appid={1}&obj={2}&opt=ctxmenu,currsel"
							, parametersMap.get(ExternalAnalyticsParameterType.TENANT_DOMAIN.name())
							, parametersMap.get(ExternalAnalyticsParameterType.APPLICATION_ID.name())
							, parametersMap.get(ExternalAnalyticsParameterType.OBJECT_ID.name())

														  );
						section.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARDS$BOARD_ANALYTICS", "DASHBOARDS$BOARD_ANALYTICS", "").getBreadcrumbs());
					}
					frameSource += "&identity=" + sessionId;

					DashboardIFrameItemDTO iFrame = new DashboardIFrameItemDTO(dashboardId, externalAnalytics.getName(), externalAnalytics.getDescription());
					String heightString = parametersMap.containsKey(ExternalAnalyticsParameterType.HEIGHT.name()) ? parametersMap.get(ExternalAnalyticsParameterType.HEIGHT.name()) : "100%";
					String widthString = parametersMap.containsKey(ExternalAnalyticsParameterType.WIDTH.name()) ? parametersMap.get(ExternalAnalyticsParameterType.WIDTH.name()) : "100%";
					iFrame.setHeight(heightString);
					iFrame.setWidth(widthString);
					iFrame.setSrc(frameSource);
					iFrame.setAnalyticsType(externalAnalytics.getExternalAnalyticsType());
					section.getDashboardItems().add(iFrame);

					if (ExternalAnalyticsType.QLIK.equals(externalAnalytics.getExternalAnalyticsType())) {
						// URL: https://${config.tenantDomain}/login/jwt-session?qlik-web-integration-id=${config.webIntegrationId}
						String preloadUrl = MessageFormat.format(
							"https://cit.us.qlikcloud.com/login/jwt-session?qlik-web-integration-id={0}"
							, parametersMap.get(ExternalAnalyticsParameterType.WEB_INTEGRATION_ID.name())
																);
						String qLikToken = getQLikJWTToken(userId, organizationId, parametersMap);

						PreloadUrlCallDTO preloadCall = new PreloadUrlCallDTO();
						preloadCall.setMethod(HttpMethod.POST);
						preloadCall.setUrl(preloadUrl);
						Map<String, String> headers = new HashMap<>();
						headers.put("content-type", "application/json");
						headers.put("Authorization", "Bearer " + qLikToken);
						headers.put("qlik-web-integration-id", parametersMap.get(ExternalAnalyticsParameterType.WEB_INTEGRATION_ID.name()));
						preloadCall.setHeaders(headers);
						iFrame.getPreloadCalls().add(preloadCall);

						ExternalAnalyticsAccessDTO accessDetails = new ExternalAnalyticsAccessDTO();
						accessDetails.setToken(qLikToken);
						accessDetails.setTokenType("QLIK");
						accessDetails.setSessionId(sessionId);
						accessDetails.setTenant(parametersMap.get(ExternalAnalyticsParameterType.TENANT_DOMAIN.name()));
						accessDetails.setWebIntegrationId(parametersMap.get(ExternalAnalyticsParameterType.WEB_INTEGRATION_ID.name()));
						accessDetails.setApplicationId(parametersMap.get(ExternalAnalyticsParameterType.APPLICATION_ID.name()));

						iFrame.setAccessDetails(accessDetails);
					} else if (ExternalAnalyticsType.POWER_BI.equals(externalAnalytics.getExternalAnalyticsType())) {
						ExternalAnalyticsAccessDTO accessDetails = new ExternalAnalyticsAccessDTO();
						accessDetails.setTokenType("POWER_BI");
						// accessDetails.setTenant(parametersMap.get(ExternalAnalyticsQlikParameters.P.name()));
						accessDetails.setApplicationId(parametersMap.get(ExternalAnalyticsParameterType.POWERBI_CLIENT_ID.name()));
						accessDetails.setWorkspaceId(parametersMap.get(ExternalAnalyticsParameterType.POWERBI_WORKSPACE_ID.name()));
						accessDetails.setReportId(parametersMap.get(ExternalAnalyticsParameterType.POWERBI_REPORT_ID.name()));
						accessDetails.setPageId(parametersMap.get(ExternalAnalyticsParameterType.POWERBI_PAGE_NAME.name()));

						iFrame.setAccessDetails(accessDetails);
					}
				}
			}

		}
		return result;
	}

	/**
	 * Obtain access details for External Analytics
	 *
	 * @param analyticsType
	 * @return
	 */
	/*
	public ExternalAnalyticsAccessDTO getAccessDetails(ExternalAnalyticsType analyticsType) {
		ExternalAnalyticsAccessDTO result = null;
		Long userId = userService.getCurrentUser().getUserId();
		Optional<OrganizationSecurityCertificates> accessDetailsOptional = organizationSecurityCertificatesRepository.findFirstByUserIdAndCertificateTypeAndIsActive(userId, analyticsType, true);
		if (accessDetailsOptional.isEmpty()) {
			Long organizationId = organizationService.getCurrentOrganizationId();
			accessDetailsOptional = organizationSecurityCertificatesRepository.findFirstByOrganizationIdAndCertificateTypeAndIsActive(organizationId, analyticsType, true);
		}

		if (accessDetailsOptional.isPresent()) {
			OrganizationSecurityCertificates accessDetails = accessDetailsOptional.get();
			result = new ExternalAnalyticsAccessDTO();
			result.setToken(accessDetails);
		}

		return result;
	}
	*/

	/**
	 * Returns Private Key for qLik
	 *
	 * @param userId
	 * @param organizationId
	 * @return
	 */
	public String getQLikPrivateKey(Long userId, Long organizationId) {
		String keyString = getDefaultQLikPrivateKey();

		// Proceed Private Key
		Optional<OrganizationSecurityCertificates> qlikPrivateKey = organizationSecurityCertificatesRepository.findFirstByUserIdAndCertificateTypeAndIsActive(userId, ExternalAnalyticsType.QLIK, true);
		if (qlikPrivateKey.isPresent()) {
			keyString = qlikPrivateKey.get().getPrivateKey();
		}

		// Proceed Private  for organization
		if (qlikPrivateKey.isEmpty()) {
			qlikPrivateKey = organizationSecurityCertificatesRepository.findFirstByOrganizationIdAndCertificateTypeAndIsActive(organizationId, ExternalAnalyticsType.QLIK, true);
			if (qlikPrivateKey.isPresent()) {
				keyString = qlikPrivateKey.get().getPrivateKey();
			}
		}

		return keyString;
	}

	/**
	 * Returns Private Key for qLik
	 *
	 * @todo Apply logic to read the key from the file
	 */
	public String getDefaultQLikPrivateKey() {
		String keyString = "";

		return keyString;
	}

	public String getQLikJWTToken(Long userId, Long organizationId, Map<String, String> parametersMap) {
		String token = "";

		String privateKeyString = getQLikPrivateKey(userId, organizationId);

		try {
			// final int expiration = 1000 * 3600 * 24 * 3;
			// final int expiration = 1000 * 3 * 3600;
			final int expiration = 3599000;

			final Reader pemReader = new StringReader(privateKeyString);
			final PEMParser pemParser = new PEMParser(pemReader);
			final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

			Object pemObject = pemParser.readObject();
			PrivateKey privateKey;
			if (pemObject instanceof PrivateKeyInfo) {
				final PrivateKeyInfo object = (PrivateKeyInfo) pemObject;
				privateKey = converter.getPrivateKey(object);
			} else if (pemObject instanceof PEMKeyPair) {
				final PEMKeyPair pemKeyPair = (PEMKeyPair) pemObject;
				final PrivateKeyInfo object = pemKeyPair.getPrivateKeyInfo();
				privateKey = converter.getPrivateKey(object);
			} else {
				throw new InternalServerErrorException("Certificate type is not supported: " + pemObject.getClass().getSimpleName());
			}

			String jwtRequestId = UUID.randomUUID().toString();

			String groupsString = parametersMap.get(ExternalAnalyticsParameterType.USER_GROUPS.name());
			Claims claims = Jwts.claims();
			claims.setSubject(parametersMap.get(ExternalAnalyticsParameterType.USER_EMAIL.name())); // app developer
			claims.put("subType", "user");
			claims.put("name", parametersMap.get(ExternalAnalyticsParameterType.USER_NAME.name()));
			claims.put("email", parametersMap.get(ExternalAnalyticsParameterType.USER_EMAIL.name()));
			claims.put("email_verified", true);
			claims.put("groups", Arrays.asList("Developer")); // TODO apply groups
			claims.setId(jwtRequestId);
			claims.setNotBefore(new Date(System.currentTimeMillis()));
			claims.setIssuedAt(new Date(System.currentTimeMillis()));
			claims.setExpiration(new Date(System.currentTimeMillis() + expiration));
			claims.setAudience("qlik.api/login/jwt-session");
			claims.setIssuer(parametersMap.get(ExternalAnalyticsParameterType.ISSUER.name())); // issuer: "cit.us.qlikcloud.com",

			token = Jwts.builder()
						.signWith(SignatureAlgorithm.RS256, privateKey) // ECDSA using P-256 and SHA-256
						.setHeaderParam("typ", "JWT") //
						.setHeaderParam(JwsHeader.KEY_ID, parametersMap.get(ExternalAnalyticsParameterType.API_KEY_ID.name())) //
						.setClaims(claims)
						.compact();

		} catch (PEMException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Certificate is invalid. Please contact support.");
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to create JWT token. Please contact support.");
		}

		return token;
	}


}
