package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitEditDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitViewExtDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.BusinessUnitService;
import com.cyberintech.vrisk.server.service.OrganizationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Business Units management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = BusinessUnitController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Business Units Management Controller"
)
@Tag(name = "Business Units Management")
public class BusinessUnitController {

	static final String CONTROLLER_URI = "/api/business-units";

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Business Units List for current Risk Model
	 *
	 * @return Business Units List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Business Units List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_UNIT_READ)")
	public FilteredResponse<NameFilter, BusinessUnitViewExtDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, BusinessUnitViewExtDTO> result = businessUnitService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Business Unit details
	 *
	 * @return Business Unit Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Business Unit details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_UNIT_READ)")
	public BusinessUnitEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		BusinessUnitEditDTO itemDTO = businessUnitService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Download CSV Template
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/download-csv-template", name = "Get Business Units CSV template")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_UNIT_DOWNLOAD_TEMPLATE)")
	public void downloadCSVTemplate(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = businessUnitService.getDownloadTemplate();

		// Build HTTP Response
		// Response.ResponseBuilder response = Response.ok(byteArrayInputStream.readAllBytes());
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		response.setHeader("Content-Disposition", "attachment; filename=\"BusinessUnitsTemplate.csv\"");
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV Template
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/download-as-csv", name = "Get Business Units Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_UNIT_DOWNLOAD)")
	public void downloadCSVData(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = businessUnitService.getDownloadData();

		// Build HTTP Response
		// Response.ResponseBuilder response = Response.ok(byteArrayInputStream.readAllBytes());
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		String fileName = applicationProperties.buildExportFileName("BusinessUnits", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Upload file with Business Units data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/upload-from-csv", name = "Import Business Units data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_UNIT_UPLOAD)")
	public ImportResultDTO uploadCSVTemplate(@RequestParam("file") MultipartFile file) {
		ImportResultDTO result = businessUnitService.importFromCSVFile(file);

		return result;
	}

	/**
	 * Create new Business Unit
	 *
	 * @return New Business Unit
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Business Unit", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_UNIT_CREATE)")
	public BusinessUnitEditDTO create(
		@Parameter(description = "Business Unit Details", required = true) @RequestBody BusinessUnitEditDTO newItemDTO
	) {

		BusinessUnitEditDTO result = businessUnitService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Business Unit
	 *
	 * @return Updated Business Unit
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Business Unit", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_UNIT_UPDATE)")
	public BusinessUnitEditDTO update(
		@Parameter(description = "Business Units update Details", required = true) @RequestBody BusinessUnitEditDTO itemDTO
	) {

		BusinessUnitEditDTO result = businessUnitService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Business Unit
	 *
	 * @return ID of removed Business Unit
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Business Unit", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_UNIT_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Business Unit Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = businessUnitService.delete(itemDTO.getId());

		return result;
	}

}
