package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.AuditLogFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.audit.AuditLogViewDTO;
import com.cyberintech.vrisk.server.model.dto.audit.AuditLogViewExtendedDTO;
import com.cyberintech.vrisk.server.model.dto.audit.ItemTypeDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.service.AuditLogService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Audit Log management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-23
 */
@RestController
@RequestMapping(
	value = AuditLogsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Audit Log Management Controller"
)
@Tag(name = "Audit Log Management")
public class AuditLogsController {

	static final String CONTROLLER_URI = "/api/audit-logs";

	@Autowired
	private AuditLogService auditLogService;

	/**
	 * Get Audit Log List for current Risk Model
	 *
	 * @return Audit Log List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Audit Log List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).AUDIT_LOG_READ)")
	public FilteredResponse<AuditLogFilter, AuditLogViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<AuditLogFilter> filteredRequest
	) {

		FilteredResponse<AuditLogFilter, AuditLogViewDTO> result = auditLogService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Audit Log item types
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/item-types", name = "Get Audit Log item types")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<ItemTypeDTO> getItemTypes() {
		return Arrays.stream(VItemType.values()).map(ItemTypeDTO::new).collect(Collectors.toList());
	}

	/**
	 * Get Audit Log details
	 *
	 * @return Audit Log Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Audit Log details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).AUDIT_LOG_DETAILS)")
	public AuditLogViewDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {
		AuditLogViewDTO itemDTO = auditLogService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Get all Audit Logs for some particular entity
	 *
	 * @return Audit Logs
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/entity/log", name = "Get all Audit Logs for some particular entity")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).AUDIT_LOG_DETAILS)")
	public List<AuditLogViewExtendedDTO> searchAuditLogDetailsForEntity(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody AuditLogFilter filter
	) {
		List<AuditLogViewExtendedDTO> result = auditLogService.searchItemAuditLogs(filter);

		return result;
	}

}
