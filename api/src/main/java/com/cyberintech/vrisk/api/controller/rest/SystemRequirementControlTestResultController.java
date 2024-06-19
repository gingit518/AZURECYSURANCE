package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ControlTestResultFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityAuditCommentDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.SystemRequirementControlTestResultDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.service.SystemRequirementControlTestResultService;
import com.cyberintech.vrisk.server.service.SystemsService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;

/**
 * System Requirement Control Test Result management controller. Basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.02.02
 */
@RestController
@RequestMapping(
	value = SystemRequirementControlTestResultController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "System Requirement Control Test Result Management Controller"
)
@Tag(name = "System Requirement Control Test Result Management")
public class SystemRequirementControlTestResultController {

	static final String CONTROLLER_URI = "/api/system-requirement-control-test-results";

	@Autowired
	private SystemRequirementControlTestResultService systemRequirementControlTestResultService;

	@Autowired
	private SystemsService systemsService;

	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "System Requirement Control Test Result list for current Filters and Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_CONTROL_TEST_RESULT_DRILLDOWN)")
	public FilteredResponse<ControlTestResultFilter, SystemRequirementControlTestResultDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ControlTestResultFilter> filteredRequest
	) {

		FilteredResponse<ControlTestResultFilter, SystemRequirementControlTestResultDTO> itemDTOs = systemRequirementControlTestResultService.getListFiltered(filteredRequest);

		return itemDTOs;
	}

//	/**
//	 * Get System Requirement Control Test Result details
//	 *
//	 * @return System Requirement Control Test Result Details
//	 */
//	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get System Requirement Control Test Result details")
//	@Parameters({
//		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
//	})
//	public SystemRequirementControlTestResultDTO getDetails(
//		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
//	) {
//
//		SystemRequirementControlTestResultDTO itemDTO = systemRequirementControlTestResultService.getDetails(itemId);
//
//		return itemDTO;
//	}

	/**
	 * Get or Create if Not Exist
	 *
	 * @return System Requirement Control Test Result Details
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/alt-get", name = "Get or Create if Not Exist System Requirement Control Test Result details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public SystemRequirementControlTestResultDTO getOrCreateIfNotExist(
		@Parameter(description = "System Requirement Control Test Result Details", required = true) @RequestBody SystemRequirementControlTestResultDTO itemDTO
	) {

		SystemRequirementControlTestResultDTO result = systemRequirementControlTestResultService.getOrCreateIfNotExist(itemDTO);

		return result;
	}

	/**
	 * Create new Audit Comment
	 *
	 * @return Security Audit Comment
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/{itemId}/audit/comments", name = "Create new Audit Comment")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public SecurityAuditCommentDTO addComment(
		@Parameter(description = "Security Audit Comment Details", required = true) @RequestBody SecurityAuditCommentDTO itemDTO,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		SecurityAuditCommentDTO result = systemRequirementControlTestResultService.createAuditComment(itemDTO, itemId);

		return result;
	}

	/**
	 * Get list of Audit Comments for System Requirement Control Test Result
	 *
	 * @return List of Security Audit Comment
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}/audit/comments", name = "Create new Audit Comment")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<SecurityAuditCommentDTO> getAuditComments(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {
		List<SecurityAuditCommentDTO> result = systemRequirementControlTestResultService.getAuditComments(itemId);

		return result;
	}

	/**
	 * Set evidence eligibility System Requirement Control Test Result
	 *
	 * @return List of Security Audit Comment
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}/set-evidence-eligible/{status}", name = "Set evidence eligibility System Requirement Control Test Result")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Boolean setEvidenceEligibility(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId,
		@PathVariable("status") @NotNull Boolean status
	) {
		systemRequirementControlTestResultService.setEvidenceEligible(itemId, status);

		return status;
	}

	/**
	 * Download CSV data
	 *
	 * @param response
	 * @param systemId
	 * @param assessmentId
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/download/csv/{systemId}", name = "Get Assessments Audit Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_CONTROL_TEST_RESULT_DOWNLOAD)")
	public void downloadCSVData(
		HttpServletResponse response,
		@PathVariable("systemId") @NotNull @Size(min = 1) Long systemId,
		@RequestParam(name = "assessmentId", required = false) @Size(min = 0) Long assessmentId
	) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = systemRequirementControlTestResultService.buildCsvContent(systemId, assessmentId);

		// Build HTTP Response
		// Response.ResponseBuilder response = Response.ok(byteArrayInputStream.readAllBytes());
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		Systems system = systemsService.getSystemForCurrentOrganization(systemId);
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"AssessmentsAudit.{0}.csv\"", system.getName()));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

//	/**
//	 * Create new System Requirement Control Test Result
//	 *
//	 * @return New System Requirement Control Test Result
//	 */
//	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new System Requirement Control Test Result")
//	@Parameters({
//		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
//	})
//	public SystemRequirementControlTestResultDTO create(
//		@Parameter(description = "System Requirement Control Test Result Details", required = true) @RequestBody SystemRequirementControlTestResultDTO newItemDTO
//	) {
//
//		SystemRequirementControlTestResultDTO result = systemRequirementControlTestResultService.create(newItemDTO);
//
//		return result;
//	}

	/**
	 * Update System Requirement Control Test Result
	 *
	 * @return Updated System Requirement Control Test Result
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing System Requirement Control Test Result")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_CONTROL_TEST_RESULT_UPDATE)")
	public SystemRequirementControlTestResultDTO update(
		@Parameter(description = "System Requirement Control Test Result Details", required = true) @RequestBody SystemRequirementControlTestResultDTO itemDTO
	) {

		SystemRequirementControlTestResultDTO result = systemRequirementControlTestResultService.update(itemDTO);

		return result;
	}

//	/**
//	 * Deletes System Requirement Control Test Result
//	 *
//	 * @return ID of removed item
//	 */
//	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing System Requirement Control Test Result")
//	@Parameters({
//		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
//	})
//	public Long delete(
//		@Parameter(description = "Simple System Requirement Control Test Result Details", required = true) @RequestBody ItemViewDTO itemDTO
//	) {
//		Long result = systemRequirementControlTestResultService.delete(itemDTO.getId());
//
//		return result;
//	}
}
