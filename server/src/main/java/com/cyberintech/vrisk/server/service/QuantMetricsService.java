package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryRefDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.MetricFormulaItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantMetricsEditDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantMetricsViewDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantsRefDTO;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationRefDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_constants.RiskModelConstantViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.*;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import com.cyberintech.vrisk.server.service.utils.ExportUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import java.io.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.cyberintech.vrisk.server.service.csv.QuantMetricImporter.*;

/**
 * Quant Metrics management Service. Implements basic user CRUD.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2018-11-13
 */
@Service
@Slf4j
public class QuantMetricsService {
	@Autowired
	private IndustryRepository industryRepository;
	@Autowired
	private NewsRepository newsRepository;
	@Autowired
	private DataTypeClassificationRepository dataTypeClassificationRepository;
	@Autowired
	private DataAssetClassificationRepository dataAssetClassificationRepository;
	@Autowired
	private FormulaItemsRepository formulaItemsRepository;
	@Autowired
	private QualMetricsRepository qualMetricsRepository;
	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private QuantMetricsRepository quantMetricsRepository;

	@Autowired
	private QuantsRepository quantsRepository;

	@Autowired
	private DataTypeClassificationService dataTypeClassificationService;

//	@Autowired
//	private MetricVariablesRepository metricVariablesRepository;

	@Autowired
	private MetricFormulaItemsRepository metricFormulaItemsRepository;

	@Autowired
	private RiskModelConstantRepository riskModelConstantRepository;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private RegulationService regulationService;

	@Autowired
	private TechnologyCategoryService technologyCategoryService;

	@Autowired
	private TechnologyService technologyService;

	@Autowired
	private UserService userService;

	@Autowired
	private RegulationRepository regulationRepository;

	@Autowired
	private VariableTypesRepository variableTypesRepository;

	@Autowired
	private TechnologyCategoryRepository technologyCategoriesRepository;

	@Autowired
	private IndustryService industryService;

