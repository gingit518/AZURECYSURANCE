package com.cyberintech.vrisk.server.service.dashboards.powerbi;

import com.cyberintech.vrisk.server.context.ApplicationContextThreadLocal;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.model.EmbedConfig;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.model.EmbedConfigRequest;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.model.EmbedToken;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.model.ReportConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Service with helper methods to get report's details and multi-resource embed token
 */
@Service
@Slf4j
public class PowerBIService {

	@Autowired
	private AzureADService azureADService;

	@Autowired
	private PowerBIConfig powerBIConfig;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Get embed params for a report for a single workspace
	 *
	 * @param {string} accessToken
	 * @param {string} workspaceId
	 * @param {string} reportId
	 * @param {string} additionalDatasetIds
	 * @return EmbedConfig object
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public EmbedConfig getEmbedConfig(String accessToken, String workspaceId, String reportId, String... additionalDatasetIds) throws JsonMappingException, JsonProcessingException {
		if (reportId == null || reportId.isEmpty()) {
			throw new RuntimeException("Empty Report Id");
		}
		if (workspaceId == null || workspaceId.isEmpty()) {
			throw new RuntimeException("Empty Workspace Id");
		}

		ReportConfig embedReport = getPowerBIReportConfig(accessToken, workspaceId, reportId);

		// Add embed config to client response object

		// Create embedding configuration object
		EmbedConfig reportEmbedConfig = new EmbedConfig();
		reportEmbedConfig.setEmbedReports(new ArrayList<ReportConfig>());
		reportEmbedConfig.getEmbedReports().add(embedReport);

		// Create a list of DatasetIds
		List<String> datasetIds = new ArrayList<String>();
		datasetIds.add(embedReport.getDatasetId());

		// Append to existing list of datasetIds to achieve dynamic binding later
		datasetIds.addAll(Arrays.asList(additionalDatasetIds));

		// Get embed token
		EmbedToken embedToken = getEmbedToken(accessToken, reportId, datasetIds);
		reportEmbedConfig.setEmbedToken(embedToken);

		return reportEmbedConfig;
	}

