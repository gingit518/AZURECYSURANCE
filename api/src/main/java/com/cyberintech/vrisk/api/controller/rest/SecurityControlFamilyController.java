package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ByParentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityControlFamilyDTO;
import com.cyberintech.vrisk.server.service.SecurityControlFamilyService;
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

/**
 * Security Control Families management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-01-23
 */
@RestController
@RequestMapping(
	value = SecurityControlFamilyController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Security Control Families Management Controller"
)
@Tag(name = "Security Control Families Management")
public class SecurityControlFamilyController {

	static final String CONTROLLER_URI = "/api/security-control-families";

	@Autowired
	private SecurityControlFamilyService securityControlFamilyService;

	/**
	 * Get Security Control Families List for current Risk Model
	 *
	 * @return Security Control Families List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Security Control Families List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<ByParentFilter, SecurityControlFamilyDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ByParentFilter> filteredRequest
	) {

		FilteredResponse<ByParentFilter, SecurityControlFamilyDTO> result = securityControlFamilyService.getListFiltered(filteredRequest);

		return result;
	}

}
