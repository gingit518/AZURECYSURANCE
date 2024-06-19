package com.cyberintech.vrisk.server.service.csv;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * @author Andrii Iakovenko
 * @since 2022-07-19
 */
@Slf4j
@RequiredArgsConstructor
public class QuantMetricImporter implements CSVImporter {

	public static final String QUANT_METRIC_NAME_HEADER = "Quant Metric Name";
	public static final String QUANT_METRIC_DESCRIPTION_HEADER = "Description";
	public static final String QUANT_METRIC_ORDINAL_HEADER = "Ordinal";
	public static final String QUANT_METRIC_QUANT_NAME_HEADER = "Quant Name";
	public static final String QUANT_METRIC_LEVEL_HEADER = "Quant Metric Level";
	public static final String QUANT_METRIC_FORMULA_HEADER = "Metric Formula";
	public static final String QUANT_METRIC_FORMULA_DATA_HEADER = "Metric Formula Data";
	public static final String QUANT_METRIC_REGULATION_ACRONYMS_HEADER = "Regulation Acronyms";
	public static final String QUANT_METRIC_REGULATION_RESTRICTED_HEADER = "Is Regulation Restricted";
	public static final String QUANT_METRIC_DATA_TYPE_CLASSIFICATION_HEADER = "Data Classes";
	public static final String QUANT_METRIC_TCHNOLOGY_CATEGORIES_HEADER = "Technology Categories";
	public static final String QUANT_METRIC_INDUSTRIES_HEADER = "Industries";

	@Override
	public ImportResultDTO doImport(InputStream inputStream) {
		ImportResultDTO result = new ImportResultDTO();
		return result;
	}
}