	/**
	 * Get embed params for multiple reports for a single workspace
	 * @param {string} accessToken
	 * @param {string} workspaceId
	 * @param {List<string>} reportIds
	 * @return EmbedConfig object
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public EmbedConfig getEmbedConfig(String accessToken, String workspaceId, List<String> reportIds) throws JsonMappingException, JsonProcessingException {

		// Note: This method is an example and is not consumed in this sample app

		if (reportIds == null || reportIds.isEmpty()) {
			throw new RuntimeException("Empty Report Ids");
		}
		if (workspaceId == null || workspaceId.isEmpty()) {
			throw new RuntimeException("Empty Workspace Id");
		}

		// Create embedding configuration object
		EmbedConfig reportEmbedConfig = new EmbedConfig();
		reportEmbedConfig.setEmbedReports(new ArrayList<ReportConfig>());

		// Create a list of DatasetIds
		List<String> datasetIds = new ArrayList<String>();

		for (String reportId : reportIds) {
			ReportConfig embedReport = getPowerBIReportConfig(accessToken, workspaceId, reportId);

			// Add embed config to client response object
			reportEmbedConfig.getEmbedReports().add(embedReport);

			// Add datasetId in the datasetIds
			datasetIds.add(embedReport.getDatasetId());
		}

		// Get embed token
		EmbedToken embedToken = getEmbedToken(accessToken, reportIds, datasetIds);
		reportEmbedConfig.setEmbedToken(embedToken);

		return reportEmbedConfig;
	}

	/**
	 * Get embed params for multiple reports for a single workspace
	 *
	 * @param {string} accessToken
	 * @param {string} workspaceId
	 * @param {List<string>} reportIds
	 * @param {List<string>} additionalDatasetIds
	 * @return EmbedConfig object
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public EmbedConfig getEmbedConfigForReports(String accessToken, String workspaceId, List<String> reportIds, List<String> additionalDatasetIds) throws JsonMappingException, JsonProcessingException {

		// Note: This method is an example and is not consumed in this sample app

		if (reportIds == null || reportIds.isEmpty()) {
			throw new RuntimeException("Empty Report Ids");
		}
		if (workspaceId == null || workspaceId.isEmpty()) {
			throw new RuntimeException("Empty Workspace Id");
		}

		// Create embedding configuration object
		EmbedConfig reportEmbedConfig = new EmbedConfig();
		reportEmbedConfig.setEmbedReports(new ArrayList<ReportConfig>());

		for (String reportId : reportIds) {

			ReportConfig embedReport = getPowerBIReportConfig(accessToken, workspaceId, reportId);

			// Add embed config to client response object
			reportEmbedConfig.getEmbedReports().add(embedReport);

			// Create a list of DatasetIds if it is null
			if (additionalDatasetIds == null) {
				additionalDatasetIds = new ArrayList<String>();
			}

			// Add datasetId in the datasetIds
			additionalDatasetIds.add(embedReport.getDatasetId());
		}

		// Get embed token
		EmbedToken embedToken = getEmbedToken(accessToken, reportIds, additionalDatasetIds);
		reportEmbedConfig.setEmbedToken(embedToken);

		return reportEmbedConfig;
	}


	/**
	 * Refresh PowerBI Report Data in Async Mode
	 *
	 * @param {string} clientId
	 * @param {string} workspaceId
	 * @param {string} reportId
	 * @return Boolean
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@Async
	public Boolean refreshPowerBIReportDataset(String clientId, String workspaceId, String reportId) throws JsonProcessingException, MalformedURLException, ExecutionException, InterruptedException {
		if (reportId == null || reportId.isEmpty()) {
			throw new RuntimeException("Empty Report Id");
		}
		if (workspaceId == null || workspaceId.isEmpty()) {
			throw new RuntimeException("Empty Workspace Id");
		}

		String accessToken = azureADService.getAccessToken(clientId);
		log.debug(String.format("[POWER_BI] Client %s access token: %s", clientId, accessToken));

		ReportConfig embedReport = getPowerBIReportConfig(accessToken, workspaceId, reportId);

		Boolean result = false;

		// REST API URL to Refresh report Dataset
		String endPointUrl = String.format("https://api.powerbi.com/v1.0/myorg/groups/%s/datasets/%s/refreshes", workspaceId, embedReport.getDatasetId());

		// Request header
		HttpHeaders reqHeader = new HttpHeaders();
		reqHeader.put("Content-Type", Arrays.asList("application/json"));
		reqHeader.put("Authorization", Arrays.asList("Bearer " + accessToken));

		// HTTP entity object - holds header and body
		HttpEntity<String> reqEntity = new HttpEntity<> (reqHeader);

		// Rest API get report's details
		ResponseEntity<String> response = postForEntity("https://api.powerbi.com/v1.0/myorg/groups/" + workspaceId + "/datasets/" + embedReport.getDatasetId() + "/refreshes", reqEntity, String.class);

		HttpHeaders responseHeader = response.getHeaders();
		String responseBody = response.getBody();

		if (response.getStatusCodeValue() == 200 || response.getStatusCodeValue() == 202) {
			result = true;
			log.warn(String.format("[POWER_BI] Successfully Refreshed Datasource %s for Workspace %s", embedReport.getDatasetId(), workspaceId));
		} else {
			log.warn(String.format("[POWER_BI] Failed to refresh Datasource %s for Workspace %s", embedReport.getDatasetId(), workspaceId));
		}

		return result;
	}

	/**
	 * Get Report Confiouration for Power BI report
	 *
	 * @param accessToken
	 * @param workspaceId
	 * @param reportId
	 * @return
	 * @throws JsonProcessingException
	 */
	public ReportConfig getPowerBIReportConfig(String accessToken, String workspaceId, String reportId) throws JsonProcessingException {
		// Get Report In Group API: https://api.powerbi.com/v1.0/myorg/groups/{workspaceId}/reports/{reportId}
		StringBuilder urlStringBuilder = new StringBuilder("https://api.powerbi.com/v1.0/myorg/groups/");
		urlStringBuilder.append(workspaceId);
		urlStringBuilder.append("/reports/");
		urlStringBuilder.append(reportId);

		// Request header
		HttpHeaders reqHeader = new HttpHeaders();
		reqHeader.put("Content-Type", Arrays.asList("application/json"));
		reqHeader.put("Authorization", Arrays.asList("Bearer " + accessToken));

		// HTTP entity object - holds header and body
		HttpEntity<String> reqEntity = new HttpEntity<> (reqHeader);

		// REST API URL to get report details
		String endPointUrl = urlStringBuilder.toString();

		// Rest API get report's details
		ResponseEntity<String> response = getForEntity("https://api.powerbi.com/v1.0/myorg/groups/" + workspaceId + "/reports/" + reportId, reqEntity, String.class);

		HttpHeaders responseHeader = response.getHeaders();
		String responseBody = response.getBody();

		// Create Object Mapper to convert String into Object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// Get the request Id
		List<String> reqIdList = responseHeader.get("RequestId");
		log.info("Retrieved report details");
		if (reqIdList != null && !reqIdList.isEmpty()) {
			for (String reqId: reqIdList) {
				log.info("Request Id: {}", reqId);
			}
		}

		// Convert responseBody string into ReportConfig class object
		ReportConfig embedReport = mapper.readValue(responseBody, ReportConfig.class);
		return embedReport;
	}