	/**
	 * Get Quant Metrics List
	 *
	 * @return Quant Metrics List
	 */
	public List<QuantMetricsViewDTO> getList() {
		List<QuantMetrics> items = quantMetricsRepository.findAll();

		List<QuantMetricsViewDTO> itemDTOs = QuantMetricsViewDTO.fromEntitiesList(items, QuantMetricsViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Quant Metrics List
	 *
	 * @return Quant Metrics List
	 */
	public FilteredResponse<NameFilter, QuantMetricsViewDTO> getListFiltered(Long riskModelId, FilteredRequest<NameFilter> filteredRequest) {
		List<QuantMetrics> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, QuantMetricsViewDTO> filteredResponse = new FilteredResponse<NameFilter, QuantMetricsViewDTO>(filteredRequest);

		String namePattern = "";
		List<Long> excludeIds = Arrays.asList(0L);

		if (filteredRequest.getFilter() != null) {
			if (filteredRequest.getFilter().getName() != null) {
				namePattern = filteredRequest.getFilter().getName();
			}
			if (filteredRequest.getFilter().getExcludeIds() != null && filteredRequest.getFilter().getExcludeIds().size() > 0) {
				excludeIds = filteredRequest.getFilter().getExcludeIds();
			}
		}

		items = quantMetricsRepository.getListByRiskModelAndName(riskModelId, namePattern, excludeIds, filteredRequest.toPageRequest());
		count = quantMetricsRepository.getCountByRiskModelAndName(riskModelId, namePattern, excludeIds);

		// List<QuantMetricsViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, QuantMetricsViewDTO.class);
		List<QuantMetricsViewDTO> itemsDTOList = items.stream().map(quantMetrics -> {
			QuantMetricsViewDTO result = new QuantMetricsViewDTO(quantMetrics);
			// FormulaBuilder builder = FormulaBuilder.of(quantMetrics, quantMetrics.getName());
			// builder.build();
			result.setFormula(buildFormula(quantMetrics));

			return result;
		}).collect(Collectors.toList());

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Check is Quant Defined in he Risk Model
	 *
	 * @param riskModelId
	 * @param quantsDomain
	 * @return
	 */
	public boolean isQuanDefined(Long riskModelId, QuantsDomain quantsDomain) {
		return isQuanDefined(riskModelId, quantsDomain.getId());
	}

	/**
	 * Check is Quant Defined in he Risk Model
	 *
	 * @param riskModelId
	 * @param quantId
	 * @return
	 */
	public boolean isQuanDefined(Long riskModelId, Long quantId) {
		Long count = quantMetricsRepository.getCountForRiskModelIdAndQuantId(riskModelId, quantId);

		return count > 0;
	}

	/**
	 * Get Quant Metric details
	 *
	 * @return Quant Metric Details
	 */
	public QuantMetricsEditDTO getDetails(Long itemId) {

		QuantMetrics itemDetails = getQuantMetric(itemId);

		QuantMetricsEditDTO itemDTO = new QuantMetricsEditDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Quant Metric details
	 *
	 * @return Quant Metric Details
	 */
	public QuantMetrics getQuantMetric(Long itemId) {
		QuantMetrics itemDetails;

		try {
			itemDetails = quantMetricsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Quant Metric not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		// TODO: 17.02.20 add verification
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Risk Quant Metrics List inside current Organization
	 *
	 * @return Quant Metrics List
	 */
	public List<QuantMetricsViewDTO> getListByRiskModel(Long riskModelId) {

		RiskModels riskModel = riskModelService.getRiskModel(riskModelId);

		List<QuantMetrics> items = quantMetricsRepository.getListByRiskModelId(riskModelId);

		List<QuantMetricsViewDTO> itemDTOs = QuantMetricsViewDTO.fromEntitiesList(items, QuantMetricsViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Create new Quant Metric
	 *
	 * @return New Quant Metric
	 */
	public QuantMetricsEditDTO create(QuantMetricsEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());

		QuantMetrics newItem = new QuantMetrics();
		newItem.setRiskModelId(riskModel.getId());
		newItem.setCreatedAt(new Date());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		applyEntityChanges(newItemDTO, newItem);

		QuantMetrics saveResult = quantMetricsRepository.save(newItem);

		QuantMetricsEditDTO result = new QuantMetricsEditDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.QUANTIFICATION_METRIC,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, riskModel.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Risk Quant Metric
	 *
	 * @return Updated Quant Metrics
	 */
	public QuantMetricsEditDTO update(QuantMetricsEditDTO itemDTO) {

		QuantMetricsEditDTO result;

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		QuantMetrics existingItem = getQuantMetric(itemDTO.getId());
		QuantMetricsEditDTO existingItemDTO = new QuantMetricsEditDTO(existingItem);

		// Verify Risk Model and Organization Id
		// TODO: 17.02.20 add verification
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());

		// Update item details
		existingItem.setQuant(null);
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		QuantMetrics saveResult = quantMetricsRepository.save(existingItem);

		result = new QuantMetricsEditDTO(saveResult);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.QUANTIFICATION_METRIC,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, riskModel.getOrganizationId())
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(QuantMetricsEditDTO itemDTO, QuantMetrics entity) {

		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setOrdinal(itemDTO.getOrdinal());
		if (itemDTO.getQuantMetricLevel() != null) {
			entity.setQuantMetricLevel(itemDTO.getQuantMetricLevel());
		}

		// Set Data Type Classifications
		entity.setDataTypeClassifications(new HashSet<>());
		Optional.ofNullable(itemDTO.getDataTypeClassifications()).orElse(new ArrayList<>()).stream().forEach(dataTypeClassificationViewDTO -> {
			entity.getDataTypeClassifications().add(dataTypeClassificationService.getDataTypeClassificationForCurrentOrganization(dataTypeClassificationViewDTO.getId()));
		});

		// Set Technology Categories
		entity.setTechnologyCategories(new HashSet<>());
		Optional.ofNullable(itemDTO.getTechnologyCategories()).orElse(new ArrayList<>()).stream().forEach(technologyCategoryRef -> {
			entity.getTechnologyCategories().add(technologyCategoryService.getTechnologyCategoryForCurrentOrganization(technologyCategoryRef.getId()));
		});

		// Set Technologies
		entity.setTechnologies(new HashSet<>());
		Optional.ofNullable(itemDTO.getTechnologies()).orElse(new ArrayList<>()).stream().forEach(technologyRef -> {
			entity.getTechnologies().add(technologyService.getTechnologyForCurrentOrganization(technologyRef.getId()));
		});

		// Set Regulations
		if (itemDTO.getRegulations() != null) {
			entity.setRegulations(new HashSet<>());
			Optional.ofNullable(itemDTO.getRegulations()).orElse(new ArrayList<>()).stream().forEach(regulationRef -> {
				entity.getRegulations().add(regulationService.getItem(regulationRef.getId()));
			});
		}

		// Set Industries
		if (itemDTO.getIndustries() != null) {
			entity.setIndustries(new HashSet<>());
			Optional.ofNullable(itemDTO.getIndustries()).orElse(new ArrayList<>()).stream().forEach(industryRef -> {
				entity.getIndustries().add(industryService.getItem(industryRef.getId()));
			});
		}

		// Set Metric Formula Items
		Optional.ofNullable(itemDTO.getMetricFormulaItems()).ifPresent(metricFormulaItemViewDTOList -> {
			entity.setMetricFormulaItems(new HashSet<>());
			metricFormulaItemViewDTOList.stream().forEach(metricFormulaItemViewDTO -> {
				if (metricFormulaItemViewDTO.getId() != null) {
					MetricFormulaItems metricVariable = metricFormulaItemsRepository.findById(metricFormulaItemViewDTO.getId()).get();
					metricVariable.setName(metricFormulaItemViewDTO.getName());
					metricVariable.setDescription(metricFormulaItemViewDTO.getDescription());
					metricVariable.setOrdinal(metricFormulaItemViewDTO.getOrdinal());
					metricVariable.setValue(metricFormulaItemViewDTO.getValue());
					metricVariable.setIsOperation(metricFormulaItemViewDTO.getIsOperation());
					metricVariable.setOperation(metricFormulaItemViewDTO.getOperation());
					if (metricFormulaItemViewDTO.getVariableType() != null && metricFormulaItemViewDTO.getVariableType().getId() != null) {
						metricVariable.setVariableTypeId(metricFormulaItemViewDTO.getVariableType().getId());
					}
					if (metricFormulaItemViewDTO.getQuantMetricRef() != null && metricFormulaItemViewDTO.getQuantMetricRef().getId() != null) {
						metricVariable.setQuantMetricRefId(metricFormulaItemViewDTO.getQuantMetricRef().getId());
					}
					if (metricFormulaItemViewDTO.getRiskModelConstantRef() != null && metricFormulaItemViewDTO.getRiskModelConstantRef().getId() != null) {
						metricVariable.setRiskModelConstantRefId(metricFormulaItemViewDTO.getRiskModelConstantRef().getId());
					}
					entity.getMetricFormulaItems().add(metricVariable);
				} else {
//					MetricFormulaItems metricVariable = metricFormulaItemViewDTO.toEntity();
					MetricFormulaItems metricVariable = new MetricFormulaItems();
					metricVariable.setName(metricFormulaItemViewDTO.getName());
					metricVariable.setDescription(metricFormulaItemViewDTO.getDescription());
					metricVariable.setOrdinal(metricFormulaItemViewDTO.getOrdinal());
					metricVariable.setValue(metricFormulaItemViewDTO.getValue());
					metricVariable.setIsOperation(metricFormulaItemViewDTO.getIsOperation());
					metricVariable.setOperation(metricFormulaItemViewDTO.getOperation());
					if (metricFormulaItemViewDTO.getVariableType() != null && metricFormulaItemViewDTO.getVariableType().getId() != null) {
						metricVariable.setVariableTypeId(metricFormulaItemViewDTO.getVariableType().getId());
					}
					if (metricFormulaItemViewDTO.getQuantMetricRef() != null && metricFormulaItemViewDTO.getQuantMetricRef().getId() != null) {
						metricVariable.setQuantMetricRefId(metricFormulaItemViewDTO.getQuantMetricRef().getId());
					}
					if (metricFormulaItemViewDTO.getRiskModelConstantRef() != null && metricFormulaItemViewDTO.getRiskModelConstantRef().getId() != null) {
						metricVariable.setRiskModelConstantRefId(metricFormulaItemViewDTO.getRiskModelConstantRef().getId());
					}
					metricFormulaItemsRepository.save(metricVariable);
					entity.getMetricFormulaItems().add(metricVariable);
				}
			});
		});
		/*
		Optional.ofNullable(itemDTO.getMetricVariables()).ifPresent(metricVariableViewDTOList -> {
			entity.setMetricVariables(new HashSet<>());
			metricVariableViewDTOList.stream().forEach(metricVariableViewDTO -> {
				if (metricVariableViewDTO.getId() != null) {
					MetricVariables metricVariable = metricVariablesRepository.findById(metricVariableViewDTO.getId()).get();
					metricVariableViewDTO.toEntity(metricVariable);
					entity.getMetricVariables().add(metricVariable);
				} else {
					MetricVariables metricVariable = metricVariableViewDTO.toEntity();
					entity.getMetricVariables().add(metricVariable);
				}
			});
		});
		*/

		if (itemDTO.getQuant() != null) {
			entity.setQuant(quantsRepository.findById(itemDTO.getQuant().getId()).get());
		}

		// Set Deployment Type restriction
		entity.setDeploymentType(itemDTO.getDeploymentType());
		if (itemDTO.getMeasurementUnit() != null) {
			entity.setMeasurementUnit(itemDTO.getMeasurementUnit());
		}
		if (itemDTO.getUnitUIPosition() != null) {
			entity.setUnitUIPosition(itemDTO.getUnitUIPosition());
		}

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Formula Analysis result
	 */
	public class FormulaAnalysisResult {
		public boolean isConstantInvolved;
		public boolean isProcessesInvolved;
		public boolean isSystemsInvolved;
		public boolean isOrganizationInvolved;
	}

	/**
	 * Build Formula string for Quant Metric
	 *
	 * @param quantMetric
	 * @return
	 */
	public FormulaAnalysisResult analyzeFormula(QuantMetrics quantMetric) {

		FormulaAnalysisResult result = new FormulaAnalysisResult();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());

		for (MetricFormulaItems formulaItem : formulaItems) {
			/*
				(1, 'CONSTANT')
				(2, 'PROCESS_REVENUE')
				(3, 'SYSTEM_NUMBER_OF_REC')
				(4, 'ORGANIZATION_REVENUE')
			 */
			if (formulaItem.getVariableType() != null) {
				if (formulaItem.getVariableType().getId().equals(1l)) { // CONSTANT
					result.isConstantInvolved = true;
				} else if (formulaItem.getVariableType().getId().equals(2l)) { // PROCESS_REVENUE
					result.isProcessesInvolved = true;
				} else if (formulaItem.getVariableType().getId().equals(3l)) { // SYSTEM_NUMBER_OF_REC
					result.isSystemsInvolved = true;
				} else if (formulaItem.getVariableType().getId().equals(4l)) { // ORGANIZATION_REVENUE
					result.isOrganizationInvolved = true;
				}
			}
		}

		return result;
	}

	/**
	 * Build Formula string for Quant Metric
	 *
	 * @param quantMetric
	 * @return
	 */
	public String buildFormula(QuantMetrics quantMetric) {
		String result = "";

		if (quantMetric != null) {
			int i = 1;

			List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
			formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

			for (MetricFormulaItems formulaItem : formulaItems) {
				if (formulaItem.getIsOperation()) {
					if (formulaItem.getOperation().equals(VariableOperation.PLUS)) {
						result += " + ";
					} else if (formulaItem.getOperation().equals(VariableOperation.MULTIPLY)) {
						result += " * ";
					} else if (formulaItem.getOperation().equals(VariableOperation.MINUS)) {
						result += " - ";
					} else if (formulaItem.getOperation().equals(VariableOperation.DIVIDE)) {
						result += " / ";
					} else if (formulaItem.getOperation().equals(VariableOperation.OPEN_BRACKET)) {
						result += "(";
					} else if (formulaItem.getOperation().equals(VariableOperation.CLOSE_BRACKET)) {
						result += ")";
					} else if (formulaItem.getOperation().equals(VariableOperation.MAX)) {
						result += " MAX";
					} else if (formulaItem.getOperation().equals(VariableOperation.MIN)) {
						result += " MIN";
					} else if (formulaItem.getOperation().equals(VariableOperation.COMMA)) {
						result += ", ";
					} else if (formulaItem.getOperation().equals(VariableOperation.ABS)) {
						result += " ABS";
					} else if (formulaItem.getOperation().equals(VariableOperation.MEDIAN)) {
						result += " MEDIAN";
					} else if (formulaItem.getOperation().equals(VariableOperation.AVERAGE)) {
						result += " AVERAGE";
					} else if (formulaItem.getOperation().equals(VariableOperation.MODE)) {
						result += " MODE";
					} else if (formulaItem.getOperation().equals(VariableOperation.SQRT)) {
						result += " SQRT";
					} else if (formulaItem.getOperation().equals(VariableOperation.SUM)) {
						result += " SUM";
					} else if (formulaItem.getOperation().equals(VariableOperation.RAND)) {
						result += " RAND";
					} else if (formulaItem.getOperation().equals(VariableOperation.POWER)) {
						result += " POW";
					} else if (formulaItem.getOperation().equals(VariableOperation.EXPONENT)) {
						result += " EXP";
					} else if (formulaItem.getOperation().equals(VariableOperation.LOG)) {
						result += " LN";
					} else if (formulaItem.getOperation().equals(VariableOperation.HYPERBOLA)) {
						result += " 1 / ";
					}
				} else {
					String currentValue = "";
					if (formulaItem.getVariableType() == null) {
						currentValue = formulaItem.getName() != null ? formulaItem.getName() : "";
					} else if (formulaItem.getVariableType().getId().equals(VariableType.CONSTANT.getId())) { // CONSTANT
						currentValue = StringUtils.isNotEmpty(formulaItem.getName()) ? formulaItem.getName() : String.format("%,.2f", formulaItem.getValue());
					} else if (formulaItem.getVariableType().getId().equals(VariableType.RISK_MODEL_CONSTANT.getId())) { // RISC MODEL CONSTANT
						currentValue = StringUtils.isNotEmpty(formulaItem.getRiskModelConstantRef().getName()) ? formulaItem.getRiskModelConstantRef().getName() :
							String.format("%,.2f", formulaItem.getRiskModelConstantRef().getValue());
					} else if (formulaItem.getVariableType().getId().equals(VariableType.QUANT_METRIC.getId())) { // QUANTIFICATION METRIC
						currentValue = formulaItem.getQuantMetricRef() != null && StringUtils.isNotEmpty(formulaItem.getQuantMetricRef().getName())
							? formulaItem.getQuantMetricRef().getName() :
							formulaItem.getVariableType().getName();
					} else {
						currentValue = formulaItem.getVariableType().getName() != null ? formulaItem.getVariableType().getName() : "";
					}
					i++;

					result += currentValue;
				}
			}
		}

		return result.trim();
	}

	/**
	 * Build Formula string for Quant Metric Export
	 *
	 * @param quantMetric
	 * @return
	 */
	public String buildFormulaExport(QuantMetrics quantMetric) {
		String result = "";

		if (quantMetric != null) {
			int i = 1;

			List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
			formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

			for (MetricFormulaItems formulaItem : formulaItems) {
				if (formulaItem.getIsOperation()) {
					if (formulaItem.getOperation().equals(VariableOperation.PLUS)) {
						result += " + ";
					} else if (formulaItem.getOperation().equals(VariableOperation.MULTIPLY)) {
						result += " * ";
					} else if (formulaItem.getOperation().equals(VariableOperation.MINUS)) {
						result += " - ";
					} else if (formulaItem.getOperation().equals(VariableOperation.DIVIDE)) {
						result += " / ";
					} else if (formulaItem.getOperation().equals(VariableOperation.OPEN_BRACKET)) {
						result += " (";
					} else if (formulaItem.getOperation().equals(VariableOperation.CLOSE_BRACKET)) {
						result += ") ";
					} else if (formulaItem.getOperation().equals(VariableOperation.MAX)) {
						result += " MAX";
					} else if (formulaItem.getOperation().equals(VariableOperation.MIN)) {
						result += " MIN";
					} else if (formulaItem.getOperation().equals(VariableOperation.COMMA)) {
						result += ", ";
					} else if (formulaItem.getOperation().equals(VariableOperation.ABS)) {
						result += " ABS";
					} else if (formulaItem.getOperation().equals(VariableOperation.MEDIAN)) {
						result += " MEDIAN";
					} else if (formulaItem.getOperation().equals(VariableOperation.AVERAGE)) {
						result += " AVERAGE";
					} else if (formulaItem.getOperation().equals(VariableOperation.MODE)) {
						result += " MODE";
					} else if (formulaItem.getOperation().equals(VariableOperation.SQRT)) {
						result += " SQRT";
					} else if (formulaItem.getOperation().equals(VariableOperation.SUM)) {
						result += " SUM";
					} else if (formulaItem.getOperation().equals(VariableOperation.RAND)) {
						result += " RAND";
					} else if (formulaItem.getOperation().equals(VariableOperation.POWER)) {
						result += " POW";
					} else if (formulaItem.getOperation().equals(VariableOperation.EXPONENT)) {
						result += " EXP";
					} else if (formulaItem.getOperation().equals(VariableOperation.LOG)) {
						result += " LN";
					} else if (formulaItem.getOperation().equals(VariableOperation.HYPERBOLA)) {
						result += " 1 / ";
					}
				} else {
					String currentValue = "";
					if (formulaItem.getVariableType().getId().equals(1l)) { // CONSTANT
						currentValue = StringUtils.isNotEmpty(formulaItem.getName()) ? formulaItem.getName() : String.format("%,.2f", formulaItem.getValue());
						if (StringUtils.isNotEmpty(formulaItem.getName()) && formulaItem.getValue() != null) {
							currentValue += " [" + String.format("%,.2f", formulaItem.getValue()) + "] ";
						}
					} else if (formulaItem.getVariableType().getId().equals(16l)) { // RISC MODEL CONSTANT
						currentValue = StringUtils.isNotEmpty(formulaItem.getRiskModelConstantRef().getName()) ? formulaItem.getRiskModelConstantRef().getName() :
							String.format("%,.2f", formulaItem.getRiskModelConstantRef().getValue());
						if (StringUtils.isNotEmpty(formulaItem.getRiskModelConstantRef().getName()) && formulaItem.getRiskModelConstantRef().getValue() != null) {
							currentValue += " [" + String.format("%,.2f", formulaItem.getRiskModelConstantRef().getValue()) + "] ";
						}
					} else {
						currentValue = formulaItem.getVariableType().getName() != null ? formulaItem.getVariableType().getName() : "";
					}
					i++;

					result += currentValue;
				}
			}
		}

		return result;
	}

	/**
	 * Build Formula string for Quant Metric Export
	 *
	 * @param quantMetric
	 * @return
	 */
	public String buildFormulaDataExport(QuantMetrics quantMetric) {
		String result = "";

		ObjectWriter objectWriter = new ObjectMapper().writer();
		try {
			QuantMetricsEditDTO quantMetricDto = new QuantMetricsEditDTO(quantMetric);
			for (MetricFormulaItemViewDTO formulaItem : quantMetricDto.getMetricFormulaItems()) {
				formulaItem.setId(null);
				if (formulaItem.getQuantMetricRef() != null) formulaItem.getQuantMetricRef().setId(null);
			}
			String json = objectWriter.writeValueAsString(quantMetricDto.getMetricFormulaItems());
			result = json;
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize formula for QUANT: {}", quantMetric.getName());
		}

		return result;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(QuantMetricsEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		// if (existingItemDTO.getOwner() != null) logItems.add(AuditLogItemId.of(VItemType.SYSTEM_OWNER, existingItemDTO.getOwner().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Deletes Quant Metric Item
	 *
	 * @param itemId
	 * @return Id of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		QuantMetrics existingItem = getQuantMetric(itemId);
		QuantMetricsEditDTO existingItemDTO = new QuantMetricsEditDTO(existingItem);
		quantMetricsRepository.delete(existingItem);
		quantMetricsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.QUANTIFICATION_METRIC,
			existingItemDTO.getId(),
			existingItemDTO,
			null
		);

		return itemId;
	}

	public void exportQuantMetrics(ServletOutputStream outputStream, Long riskModelId) {
		try {
			CSVPrinter csvPrinter = createCsvPrinter(outputStream);

			List<QuantMetrics> quantMetrics = quantMetricsRepository.getListByRiskModelAndName(riskModelId, "", Arrays.asList(0L),
				PageRequest.of(0, 10000000, Sort.by("name")));
			for (QuantMetrics quantMetric : quantMetrics) {

				csvPrinter.printRecord(
					quantMetric.getName().trim(),
					(StringUtils.isNotEmpty(quantMetric.getDescription()) ? quantMetric.getDescription().trim() : ""),
					(quantMetric.getOrdinal() != null ? quantMetric.getOrdinal().toString().trim() : ""),
					(quantMetric.getQuant() != null ? quantMetric.getQuant().getName().trim() : ""),
					ExportUtils.metricLevelAsString(quantMetric.getQuantMetricLevel()),
					ExportUtils.regulationsAsString(quantMetric.getRegulations()),
					ExportUtils.dataTypeClassificationAsString(quantMetric.getDataTypeClassifications()),
					ExportUtils.technologyCategoriesAsString(quantMetric.getTechnologyCategories()),
					ExportUtils.industriesAsString(quantMetric.getIndustries()),
					(!quantMetric.getMetricFormulaItems().isEmpty() ? buildFormulaExport(quantMetric) : ""),
					(!quantMetric.getMetricFormulaItems().isEmpty() ? buildFormulaDataExport(quantMetric) : "")
				);
				//ExportUtils.metricFormulaItemsAsString(quantMetric.getMetricFormulaItems()));
			}
			csvPrinter.flush();
		} catch (IOException e) {
			log.error("Failed to Exposure Metrics CSV Data file", e);
			throw new InternalServerErrorException("Failed to generate Exposure Metrics CSV Data file");
		}
	}

	/**
	 * Create CSV Printer to build Technologies
	 *
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	private CSVPrinter createCsvPrinter(OutputStream outputStream) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVUtils.createCSVFormatBuilder(
			QUANT_METRIC_NAME_HEADER,
			QUANT_METRIC_DESCRIPTION_HEADER,
			QUANT_METRIC_ORDINAL_HEADER,
			QUANT_METRIC_QUANT_NAME_HEADER,
			QUANT_METRIC_LEVEL_HEADER,
			QUANT_METRIC_REGULATION_ACRONYMS_HEADER,
			QUANT_METRIC_DATA_TYPE_CLASSIFICATION_HEADER,
			QUANT_METRIC_TCHNOLOGY_CATEGORIES_HEADER,
			QUANT_METRIC_INDUSTRIES_HEADER,
			QUANT_METRIC_FORMULA_HEADER,
			QUANT_METRIC_FORMULA_DATA_HEADER
		).build();
		return new CSVPrinter(writer, csvFormat);
	}

	/**
	 * Insert data from CSV file
	 */
	@Transactional
	public ImportResultDTO importQuantMetricsFromCSVFile(Long riskModelId, InputStream fileContentStream) {
		ImportResultDTO result = new ImportResultDTO();
		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			if (csvParser.getHeaderMap().containsKey(QUANT_METRIC_NAME_HEADER)) {
				result = importQuantMetricFromCSVItems(riskModelId, csvRecordList);
			} else {
				throw new BadRequestException("Quant Metric Name header not found. Import Failed.");
			}

		} catch (IOException | ParseException e) {
			log.error("Failed to import Quant Metric", e);
		} catch (Exception e) {
			log.error("Failed to import Quant Metric due to global exception", e);
		}
		return result;
	}

	@Transactional
	public ImportResultDTO importQuantMetricFromCSVItems(Long riskModelId, List<CSVRecord> csvRecordList) throws ParseException {

		ImportResultDTO result = new ImportResultDTO();

		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, QuantMetricsEditDTO> quantMetricsMap = new HashMap<>();
		Map<Long, QuantMetricsEditDTO> quantMetricsToUpdateRefs = new HashMap<>();
		Map<Long, List<MetricFormulaItemViewDTO>> quantMetricsFormulasMap = new HashMap<>();
		for (CSVRecord csvRecord : csvRecordList) {
			// Accessing values by Header names
			String metricName = csvRecord.isMapped(QUANT_METRIC_NAME_HEADER) ? csvRecord.get(QUANT_METRIC_NAME_HEADER).trim() : null;
			String description = csvRecord.isMapped(QUANT_METRIC_DESCRIPTION_HEADER) ? csvRecord.get(QUANT_METRIC_DESCRIPTION_HEADER).trim() : null;
			String ordinal = csvRecord.isMapped(QUANT_METRIC_ORDINAL_HEADER) ? csvRecord.get(QUANT_METRIC_ORDINAL_HEADER).trim() : null;
			String quantName = csvRecord.isMapped(QUANT_METRIC_QUANT_NAME_HEADER) ? csvRecord.get(QUANT_METRIC_QUANT_NAME_HEADER).trim() : null;
			String metricLevel = csvRecord.isMapped(QUANT_METRIC_LEVEL_HEADER) ? csvRecord.get(QUANT_METRIC_LEVEL_HEADER).trim() : null;
			String regulations = csvRecord.isMapped(QUANT_METRIC_REGULATION_ACRONYMS_HEADER) ? csvRecord.get(QUANT_METRIC_REGULATION_ACRONYMS_HEADER) : null;
			String dataClasses = csvRecord.isMapped(QUANT_METRIC_DATA_TYPE_CLASSIFICATION_HEADER) ? csvRecord.get(QUANT_METRIC_DATA_TYPE_CLASSIFICATION_HEADER) : null;
			String technologyCategories = csvRecord.isMapped(QUANT_METRIC_TCHNOLOGY_CATEGORIES_HEADER) ? csvRecord.get(QUANT_METRIC_TCHNOLOGY_CATEGORIES_HEADER) : null;
			String industries = csvRecord.isMapped(QUANT_METRIC_INDUSTRIES_HEADER) ? csvRecord.get(QUANT_METRIC_INDUSTRIES_HEADER) : null;
			String metricFormula = csvRecord.isMapped(QUANT_METRIC_FORMULA_HEADER) ? csvRecord.get(QUANT_METRIC_FORMULA_HEADER) : null;
			String metricFormulaData = csvRecord.isMapped(QUANT_METRIC_FORMULA_DATA_HEADER) ? csvRecord.get(QUANT_METRIC_FORMULA_DATA_HEADER) : null;

			QuantMetrics quantMetricDetails = new QuantMetrics();

			if (StringUtils.isEmpty(metricName)) {
				result.getIgnored().add(new ItemViewDTO("Missing Quant Metric Name!"));
				continue;
			} else {
				List<QuantMetrics> existingQuantMetrics = quantMetricsRepository.getListByRiskModelAndName(riskModelId, metricName, Arrays.asList(0L), Pageable.ofSize(1000000));
				if (!existingQuantMetrics.isEmpty()) {
					for (QuantMetrics existingMetric : existingQuantMetrics) {
						if (existingMetric.getName().trim().equals(metricName)) {
							quantMetricDetails = existingMetric;
							break;
						}
					}
				}
			}
			QuantMetricsEditDTO quantMetricDTO = new QuantMetricsEditDTO();
			if (quantMetricDetails.getId() != null) {
				quantMetricDTO = new QuantMetricsEditDTO(quantMetricDetails);
			}
			quantMetricDTO.setName(metricName);

			if (StringUtils.isNotEmpty(description)) {
				quantMetricDTO.setDescription(description);
			} else {
				quantMetricDTO.setDescription("");
			}

			if (StringUtils.isNotEmpty(ordinal)) {
				try {
					quantMetricDTO.setOrdinal(Long.parseLong(ordinal));
				} catch (NumberFormatException e) {
					log.error("Unparseable ordinal", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, Unparseable ordinal: {1}", metricName, ordinal)));
					continue;
				}
			}

			if ((StringUtils.isNotEmpty(quantName)) && (quantsRepository.findFirstByName(quantName).isPresent())) {
				quantMetricDTO.setQuant(new QuantsRefDTO(quantsRepository.findFirstByName(quantName).get()));
			} else if (StringUtils.isNotEmpty(quantName)) {
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, No such Quant name: {1}", metricName, quantName)));
				continue;
			}


			try {
				if (StringUtils.isEmpty(metricLevel)) {
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, Missing metric level", metricName)));
					continue;
				}
				quantMetricDTO.setQuantMetricLevel(QuantMetricLevel.valueOf(metricLevel));
			} catch (Exception e) {
				log.error("Unparseable metric level", e);
				result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, No such metric level: {1}", metricName, metricLevel)));
				continue;
			}

			if (StringUtils.isNotEmpty(regulations)) {
				String[] regulationAcronyms = regulations.split(CSVUtils.LIST_SEPARATOR);
				List<RegulationRefDTO> regulationRefDTOs = new ArrayList<>();
				String wrongAcronym = "";
				for (String regulationAcronym : regulationAcronyms) {
					if (StringUtils.isEmpty(regulationAcronym.trim())) {
						continue;
					}
					Regulations regulationItem = regulationRepository.findFirstByAcronym(regulationAcronym).isPresent() ?
						regulationRepository.findFirstByAcronym(regulationAcronym).get() : null;
					if (regulationItem != null) {
						regulationRefDTOs.add(new RegulationRefDTO(regulationItem));
					} else {
						wrongAcronym = regulationAcronym;
						break;
					}
				}
				if (regulationAcronyms.length != regulationRefDTOs.size()) {
					if (StringUtils.isNotEmpty(wrongAcronym)) {
						result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, No such regulation: {1}", metricName, wrongAcronym)));
						continue;
					}
				}
				quantMetricDTO.setRegulations(regulationRefDTOs);
			}

			if (StringUtils.isNotEmpty(dataClasses)) {
				String[] dataTypeClassifications = dataClasses.split(CSVUtils.LIST_SEPARATOR);
				List<DataTypeClassificationViewDTO> dataTypeClassificationDTOs = new ArrayList<>();
				String wrongDataClass = "";
				for (String dataTypeClassification : dataTypeClassifications) {
					if (StringUtils.isEmpty(dataTypeClassification.trim())) {
						continue;
					}
					DataTypeClassification dataTypeClassificationItem = dataTypeClassificationRepository.findFirstByNameIgnoreCase(dataTypeClassification).isPresent() ?
						dataTypeClassificationRepository.findFirstByNameIgnoreCase(dataTypeClassification).get() : null;
					if (dataTypeClassificationItem != null) {
						dataTypeClassificationDTOs.add(new DataTypeClassificationViewDTO(dataTypeClassificationItem));
					} else {
						wrongDataClass = dataTypeClassification;
						break;
					}
				}
				if (dataTypeClassifications.length != dataTypeClassificationDTOs.size()) {
					if (StringUtils.isNotEmpty(wrongDataClass)) {
						result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, No such data class: {1}", metricName, wrongDataClass)));
						continue;
					}
				}
				quantMetricDTO.setDataTypeClassifications(dataTypeClassificationDTOs);
			}


			if (StringUtils.isNotEmpty(technologyCategories)) {
				String[] technologyCategoriesArray = technologyCategories.split(CSVUtils.LIST_SEPARATOR);
				List<TechnologyCategoryRefDTO> technologyCategoriesRefDTO = new ArrayList<>();
				String wrongTechnologyCategory = "";
				for (String technologyCategory : technologyCategoriesArray) {
					if (StringUtils.isEmpty(technologyCategory.trim())) {
						continue;
					}
					TechnologyCategories technologyCategoryItem = technologyCategoriesRepository.findFirstByNameIgnoreCase(technologyCategory).isPresent() ?
						technologyCategoriesRepository.findFirstByNameIgnoreCase(technologyCategory).get() : null;
					if (technologyCategoryItem != null) {
						technologyCategoriesRefDTO.add(new TechnologyCategoryRefDTO(technologyCategoryItem));
					} else {
						wrongTechnologyCategory = technologyCategory;
						break;
					}
				}
				if (technologyCategoriesArray.length != technologyCategoriesRefDTO.size()) {
					if (StringUtils.isNotEmpty(wrongTechnologyCategory)) {
						result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, No such technology category: {1}", metricName, wrongTechnologyCategory)));
						continue;
					}
				}
				quantMetricDTO.setTechnologyCategories(technologyCategoriesRefDTO);
			}


			if (StringUtils.isNotEmpty(industries)) {
				String[] industriesArray = industries.split(CSVUtils.LIST_SEPARATOR);
				List<IndustryRefDTO> industriesRefDTO = new ArrayList<>();
				String wrongIndustry = "";
				for (String industry : industriesArray) {
					if (StringUtils.isEmpty(industry.trim())) {
						continue;
					}
					Industries industryItem = industryRepository.findFirstByNameIgnoreCase(industry).isPresent() ?
						industryRepository.findFirstByNameIgnoreCase(industry).get() : null;
					if (industryItem != null) {
						industriesRefDTO.add(new IndustryRefDTO(industryItem));
					} else {
						wrongIndustry = industry;
						break;
					}
				}
				if (industriesArray.length != industriesRefDTO.size()) {
					if (StringUtils.isNotEmpty(wrongIndustry)) {
						result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0}, No such industry: {1}", metricName, wrongIndustry)));
						continue;
					}
				}
				quantMetricDTO.setIndustries(industriesRefDTO);
			}


			List<MetricFormulaItemViewDTO> formulaItems = new ArrayList<>();
			List<MetricFormulaItemViewDTO> formulaItemClone = new ArrayList<>();
			if (StringUtils.isNotEmpty(metricFormulaData)) {
				try {
					formulaItems = objectMapper.readValue(metricFormulaData, new com.fasterxml.jackson.core.type.TypeReference<List<MetricFormulaItemViewDTO>>(){});
					formulaItemClone = objectMapper.readValue(metricFormulaData, new com.fasterxml.jackson.core.type.TypeReference<List<MetricFormulaItemViewDTO>>(){});
					for (MetricFormulaItemViewDTO formulaItem : formulaItems) {
						formulaItem.setQuantMetricRef(null);
					}
				} catch (JsonProcessingException e) {
					log.error("Failed to convert formula items for the QUANT metric: {}", metricName);
				}
			} else if (StringUtils.isNotEmpty(metricFormula)) {
				try {
					formulaItems = parseFormula(metricFormula, quantMetricDetails);
					quantMetricDTO.setMetricFormulaItems(formulaItems);
				} catch (ItemNotFoundException e) {
					log.error("Unparseable formula", e);
					result.getIgnored().add(new ItemViewDTO(MessageFormat.format("{0} - " + e.getMessage(), metricName)));
					continue;
				}
			}

			QuantMetricsEditDTO quantMetricResult = null;
			if (quantMetricDetails.getId() != null) {
				// Compare existing formula in database with parsed formula:
				List<MetricFormulaItemViewDTO> itemsToDelete = new ArrayList<>();
				List<MetricFormulaItemViewDTO> oldFormulaItems = DTOBase.fromEntitiesList(quantMetricDetails.getMetricFormulaItems().stream().toList(), MetricFormulaItemViewDTO.class);
				oldFormulaItems.sort((o1, o2) -> Math.toIntExact(o1.getOrdinal() - o2.getOrdinal()));
				List<MetricFormulaItemViewDTO> newFormulaItems;
				if (CollectionUtils.isNotEmpty(formulaItems)) {
					newFormulaItems = formulaItems;
					quantMetricDTO.setMetricFormulaItems(formulaItems);
				} else {
					newFormulaItems = new ArrayList<>();
					itemsToDelete = oldFormulaItems;
					quantMetricDTO.setMetricFormulaItems(newFormulaItems);
				}

				/*
				if (!newFormulaItems.isEmpty() && oldFormulaItems.size() == newFormulaItems.size()) {

					int i = 0;
					for (oldFormulaItems.get(i); i <= oldFormulaItems.size() - 1; i++) {
						if (oldFormulaItems.get(i).getOrdinal() != null && oldFormulaItems.get(i).getOrdinal().equals(newFormulaItems.get(i).getOrdinal())
							&& ((oldFormulaItems.get(i).getValue() == null && newFormulaItems.get(i).getValue() == null)
							|| oldFormulaItems.get(i).getValue().equals(newFormulaItems.get(i).getValue()))
							&& ((oldFormulaItems.get(i).getOperation() == null && newFormulaItems.get(i).getOperation() == null)
							|| oldFormulaItems.get(i).getOperation().equals(newFormulaItems.get(i).getOperation()))
							&& ((oldFormulaItems.get(i).getVariableType() == null && newFormulaItems.get(i).getVariableType() == null)
							|| oldFormulaItems.get(i).getVariableType().equals(newFormulaItems.get(i).getVariableType()))) {

							quantMetricDTO.setMetricFormulaItems(oldFormulaItems);
						} else {

							quantMetricDTO.setMetricFormulaItems(newFormulaItems);
							itemsToDelete = oldFormulaItems;
							break;
						}
					}
				} else {
					itemsToDelete = oldFormulaItems;
				}
				 */

				quantMetricResult = update(quantMetricDTO);

				/*
				if (!itemsToDelete.isEmpty()) {
					itemsToDelete.forEach(item -> metricFormulaItemsRepository.deleteById(item.getId()));
				}
				*/
				result.getUpdated().add(new ItemViewDTO(quantMetricResult.getId(), quantMetricResult.getName()));
			} else {
				if (CollectionUtils.isNotEmpty(formulaItems)) quantMetricDTO.setMetricFormulaItems(formulaItems);
				quantMetricDTO.setRiskModelId(riskModelId);
				quantMetricResult = create(quantMetricDTO);
				result.getCreated().add(new ItemViewDTO(quantMetricResult.getId(), quantMetricResult.getName()));
			}

			quantMetricsMap.put(quantMetricResult.getName(), quantMetricResult);
			quantMetricsToUpdateRefs.put(quantMetricResult.getId(), quantMetricResult);
			quantMetricsFormulasMap.put(quantMetricResult.getId(), formulaItemClone);

		}

		for (Map.Entry<Long, List<MetricFormulaItemViewDTO>> formulaItemEntry : quantMetricsFormulasMap.entrySet()) {
			final QuantMetricsEditDTO quantMetricDTO = quantMetricsToUpdateRefs.get(formulaItemEntry.getKey());
			List<MetricFormulaItemViewDTO> formulaItems = formulaItemEntry.getValue();
			for (final MetricFormulaItemViewDTO formulaItem : formulaItems) {
				if (formulaItem.getQuantMetricRef() != null && formulaItem.getQuantMetricRef().getName() != null) {
					Optional<MetricFormulaItemViewDTO> quantFormulaWithRefDTO = quantMetricDTO.getMetricFormulaItems().stream().filter(metricFormulaItemViewDTO -> metricFormulaItemViewDTO.getOrdinal().equals(formulaItem.getOrdinal())).findFirst();
					if (quantFormulaWithRefDTO.isPresent()) {
						MetricFormulaItems formulaItemDetails = metricFormulaItemsRepository.findById(quantFormulaWithRefDTO.get().getId()).get();
						QuantMetricsEditDTO metricDTO = quantMetricsMap.get(formulaItem.getQuantMetricRef().getName());
						if (metricDTO != null) {
							formulaItemDetails.setQuantMetricId(quantMetricDTO.getId());
							formulaItemDetails.setQuantMetricRefId(metricDTO.getId());
							metricFormulaItemsRepository.save(formulaItemDetails);
						}
					}

				}
			}
		}

		result.setStatus("SUCCESS");
		return result;
	}

	public List<MetricFormulaItemViewDTO> parseFormula(String formula, QuantMetrics quantMetric) {
		if (StringUtils.isEmpty(formula)) {
			throw new ServerException("Missing formula!", ApplicationExceptionCodes.EMPTY_FORMULA);
		}
		List<MetricFormulaItemViewDTO> result = new ArrayList<>();
		List<String> stringFormulaItems = new ArrayList<>();
		Pattern pattern = Pattern.compile("\\s");
		//Remove extra spaces
		formula = formula.replaceAll("\\s{2,}", " ");

		String[] formulaItemsWithoutCommas = pattern.split(formula);

		// Commas separating
		List<String> formulaItems = new ArrayList<>();
		for (String formulaItemWithoutCommas : formulaItemsWithoutCommas) {
			if (StringUtils.right(formulaItemWithoutCommas, 1).equals(",")) {
				formulaItems.add(formulaItemWithoutCommas.substring(0, formulaItemWithoutCommas.length() - 1));
				formulaItems.add(",");
			} else formulaItems.add(formulaItemWithoutCommas);
		}

		// Bracket separating
		while (true) {
			List<String> formulaItemsWithoutBrackets = formulaItems;

			List<String> formulaItemsPrevious = formulaItems;
			formulaItems = new ArrayList<>();
			for (String formulaItemWithoutBrackets : formulaItemsWithoutBrackets) {
				if (StringUtils.left(formulaItemWithoutBrackets, 1).equals("(")) {
					formulaItems.add("(");
					if (formulaItemWithoutBrackets.length() != 1) {
						formulaItems.add(formulaItemWithoutBrackets.substring(1));
					}
				} else if (StringUtils.left(formulaItemWithoutBrackets, 1).equals(")")) {
					formulaItems.add(")");
					if (formulaItemWithoutBrackets.length() != 1) {
						formulaItems.add(formulaItemWithoutBrackets.substring(1));
					}
				} else if (StringUtils.right(formulaItemWithoutBrackets, 1).equals(")")) {
					if (formulaItemWithoutBrackets.length() != 1) {
						formulaItems.add(formulaItemWithoutBrackets.substring(0, formulaItemWithoutBrackets.length() - 1));
					}
					formulaItems.add(")");
				} else if (StringUtils.right(formulaItemWithoutBrackets, 1).equals("(")) {
					if (formulaItemWithoutBrackets.length() != 1) {
						formulaItems.add(formulaItemWithoutBrackets.substring(0, formulaItemWithoutBrackets.length() - 1));
					}
					formulaItems.add("(");
				} else formulaItems.add(formulaItemWithoutBrackets);
			}
			if (formulaItemsPrevious.size() == formulaItems.size()) {
				break;
			}
		}

		// Formula items detaching
		int j = 0;
		for (String formulaItem : formulaItems) {
			if (j == formulaItems.size()) break;
			formulaItem = formulaItems.get(j);
			if (!formulaItem.matches("^[a-zA-Z]*$")) {
				stringFormulaItems.add(formulaItem);
				if (j < formulaItems.size() - 1) {
					j++;
				} else break;
				continue;
			}
			String formulaItemNameWithSpaces = formulaItem;
			for (int i = j; i < formulaItems.size() - 1; i++) {
				if (formulaItem.matches("^[a-zA-Z]*$")) {

					if (formulaItems.get(i + 1).matches("^[a-zA-Z]*$")) {
						formulaItemNameWithSpaces = formulaItemNameWithSpaces + "\s" + formulaItems.get(i + 1);
						if (j < formulaItems.size() - 1) {
							j++;
						}
					} else break;
				}
			}
			formulaItem = formulaItemNameWithSpaces;
			if (formulaItem != "") {
				stringFormulaItems.add(formulaItem);
			}
			j++;
		}

		// Formula items recognising
		int i = 0;
		for (String formulaItem : stringFormulaItems) {

			MetricFormulaItemViewDTO formulaItemViewDTO = new MetricFormulaItemViewDTO();

			formulaItemViewDTO.setOrdinal((long) i);
			i++;

			switch (formulaItem) {
				case "(" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.OPEN_BRACKET);
				}
				case ")" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.CLOSE_BRACKET);
				}
				case "+" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.PLUS);
				}
				case "-" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.MINUS);
				}
				case "*" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.MULTIPLY);
				}
				case "/" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.DIVIDE);
				}
				case "," -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.COMMA);
				}
				case "MIN" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.MIN);
				}
				case "MAX" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.MAX);
				}
				case "ABS" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.ABS);
				}
				case "MEDIAN" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.MEDIAN);
				}
				case "AVERAGE" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.AVERAGE);
				}
				case "MODE" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.MODE);
				}
				case "SQRT" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.SQRT);
				}
				case "SUM" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.SUM);
				}
				case "RAND" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.RAND);
				}
				case "POW" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.POWER);
				}
				case "EXP" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.EXPONENT);
				}
				case "LN" -> {
					formulaItemViewDTO.setIsOperation(true);
					formulaItemViewDTO.setOperation(VariableOperation.LOG);
				}
				default -> {
					formulaItemViewDTO.setIsOperation(false);
					if (formulaItem.matches("^[0-9.,\\[\\]]*$")) {
						pattern = Pattern.compile(",");
						String[] numberWithCommas = pattern.split(formulaItem);
						if (numberWithCommas.length > 1) {
							for (int k = numberWithCommas.length - 1; k >= 0; k--) {
								if ((k == numberWithCommas.length - 1) && (numberWithCommas[k].length() >= 3 && numberWithCommas[k].matches("^[0-9.\\[\\]]*$"))) {
									pattern = Pattern.compile("\\.");
									String[] lastDigits = pattern.split(numberWithCommas[k]);
									if (lastDigits.length > 2 || (lastDigits[0].length() != 3 && lastDigits[0].matches("^[0-9.]*$"))
									|| (lastDigits[0].length() != 4 && lastDigits[0].contains("["))) {
										break;
									}
									continue;
								}
								if ((k > 0 && numberWithCommas[k].matches("^[0-9.]*$") && numberWithCommas[k].length() != 3)
								|| (k > 0 && numberWithCommas[k].contains("]") && numberWithCommas[k].length() != 4)) {
									break;
								}
								if ((numberWithCommas[k].matches("^[0-9.]*$") && numberWithCommas[0].length() > 3)
								|| (numberWithCommas[k].contains("]") && numberWithCommas[0].length() > 4)) {
									break;
								}
								formulaItem = formulaItem.replaceAll(",", "");
							}
						}

						if (formulaItem.contains("[")) {
							formulaItem = formulaItem.replace("[", "");
							formulaItem = formulaItem.replace("]", "");
							if (result.get(i-2).getRiskModelConstantRef() == null) {
								try {
									result.get(i - 2).setValue(Double.parseDouble(formulaItem));
								} catch (NumberFormatException e) {
									throw new ItemNotFoundException("Unparseable item [" + formulaItem + "] in formula: " + formula);
								}
							}
							i--;
							continue;
						} else {
							try {
								formulaItemViewDTO.setValue(Double.parseDouble(formulaItem));
							} catch (NumberFormatException e) {
								throw new ItemNotFoundException("Unparseable item [" + formulaItem + "] in formula: " + formula);
							}

							VariableTypes variableType = variableTypesRepository.findByNameAndRelation("constant", VariableTypeRelation.QUANT_METRIC)
								.isPresent() ? variableTypesRepository.findByNameAndRelation("constant", VariableTypeRelation.QUANT_METRIC).get() : null;
							if (variableType != null) {
								formulaItemViewDTO.setVariableType(new ItemViewDTO<>(variableType.getId(), variableType.getName()));
							}
						}
					} else if (variableTypesRepository.findByNameAndRelation(formulaItem, VariableTypeRelation.QUANT_METRIC).isPresent()) {
						VariableTypes variableType = variableTypesRepository.findByNameAndRelation(formulaItem, VariableTypeRelation.QUANT_METRIC).get();
						formulaItemViewDTO.setVariableType(new ItemViewDTO<>(variableType.getId(), variableType.getName()));
					}  else if (metricFormulaItemsRepository.findByNameAndQuantMetricId(formulaItem, quantMetric.getId()).isPresent()) {
						formulaItemViewDTO.setName(formulaItem);
						VariableTypes variableType = variableTypesRepository.findByNameAndRelation("constant", VariableTypeRelation.QUANT_METRIC).isPresent() ?
							variableTypesRepository.findByNameAndRelation("constant", VariableTypeRelation.QUANT_METRIC).get() : new VariableTypes();
						formulaItemViewDTO.setVariableType(new ItemViewDTO<>(variableType.getId(), variableType.getName()));
					}   else if (riskModelConstantRepository.findByNameAndRiskModelId(formulaItem, quantMetric.getRiskModelId()).isPresent()) {
						VariableTypes variableType = variableTypesRepository.findByNameAndRelation("risk model constant", VariableTypeRelation.QUANT_METRIC).isPresent() ?
							variableTypesRepository.findByNameAndRelation("risk model constant", VariableTypeRelation.QUANT_METRIC).get() : new VariableTypes();
						formulaItemViewDTO.setVariableType(new ItemViewDTO<>(variableType.getId(), variableType.getName()));
						formulaItemViewDTO.setRiskModelConstantRef(new RiskModelConstantViewDTO(riskModelConstantRepository.findByNameAndRiskModelId(formulaItem, quantMetric.getRiskModelId()).get()));
					} else {
						throw new ItemNotFoundException("Unparseable item [" + formulaItem + "] in formula: " + formula);
					}
				}
			}
			result.add(formulaItemViewDTO);
		}
		if (formulaStructureAnalyze(result)) {
			return result;
		} else throw new ItemNotFoundException("Unparseable formula" + formula);
	}


	boolean formulaStructureAnalyze(List<MetricFormulaItemViewDTO> formulaItems) throws ItemNotFoundException {
		StringBuilder formula = new StringBuilder();
		//Build formula string
		for (MetricFormulaItemViewDTO formulaItem : formulaItems) {
			if (formulaItem.getIsOperation()) {
				switch (formulaItem.getOperation()) {
					case OPEN_BRACKET -> formula.append("(");
					case CLOSE_BRACKET -> formula.append(")");
					case PLUS -> formula.append("+");
					case MINUS -> formula.append("-");
					case MULTIPLY -> formula.append("*");
					case DIVIDE -> formula.append("/");
					case COMMA -> formula.append(",");
					case MIN -> formula.append("MIN");
					case MAX -> formula.append("MAX");
					case ABS -> formula.append("ABS");
					case MEDIAN -> formula.append("MEDIAN");
					case AVERAGE -> formula.append("AVERAGE");
					case MODE -> formula.append("MODE");
					case SQRT -> formula.append("SQRT");
					case SUM -> formula.append("SUM");
					case RAND -> formula.append("RAND");
					case POWER -> formula.append("POW");
					case EXPONENT -> formula.append("EXP");
					case LOG -> formula.append("LN");
				}
			} else if (formulaItem.getValue() != null && !formulaItem.getValue().toString().isEmpty()) {
				formula.append(formulaItem.getValue());
			} else formula.append(formulaItem.getVariableType().getName());
			if (formulaItem.getOrdinal() < formulaItems.size() - 1) {
				formula.append(" ");
			}
		}

		VariableOperation[] pairOperations = {VariableOperation.PLUS, VariableOperation.MINUS, VariableOperation.MULTIPLY, VariableOperation.DIVIDE};
		VariableOperation[] needBracketsOperations = {VariableOperation.MAX, VariableOperation.MIN, VariableOperation.ABS, VariableOperation.SUM,
			VariableOperation.MODE, VariableOperation.RAND, VariableOperation.SQRT, VariableOperation.AVERAGE, VariableOperation.MEDIAN, VariableOperation.POWER,
		VariableOperation.EXPONENT, VariableOperation.LOG};
		VariableOperation comma = VariableOperation.COMMA;
		VariableOperation openBracket = VariableOperation.OPEN_BRACKET;
		VariableOperation closeBracket = VariableOperation.CLOSE_BRACKET;
		// Brackets structure check
		List<Long> openBracketOrdinals = new ArrayList<>();
		List<Long> closeBracketOrdinals = new ArrayList<>();
		for (MetricFormulaItemViewDTO resultItem : formulaItems) {
			if (resultItem.getOperation() != null) {
				if (resultItem.getOperation().equals(openBracket)) {
					openBracketOrdinals.add(resultItem.getOrdinal());
				}
				if (resultItem.getOperation().equals(closeBracket)) {
					closeBracketOrdinals.add(resultItem.getOrdinal());
				}
			}
		}
		Long openBracketOrdinalsSum = 0L;
		for (Long openBracketOrdinal : openBracketOrdinals) {
			openBracketOrdinalsSum += openBracketOrdinal;
		}
		Long closeBracketOrdinalsSum = 0L;
		for (Long closeBracketOrdinal : closeBracketOrdinals) {
			closeBracketOrdinalsSum += closeBracketOrdinal;
		}
		if (openBracketOrdinals.size() != closeBracketOrdinals.size() || closeBracketOrdinalsSum < openBracketOrdinalsSum) {
			throw new ItemNotFoundException("Wrong brackets in formula: " + formula);
		}

		//first item check
		if (formulaItems.get(0).getIsOperation() && (ArrayUtils.contains(pairOperations, formulaItems.get(0).getOperation()) ||
			formulaItems.get(0).getOperation().equals(comma))) {
			throw new ItemNotFoundException("Wrong first item " + formulaItems.get(0).getOperation() + " in formula: " + formula);
		}
		//Last item check
		if ((formulaItems.get(formulaItems.size() - 1).getIsOperation()) &&
			(!formulaItems.get(formulaItems.size() - 1).getOperation().equals(VariableOperation.CLOSE_BRACKET))) {
			throw new ItemNotFoundException("Wrong last item " + formulaItems.get(formulaItems.size() - 1).getOperation() + " in formula: " + formula);
		}
		//Other items check
		for (int i = 1; i < formulaItems.size() - 1; i++) {
			if (formulaItems.get(i).getIsOperation()) {
				// Min & Max check
				if (ArrayUtils.contains(needBracketsOperations, formulaItems.get(i).getOperation())) {
					if (formulaItems.get(i + 1).getOperation() == null || !formulaItems.get(i + 1).getOperation().equals(openBracket)) {
						throw new ItemNotFoundException("Missing \"(\" after " + formulaItems.get(i).getOperation() + " in formula: " + formula);
					}
				}
				//Comma check
				if (formulaItems.get(i).getOperation().equals(comma) &&
					((formulaItems.get(i + 1).getIsOperation() && (formulaItems.get(i + 1).getOperation().equals(closeBracket))) ||
						(formulaItems.get(i + 1).getIsOperation() && ArrayUtils.contains(pairOperations, formulaItems.get(i + 1).getOperation())))) {
					throw new ItemNotFoundException("Wrong item after comma in formula: " + formula);
				}
				//Open bracket check
				if (formulaItems.get(i).getOperation().equals(openBracket) &&
					(formulaItems.get(i + 1).getIsOperation() && ((formulaItems.get(i + 1).getOperation().equals(closeBracket)) ||
						ArrayUtils.contains(pairOperations, formulaItems.get(i + 1).getOperation())))) {
					throw new ItemNotFoundException("Wrong item after open bracket in formula: " + formula);
				}
				//Close bracket check
				if (formulaItems.get(i).getOperation().equals(closeBracket) && (!formulaItems.get(i + 1).getIsOperation() ||
					(formulaItems.get(i + 1).getIsOperation() && (ArrayUtils.contains(needBracketsOperations, formulaItems.get(i + 1).getOperation())) ||
						formulaItems.get(i + 1).getOperation().equals(openBracket)))) {
					throw new ItemNotFoundException("Wrong item after close bracket in formula: " + formula);
				}
				//Pair operations check
				if (ArrayUtils.contains(pairOperations, formulaItems.get(i).getOperation()) && ((formulaItems.get(i + 1).getIsOperation() &&
					formulaItems.get(i + 1).getOperation().equals(closeBracket)) || (formulaItems.get(i + 1).getIsOperation() &&
					ArrayUtils.contains(pairOperations, formulaItems.get(i + 1).getOperation())))) {
					throw new ItemNotFoundException("Wrong item after " + formulaItems.get(i).getOperation() + " in formula: " + formula);
				}
				// Constants & variables check
			} else if (!formulaItems.get(i + 1).getIsOperation() || (formulaItems.get(i + 1).getIsOperation() &&
				(formulaItems.get(i + 1).getOperation().equals(openBracket) || ArrayUtils.contains(needBracketsOperations, formulaItems.get(i + 1).getOperation())))) {
				String stringItem;
				if (formulaItems.get(i).getValue() != null) {
					stringItem = formulaItems.get(i).getValue().toString();
				} else {
					stringItem = formulaItems.get(i).getVariableType().getName();
				}
				throw new ItemNotFoundException("Wrong item after " + stringItem + " in formula: " + formula);
			}
		}
		return true;
	}
}
