package com.cyberintech.vrisk.server.integration.bigid.batch.common.vo;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatalogImportJobParamsVO {
	@Parameter(name = "Organization Id", required = true, example = "1")
	@NotNull
	private Long organizationId;
	@NotNull
	@Parameter(name = "User Id", required = true, example = "2")
	private Long userId;

	private JobExecutionControlVO executionControl;
}