	/**
	 * Get Embed token for single report, multiple datasetIds, and optional target workspaces
	 * @see <a href="https://aka.ms/MultiResourceEmbedToken">Multi-Resource Embed Token</a>
	 * @param {string} accessToken
	 * @param {string} reportId
	 * @param {List<string>} datasetId
	 * @param {string} targetWorkspaceIds
	 * @return EmbedToken
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public EmbedToken getEmbedToken(String accessToken, String reportId, List<String> datasetIds, String... targetWorkspaceIds) throws JsonMappingException, JsonProcessingException {

		// Embed Token - Generate Token REST API
		final String uri = "https://api.powerbi.com/v1.0/myorg/GenerateToken";

		RestTemplate restTemplate = new RestTemplate();

		// Create request header
		HttpHeaders headers = new HttpHeaders();
		headers.put("Content-Type", Arrays.asList("application/json"));
		headers.put("Authorization", Arrays.asList("Bearer " + accessToken));

		EmbedConfigRequest requestObject = new EmbedConfigRequest();

		// Add dataset id in body
		for (String datasetId : datasetIds) {
			requestObject.getDatasets().add(new EmbedConfigRequest.IdObject(datasetId));
		}

		// Add report id in body
		requestObject.getReports().add(new EmbedConfigRequest.IdObject(reportId));

		// Add target workspace id in body
		for (String targetWorkspaceId: targetWorkspaceIds) {
			requestObject.getTargetWorkspaces().add(new EmbedConfigRequest.IdObject(targetWorkspaceId));
		}

		// Create Object Mapper to convert String into Object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// Request body
		String requestBodyString = mapper.writeValueAsString(requestObject);

		// Add (body, header) to HTTP entity
		HttpEntity<String> httpEntity = new HttpEntity<> (requestBodyString, headers);

		// Call the API
		ResponseEntity<String> response = postForEntity(uri, httpEntity, String.class);
		HttpHeaders responseHeader = response.getHeaders();
		String responseBody = response.getBody();

		// Convert responseBody string into EmbedToken class object
		EmbedToken embedToken = mapper.readValue(responseBody, EmbedToken.class);

		return embedToken;
	}

	/**
	 * Get Embed token for multiple reports, multiple datasetIds, and optional target workspaces
	 * @see <a href="https://aka.ms/MultiResourceEmbedToken">Multi-Resource Embed Token</a>
	 * @param {string} accessToken
	 * @param {List<string>} reportIds
	 * @param {List<string>} datasetIds
	 * @param {string} targetWorkspaceIds
	 * @return EmbedToken
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public static EmbedToken getEmbedToken(String accessToken, List<String> reportIds, List<String> datasetIds, String... targetWorkspaceIds) throws JsonMappingException, JsonProcessingException {

		// Note: This method is an example and is not consumed in this sample app

		// Embed Token - Generate Token REST API
		final String uri = "https://api.powerbi.com/v1.0/myorg/GenerateToken";

		RestTemplate restTemplate = new RestTemplate();

		// Create request header
		HttpHeaders headers = new HttpHeaders();
		headers.put("Content-Type", Arrays.asList("application/json"));
		headers.put("Authorization", Arrays.asList("Bearer " + accessToken));


		EmbedConfigRequest requestObject = new EmbedConfigRequest();

		// Add dataset id in body
		for (String datasetId : datasetIds) {
			requestObject.getDatasets().add(new EmbedConfigRequest.IdObject(datasetId));
		}

		// Add report id in body
		for (String reportId : reportIds) {
			requestObject.getReports().add(new EmbedConfigRequest.IdObject(reportId));
		}

		// Add target workspace id in body
		for (String targetWorkspaceId: targetWorkspaceIds) {
			requestObject.getTargetWorkspaces().add(new EmbedConfigRequest.IdObject(targetWorkspaceId));
		}

		// Create Object Mapper to convert String into Object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// Request body
		String requestBodyString = mapper.writeValueAsString(requestObject);

		// Add (body, header) to HTTP entity
		HttpEntity<String> httpEntity = new HttpEntity<> (requestBodyString, headers);

		// Call the API
		ResponseEntity<String> response = restTemplate.postForEntity(uri, httpEntity, String.class);
		HttpHeaders responseHeader = response.getHeaders();
		String responseBody = response.getBody();

		// Convert responseBody string into EmbedToken class object
		EmbedToken embedToken = mapper.readValue(responseBody, EmbedToken.class);

		return embedToken;
	}

	public <ENTITY> ResponseEntity<ENTITY> postForEntity(String url, @Nullable HttpEntity<String> requestEntity, Class<ENTITY> responseClass) {
		ResponseEntity<ENTITY> responseEntity = restTemplate.postForEntity(url, requestEntity, responseClass);

		return responseEntity;
	}

	public <ENTITY> ResponseEntity<ENTITY> getForEntity(String url, @Nullable HttpEntity<String> requestEntity, Class<ENTITY> responseClass) {
		ResponseEntity<ENTITY> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseClass);

		return responseEntity;
	}
}
