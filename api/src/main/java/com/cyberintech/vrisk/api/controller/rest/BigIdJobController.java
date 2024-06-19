package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogImportJobParamsVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.management.CatalogImportJobManagementService;
import com.cyberintech.vrisk.server.integration.bigid.batch.management.vo.CatalogImportLaunchJobResponseVO;
import com.cyberintech.vrisk.server.rest.exception.ServerException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

/**
 * BigID integration job controller.
 * 
 * @author Andrii Iakovenko
 * @since  2022-08-28
 */
@RestController
@RequestMapping(value = BigIdJobController.CONTROLLER_URI, produces = MediaType.APPLICATION_JSON, name = "BigID Job Management Controller")
@Tag(name = "BigID Job Management")
@Slf4j
public class BigIdJobController {

	static final String CONTROLLER_URI = "/api/bigid/jobs";

	@Autowired
	private CatalogImportJobManagementService jobService;

	@RequestMapping(method = RequestMethod.POST, name = "Start Job")
	public CatalogImportLaunchJobResponseVO launchJob(@RequestBody CatalogImportJobParamsVO params) {
		try {
			CatalogImportLaunchJobResponseVO jobExecution = jobService.launch(params);
			return jobExecution;
		} catch (Exception e) {
			log.error("Failed to start BigID integration job", e);
			throw new ServerException("Failed to start BigID integration job");
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{jobId}", name = "Get Job details")
	public CatalogImportLaunchJobResponseVO getJobStatus(@PathVariable("jobId") @NotNull Long jobId) {
		try {
			CatalogImportLaunchJobResponseVO jobExecution = jobService.getJobStatus(jobId);
			return jobExecution;
		} catch (Exception e) {
			log.error("Failed to get BigID integration job status", e);
			throw new ServerException("Failed to get BigID integration job status");
		}
	}
}
