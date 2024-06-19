package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.organization.SupportedLanguageViewDTO;
import com.cyberintech.vrisk.server.service.SupportedLanguagesService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.List;

@RestController
@RequestMapping(
	value = SupportedLanguagesController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Supported Languages Management Controller"
)
@Tag(name = "Supported Languages Management Controller")
public class SupportedLanguagesController {

	static final String CONTROLLER_URI = "api/supported-languages";

	@Autowired
	private SupportedLanguagesService supportedLanguagesService;

	/**
	 * Get Supported Languages List for current Organization
	 *
	 * @return Supported Languages List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Supported Languages List for current Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<SupportedLanguageViewDTO> getListForCurrentOrganization() {

		List<SupportedLanguageViewDTO> result = supportedLanguagesService.getListForCurrentOrganization();

		return result;
	}

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
		FilteredResponse<NameFilter,SupportedLanguageViewDTO> result = supportedLanguagesService.getListFiltered(filteredRequest, true);

		return result;
	}

}
