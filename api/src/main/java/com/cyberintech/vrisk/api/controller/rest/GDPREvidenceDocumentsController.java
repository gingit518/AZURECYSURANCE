package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPREvidenceDocumentsDTO;
import com.cyberintech.vrisk.server.service.GDPREvidenceDocumentsService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

/**
 * GDPR Evidence Documents management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-11-11
 */
@RestController
@RequestMapping(
	value = GDPREvidenceDocumentsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "GDPR Evidence Documents Management Controller"
)
@Tag(name = "GDPR Evidence Documents Management")
public class GDPREvidenceDocumentsController {

	static final String CONTROLLER_URI = "/api/gdpr-evidence-documents";

	@Autowired
	private GDPREvidenceDocumentsService gdprEvidenceDocumentsService;

	/**
	 * Get GDPR Evidence Documents List for current Risk Model
	 *
	 * @return GDPR Evidence Documents List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "GDPR Evidence Documents List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_EVIDENCE_DOCUMENT_READ)")
	public FilteredResponse<NameFilter, GDPREvidenceDocumentsDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, GDPREvidenceDocumentsDTO> result = gdprEvidenceDocumentsService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get GDPR Evidence Documents details
	 *
	 * @return GDPR Evidence Documents Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get GDPR Evidence Documents details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_EVIDENCE_DOCUMENT_READ)")
	public GDPREvidenceDocumentsDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		GDPREvidenceDocumentsDTO itemDTO = gdprEvidenceDocumentsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new GDPR Evidence Documents
	 *
	 * @return New GDPR Evidence Documents
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new GDPR Evidence Documents", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public GDPREvidenceDocumentsDTO create(
		@Parameter(description = "GDPR Evidence Documents Details", required = true) @RequestBody GDPREvidenceDocumentsDTO newItemDTO
	) {

		GDPREvidenceDocumentsDTO result = gdprEvidenceDocumentsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update GDPR Evidence Documents
	 *
	 * @return Updated GDPR Evidence Documents
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing GDPR Evidence Documents", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_EVIDENCE_DOCUMENT_UPDATE)")
	public GDPREvidenceDocumentsDTO update(
		@Parameter(description = "GDPR Evidence Documents update Details", required = true) @RequestBody GDPREvidenceDocumentsDTO itemDTO
	) {

		GDPREvidenceDocumentsDTO result = gdprEvidenceDocumentsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes GDPR Evidence Documents
	 *
	 * @return ID of removed GDPR Evidence Documents
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing GDPR Evidence Documents", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(
		@Parameter(description = "Simple GDPR Evidence Documents Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = gdprEvidenceDocumentsService.delete(itemDTO.getId());

		return result;
	}

}
