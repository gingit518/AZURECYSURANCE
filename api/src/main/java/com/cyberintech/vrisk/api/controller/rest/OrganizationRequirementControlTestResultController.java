package com.cyberintech.vrisk.api.controller.rest;


import com.cyberintech.vrisk.server.model.data.ControlTestResultFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.assessments.OrganizationRequirementControlTestResultDTO;
import com.cyberintech.vrisk.server.service.OrganizationRequirementControlTestResultService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

/**
 * Organization Requirement Control Test Result management controller. Basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.01.31
 */
@RestController
@RequestMapping(
	value = OrganizationRequirementControlTestResultController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Organization Requirement Control Test Result Management Controller"
)
@Tag(name = "Organization Requirement Control Test Result Management")
public class OrganizationRequirementControlTestResultController {

	static final String CONTROLLER_URI = "/api/organization-requirement-control-test-results";

	@Autowired
	private OrganizationRequirementControlTestResultService organizationRequirementControlTestResultService;

	/**
	 * Get Organization Requirement Control Test Result list for current Organization
	 *
	 * @return Organization Requirement Control Test Result List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Organization Requirement Control Test Result list for current Filters and Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<ControlTestResultFilter, OrganizationRequirementControlTestResultDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ControlTestResultFilter> filteredRequest
	) {

		FilteredResponse<ControlTestResultFilter, OrganizationRequirementControlTestResultDTO> itemDTOs = organizationRequirementControlTestResultService.getListFiltered(filteredRequest);

		return itemDTOs;
	}

//	/**
//	 * Get Organization Requirement Control Test Result details
//	 *
//	 * @return Organization Requirement Control Test Result Details
//	 */
//	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Organization Requirement Control Test Result details")
//	@Parameters({
//		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
//	})
//	public OrganizationRequirementControlTestResultDTO getDetails(
//		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
//	) {
//
//		OrganizationRequirementControlTestResultDTO itemDTO = organizationRequirementControlTestResultService.getDetails(itemId);
//
//		return itemDTO;
//	}

	/**
	 * Get or Create if Not Exist
	 *
	 * @return Organization Requirement Control Test Result Details
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/alt-get", name = "Get or Create if Not Exist Organization Requirement Control Test Result details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationRequirementControlTestResultDTO getOrCreateIfNotExist(
		@Parameter(description = "Organization Requirement Control Test Result Details", required = true) @RequestBody OrganizationRequirementControlTestResultDTO itemDTO
	) {

		OrganizationRequirementControlTestResultDTO result = organizationRequirementControlTestResultService.getOrCreateIfNotExist(itemDTO);

		return result;
	}


	/**
	 * Download CSV date
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/download/csv", name = "Get Assessments Audit Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ORGANIZATION_CONTROL_TEST_RESULT_DOWNLOAD)")
	public void downloadCSVData(HttpServletResponse response) throws IOException {
		// Build Download Template
		// ByteArrayInputStream byteArrayInputStream = businessUnitService.getDownloadData();

		// Build HTTP Response
		// Response.ResponseBuilder response = Response.ok(byteArrayInputStream.readAllBytes());
		byte[] fileBytes = "OK".getBytes();
		// Systems system = systemsService.getSystemForCurrentOrganization(itemId);
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"AssessmentsAudit.{0}.csv\"", "ORG"));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

//	/**
//	 * Create new Organization Requirement Control Test Result
//	 *
//	 * @return New Organization Requirement Control Test Result
//	 */
//	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Organization Requirement Control Test Result")
//	@Parameters({
//		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
//	})
//	public OrganizationRequirementControlTestResultDTO create(
//		@Parameter(description = "Organization Requirement Control Test Result Details", required = true) @RequestBody OrganizationRequirementControlTestResultDTO newItemDTO
//	) {
//
//		OrganizationRequirementControlTestResultDTO result = organizationRequirementControlTestResultService.create(newItemDTO);
//
//		return result;
//	}

	/**
	 * Update Organization Requirement Control Test Result
	 *
	 * @return Updated Organization Requirement Control Test Result
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Organization Requirement Control Test Result")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationRequirementControlTestResultDTO update(
		@Parameter(description = "Organization Requirement Control Test Result Details", required = true) @RequestBody OrganizationRequirementControlTestResultDTO itemDTO
	) {

		OrganizationRequirementControlTestResultDTO result = organizationRequirementControlTestResultService.update(itemDTO);

		return result;
	}

//	/**
//	 * Deletes Organization Requirement Control Test Result
//	 *
//	 * @return ID of removed item
//	 */
//	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Organization Requirement Control Test Result")
//	@Parameters({
//		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
//	})
//	public Long delete(
//		@Parameter(description = "Simple Organization Requirement Control Test Result Details", required = true) @RequestBody ItemViewDTO itemDTO
//	) {
//
//		Long result = organizationRequirementControlTestResultService.delete(itemDTO.getId());
//
//		return result;
//	}
}
