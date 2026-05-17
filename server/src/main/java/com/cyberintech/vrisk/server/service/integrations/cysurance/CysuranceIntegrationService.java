package com.cyberintech.vrisk.server.service.integrations.cysurance;

import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.rest.exception.ValidationException;
import com.cyberintech.vrisk.server.service.integrations.cysurance.dto.CysuranceQueryRequest;
import com.cyberintech.vrisk.server.service.integrations.cysurance.dto.CysuranceQueryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

/**
 * Cysurance integration Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2026-05-17
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CysuranceIntegrationService {


	private final RestTemplate restTemplate;

	@Value("${intgration.cysurance.api-url:https://staging.cysurance.com}")
	private String cysuranceAPIUrl;


	public OrganizationRefDTO runCysuranceIntegration(Organizations organization) {

		Map<String, String> integrationProperties = organization.getIntegrationProperties();

		String cysuranceApiKey = integrationProperties.get("cysuranceApiKey");
		String cysurancePartnerCode = integrationProperties.get("cysurancePartnerCode");
		String cysuranceEntityIdentifier = integrationProperties.get("cysuranceEntityIdentifier");

		if (StringUtils.isEmpty(cysuranceApiKey)) {
			throw new ValidationException(String.format("CYSURANCE Integration Error. API Key is not defined for Organization: [%s, %s]", organization.getId(), organization.getName()));
		}
		if (StringUtils.isEmpty(cysurancePartnerCode)) {
			throw new ValidationException(String.format("CYSURANCE Integration Error. Partner Code is not defined for Organization: [%s, %s]", organization.getId(), organization.getName()));
		}
		if (StringUtils.isEmpty(cysuranceEntityIdentifier)) {
			throw new ValidationException(String.format("CYSURANCE Integration Error. Entity Identifier is not defined for Organization: [%s, %s]", organization.getId(), organization.getName()));
		}

		// Create Object Mapper to convert String into Object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// Create request header
		HttpHeaders headers = new HttpHeaders();
		headers.put("Content-Type", Arrays.asList("application/json"));
		headers.put("Authorization", Arrays.asList(cysuranceApiKey));

		try {

			// Request body
			CysuranceQueryRequest requestObject = new CysuranceQueryRequest();
			requestObject.setRiskEntityIdentifier(cysuranceEntityIdentifier);
			requestObject.setReportingPartnerCode(cysurancePartnerCode);
			requestObject.setRiskEntityType("domain");

			String requestBodyString = mapper.writeValueAsString(requestObject);


			// Add (body, header) to HTTP entity
			HttpEntity<String> httpEntity = new HttpEntity<> (requestBodyString, headers);

			// Call the API
			String url = cysuranceAPIUrl + "/wp-json/enrollment/v1/risk-ratings/query";
			ResponseEntity<CysuranceQueryResponse> response = restTemplate.postForEntity(url, httpEntity, CysuranceQueryResponse.class);
			HttpHeaders responseHeader = response.getHeaders();
			CysuranceQueryResponse responseBody = response.getBody();

			log.info("HERE");

		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}


		OrganizationRefDTO result = new OrganizationRefDTO(organization);

		return result;
	}

}
