package com.cyberintech.vrisk.server.service;


import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.formulas.FormulaViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_metrics.RiskMetricsViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.Formulas;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskMetrics;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.repository.jpa.RiskMetricsRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

/**
 * Risk Metrics management Service. Implements basic user CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-14
 */
@Service
public class RiskMetricsService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private RiskMetricsRepository riskMetricsRepository;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Risk Metrics List
	 *
	 * @return Risk Metrics List
	 */
	public List<RiskMetricsViewDTO> getList() {
		List<RiskMetrics> items = riskMetricsRepository.findAll();

		List<RiskMetricsViewDTO> itemDTOs = RiskMetricsViewDTO.fromEntitiesList(items, RiskMetricsViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Risk Metrics List
	 *
	 * @return Risk Metrics List
	 */
	public FilteredResponse<NameFilter, RiskMetricsViewDTO> getListFiltered(Long riskModelId, FilteredRequest<NameFilter> filteredRequest) {
		List<RiskMetrics> items = null;
		Long count = 0L;
		FilteredResponse<NameFilter, RiskMetricsViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = riskMetricsRepository.getListByRiskModelAndName(riskModelId, namePattern, filteredRequest.toPageRequest());
		count = riskMetricsRepository.getCountByRiskModelAndName(riskModelId, namePattern);

		List<RiskMetricsViewDTO> itemsDTOList = RiskMetricsViewDTO.fromEntitiesList(items, RiskMetricsViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Risk Metric details
	 *
	 * @return Risk Metric Details
	 */
	public RiskMetricsViewDTO getDetails(Long itemId) {

		RiskMetrics itemDetails = getRiskMetric(itemId);

		RiskMetricsViewDTO itemDTO = new RiskMetricsViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Risk Metric details
	 *
	 * @return Risk Metric Details
	 */
	public RiskMetrics getRiskMetric(Long itemId) {

		RiskMetrics itemDetails;

		try {
			itemDetails = riskMetricsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Risk Metric not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());
		if (!organizationService.getCurrentOrganizationId().equals(riskModel.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Risk Model of Risk Metric Item [{0}] doesn't match your organization [{1}]", riskModel.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Risk Metrics List
	 *
	 * @return Risk Metrics List
	 */
	public List<RiskMetricsViewDTO> getListByRiskModel(Long riskModelId) {

		List<RiskMetrics> items = riskMetricsRepository.getListByRiskModelId(riskModelId);

		List<RiskMetricsViewDTO> itemDTOs = RiskMetricsViewDTO.fromEntitiesList(items, RiskMetricsViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Create new Risk Metric
	 *
	 * @return New Risk Metric
	 */
	@Transactional
	public RiskMetricsViewDTO create(RiskMetricsViewDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		boolean updateResidual = Boolean.TRUE.equals(newItemDTO.getIsResidual());

		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());

//		RiskMetrics newItem = newItemDTO.toEntity();
		RiskMetrics newItem = new RiskMetrics();
		newItem.setRiskModelId(riskModel.getId());
		newItem.setIsResidual(false);
		newItem.setCreatedAt(new Date());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		applyEntityChanges(newItemDTO, newItem);

		RiskMetrics saveResult = riskMetricsRepository.save(newItem);

		RiskMetricsViewDTO result = new RiskMetricsViewDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.RISK_METRIC,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, riskModel.getOrganizationId())
		);

		// Update other Residual items for this Risk Model
		if (updateResidual) {
			riskMetricsRepository.resetResidualForRiskModel(saveResult.getRiskModelId(), saveResult.getId());
		}

		return result;
	}

	/**
	 * Update Risk Metric
	 *
	 * @return Updated Risk Metric
	 */
	@Transactional
	public RiskMetricsViewDTO update(RiskMetricsViewDTO itemDTO) {

		// Get Existing item from the database
		RiskMetrics existingItem = getRiskMetric(itemDTO.getId());
		RiskMetricsViewDTO existingItemDTO = new RiskMetricsViewDTO(existingItem);

		// Verify Risk Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());
		if (!organizationService.getCurrentOrganizationId().equals(riskModel.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Risk Model of Risk Metric Item [{0}] doesn't match your organization [{1}]", riskModel.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		boolean updateResidual = (Boolean.TRUE.equals(itemDTO.getIsResidual()) && !Boolean.TRUE.equals(existingItem.getIsResidual()));

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		RiskMetrics saveResult = riskMetricsRepository.save(existingItem);

		RiskMetricsViewDTO result = new RiskMetricsViewDTO(saveResult);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.RISK_METRIC,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, riskModel.getOrganizationId())
		);

		// Update other Residual items for this Risk Model
		if (updateResidual) {
			riskMetricsRepository.resetResidualForRiskModel(existingItem.getRiskModelId(), existingItem.getId());
		}

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(RiskMetricsViewDTO itemDTO, RiskMetrics entity) {

		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		if (itemDTO.getIsResidual() != null) {
			entity.setIsResidual(itemDTO.getIsResidual());
		}

		// Set Formula
		if (itemDTO.getFormula() != null) {
			if (itemDTO.getFormula().getId() != null) {
				FormulaViewDTO updatedFormula = formulaService.update(itemDTO.getFormula());
				Formulas formula = formulaService.getFormulaForCurrentOrganization(updatedFormula.getId());
				entity.setFormula(formula);
			} else {
				FormulaViewDTO createdFormula = formulaService.create(itemDTO.getFormula());
				Formulas formula = formulaService.getFormulaForCurrentOrganization(createdFormula.getId());
				entity.setFormula(formula);
			}
		}

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(RiskMetricsViewDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Deletes Risk Metric Item
	 *
	 * @param itemId
	 * @return Id of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		RiskMetrics existingItem = getRiskMetric(itemId);
		RiskMetricsViewDTO existingItemDTO = new RiskMetricsViewDTO(existingItem);
		riskMetricsRepository.delete(existingItem);
		riskMetricsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.RISK_METRIC,
			existingItemDTO.getId(),
			existingItemDTO,
			null
		);

		return itemId;
	}



}
