package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.hints.HintImportDTO;
import com.cyberintech.vrisk.server.model.dto.hints.HintLocalizedViewDTO;
import com.cyberintech.vrisk.server.model.dto.hints.HintsDTO;
import com.cyberintech.vrisk.server.service.HintsService;
import com.cyberintech.vrisk.server.service.LanguageConstantService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
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
import java.util.List;

/**
 * Hints management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-05
 */
@RestController
@RequestMapping(
	value = HintsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Hints Management Controller"
)
@Tag(name = "Hints Management")
public class HintsController {

	static final String CONTROLLER_URI = "/api/hints";

	@Autowired
	private HintsService hintsService;

	@Autowired
	private LanguageConstantService languageConstantService;

	/**
	 * Get Hints List for specified codes
	 *
	 * @return Hints List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/get/{langCode}", name = "Hints List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<HintLocalizedViewDTO> getListForCodes(
		@Parameter(description = "List of Hint codes", required = true) @RequestBody List<String> hintCodes,
		@PathVariable("langCode") @NotNull @Size(min = 1) String langCode
	) {

		List<HintLocalizedViewDTO> result = hintsService.getList(hintCodes, langCode);

		return result;
	}

	/**
	 * Get Hints List for current Risk Model
	 *
	 * @return Hints List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Hints List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINT_READ)")
	public FilteredResponse<NameFilter, HintsDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
		, @Parameter(description = "Include translations", required = false) @Nullable @RequestParam(required = false) Boolean includeTranslations
	) {

		FilteredResponse<NameFilter, HintsDTO> result = hintsService.getListFiltered(filteredRequest, includeTranslations);

		return result;
	}

	/**
	 * Get Hint details
	 *
	 * @return Hint Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Hint details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINT_READ)")
	public HintsDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		HintsDTO itemDTO = hintsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Hint
	 *
	 * @return New Hint
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Hint", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINT_CREATE)")
	public HintsDTO create(
		@Parameter(description = "Hint Details", required = true) @RequestBody HintsDTO newItemDTO
	) {

		HintsDTO result = hintsService.create(newItemDTO);

		// Reloading Hints Translations
		languageConstantService.reloadHintsLanguageConstants();

		return result;
	}

	/**
	 * Create new Hint
	 *
	 * @return New Hint
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/import", name = "Create new Hint", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINT_CREATE)")
	public List<HintsDTO> importFromJson(@Parameter(description = "Hint Details", required = true) @RequestBody List<HintImportDTO> hintsDetailsList) {

		List<HintsDTO> result = hintsService.importFromLinks(hintsDetailsList);

		// Reloading Hints Translations
		languageConstantService.reloadHintsLanguageConstants();

		return result;
	}

	/**
	 * Update Hint
	 *
	 * @return Updated Hint
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Hint", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINT_UPDATE)")
	public HintsDTO update(
		@Parameter(description = "Hints update Details", required = true) @RequestBody HintsDTO itemDTO
	) {

		HintsDTO result = hintsService.update(itemDTO);

		// Reloading Hints Translations
		languageConstantService.reloadHintsLanguageConstants();

		return result;
	}

	/**
	 * Deletes Hint
	 *
	 * @return ID of removed Hint
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Hint", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINT_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Hint Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = hintsService.delete(itemDTO.getId());

		return result;
	}

	/**
	 * Download CSV document with Hints
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/export", name = "Export Hints Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINTS_EXPORT)")
	public void downloadAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = hintsService.getDownloadData();

		// Build HTTP Response
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		response.setHeader("Content-Disposition", "attachment; filename=\"Hints.csv\"");
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Upload file with Hints data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/import", name = "Import Hints data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINTS_IMPORT)")
	public ImportResultDTO uploadAsCSV(@RequestParam("file") MultipartFile file) {
		ImportResultDTO result = hintsService.importFromCSVFile(file);

		return result;
	}

}
