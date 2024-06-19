package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.organization.SupportedLanguageEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.SupportedLanguageViewDTO;
import com.cyberintech.vrisk.server.service.SupportedLanguagesService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping(
	value = AdminSupportedLanguagesController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Supported Languages Management Controller"
)
@Tags(value = {@Tag(name = "Admin Supported Languages Management Controller"), @Tag(name = "Administration")})
public class AdminSupportedLanguagesController {

	static final String CONTROLLER_URI = "api/admin/supported-languages";

	@Autowired
	private SupportedLanguagesService supportedLanguagesService;

	/**
	 * Get Supported Languages List filtered
	 *
	 * @return Supported Languages List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Supported Languages List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter,SupportedLanguageViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {
		FilteredResponse<NameFilter, SupportedLanguageViewDTO> result = supportedLanguagesService.getListFiltered(filteredRequest, false);

		return result;
	}

	/**
	 * Get Supported Language details
	 *
	 * @return Supported Language Details
	 */
	@RequestMapping(method = RequestMethod.GET, value="/{itemId}", name = "Get Supported Language details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public SupportedLanguageEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		SupportedLanguageEditDTO itemDTO = supportedLanguagesService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Update Supported Language
	 *
	 * @return Updated Supported Language
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Supported Language")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public SupportedLanguageEditDTO update(
		@Parameter(description = "Supported Language update Details", required = true) @RequestBody SupportedLanguageEditDTO itemDTO
	) {

		SupportedLanguageEditDTO result = supportedLanguagesService.update(itemDTO);

		return result;
	}

}
