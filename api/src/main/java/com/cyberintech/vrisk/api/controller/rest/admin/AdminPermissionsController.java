package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.role.PermissionRolesDTO;
import com.cyberintech.vrisk.server.model.dto.role.RolePermissionUpdateDTO;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.admin.AdminPermissionService;
import com.cyberintech.vrisk.server.service.admin.AdminUserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Admin Permissions controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-14
 */
@RestController
@RequestMapping(
	value = AdminPermissionsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Permissions Management Controller"
)
@Tags(value = {@Tag(name = "Admin Permissions Management"), @Tag(name = "Administration")})
public class AdminPermissionsController {

	static final String CONTROLLER_URI = "/api/admin/permissions";

	@Autowired
	private AdminUserService adminUserService;

	@Autowired
	private AdminPermissionService adminPermissionService;

	@Autowired
	private ApplicationProperties applicationProperties;

	/**
	 * Get all Permissions data with Roles
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/permission-roles", name = "Get all Permissions data with Roles")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<PermissionRolesDTO> getAllPermissionRoles() {
		return adminPermissionService.getAllPermissionRoles();
	}

	/**
	 * Update permissions
	 *
	 * @return New User
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/update-items", name = "Update permissions", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<RolePermissionUpdateDTO> updateItems(@Parameter(description = "Permissions List", required = true) @RequestBody List<RolePermissionUpdateDTO> permissionItems) {

		List<RolePermissionUpdateDTO> result = adminPermissionService.updateRolePermissionData(permissionItems);

		return result;
	}

	/**
	 * Upload file with Environment permissions data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/import", name = "Import Permissions data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ImportResultDTO uploadSubsidiariesDataAsCSV(@RequestParam("file") MultipartFile file, @RequestParam(name = "forceSynchronize", required = false) Boolean forceSynchronize) {
		ImportResultDTO result = adminPermissionService.importFromCSVFile(file, forceSynchronize);

		return result;
	}

	/**
	 * Download file with Environment permissions data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/export", name = "Export Permissions data as CSV template")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadPermissionsAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = adminPermissionService.getDownloadData();

		// Build HTTP Response
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("Permissions", new SimpleDateFormat("yyyyMMdd").format(new Date()), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}


}
