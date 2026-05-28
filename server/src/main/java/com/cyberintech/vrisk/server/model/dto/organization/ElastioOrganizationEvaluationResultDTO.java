package com.cyberintech.vrisk.server.model.dto.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Organization View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2026-03-06
 */
@Setter
@Getter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElastioOrganizationEvaluationResultDTO {

	// ========== ========== ========== BASELINE SCENARIO VARIABLES ========== ========== ========== //
	private Double baselineStorageAnnualCost;
	private Double ebsSnapshotCost;
	private Double s3VersioningCost;
	private Double backupValidationCost;
	private Double totalBaselineAnnualCost;
	private Double baselineRansomwareExposure;
	private Double baselineDowntimeLoss;
	// ========== ========== ========== BASELINE SCENARIO VARIABLES ========== ========== ========== //

	// ========== ========== ========== CORE CALCULATIONS VARIABLES ========== ========== ========== //
	private Double downtimeLossPostElastio;
	private Double storageSavings;
	private Double downtimeSavings;
	private Double roiRansomware;
	private Double roiStorage;
	private Double totalAnnualSavings;
	private Double paybackPeriodRansomware;
	private Double paybackPeriodStorage;
	// ========== ========== ========== CORE CALCULATIONS VARIABLES ========== ========== ========== //

	public void initDemoValues() {
		baselineStorageAnnualCost = 2450000D;
		ebsSnapshotCost = 850000D;
		s3VersioningCost = 320000D;
		backupValidationCost = 180000D;
		totalBaselineAnnualCost = baselineStorageAnnualCost + ebsSnapshotCost + s3VersioningCost + backupValidationCost;

		baselineRansomwareExposure = 12500000D;
		baselineDowntimeLoss = 8750000D;

		downtimeLossPostElastio = 1250000D;
		storageSavings = 1520000D;
		downtimeSavings = 7500000D;
		roiRansomware = 485D;
		roiStorage = 280D;
		paybackPeriodRansomware = 3.2D;
		paybackPeriodStorage = 8.5D;

		totalAnnualSavings = downtimeLossPostElastio + storageSavings + downtimeSavings;
	}

}
