package com.cyberintech.vrisk.api.controller.rest.integrations;

// import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogImportJobParamsVO;
// import com.cyberintech.vrisk.server.integration.bigid.batch.management.CatalogImportJobManagementService;
// import com.cyberintech.vrisk.server.integration.bigid.batch.management.vo.CatalogImportLaunchJobResponseVO;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping(
	value = BigIdIntegrationController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Integrations - Big Id Controller"
)
public class BigIdIntegrationController {

	static final String CONTROLLER_URI = "/api/integrations/big-id";

	/*
	private final CatalogImportJobManagementService catalogImportJobManagementService;

	@PostMapping(value = "/catalog-import/job/launch", name = "Start catalog import job with provided parameters.")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	ResponseEntity<CatalogImportLaunchJobResponseVO> startJob(@RequestBody CatalogImportJobParamsVO jobParams) {
		return ResponseEntity.ok(catalogImportJobManagementService.launch(jobParams));
	}

	@GetMapping(value = "/catalog-import/job/{jobId}", name = "Get Catalog import job status.")
	@Parameters({
		@Parameter(name = "jobId", value = "The Catalog Import Job Id", example = "10", required = true, dataType = "long", paramType = "path"),
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	ResponseEntity<CatalogImportLaunchJobResponseVO> getJobStatus(@PathVariable Long jobId) {
		return ResponseEntity.ok(catalogImportJobManagementService.getJobStatus(jobId));
	}

	@GetMapping(value = "/catalog-import/job", name = "Get Catalog Import Jobs list with descending order of execution.")
	@Parameters({
		@Parameter(name = "size", value = "Size of the Page", example = "10", required = true, dataType = "int", in = ParameterIn.QUERY),
		@Parameter(name = "page", value = "Number of current page", example = "0", required = true, dataType = "int", in = ParameterIn.QUERY),
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	ResponseEntity<Page<CatalogImportLaunchJobResponseVO>> getCatalogImportJobs(
		@Parameter(hidden = true) @ApiIgnore @PageableDefault Pageable pageable) {
		return ResponseEntity.ok(catalogImportJobManagementService.getRecentJobInstances(pageable));
	}
	*/

}
