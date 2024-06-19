package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.BaseFilter;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.metric_risk.MetricRisksEditDTO;
import com.cyberintech.vrisk.server.model.dto.metric_risk.MetricRisksViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.MetricDomainRepository;
import com.cyberintech.vrisk.server.repository.jpa.MetricRisksRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

/**
 * Metric Risks management Service. Implements basic metrics CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-22
 */
@Service
public class MetricRisksService {

	@Autowired
	private MetricRisksRepository metricRisksRepository;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private QualitativeQuestionService qualitativeQuestionService;

	@Autowired
	private QuestionWeightService questionWeightService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private RiskTypeService riskTypeService;

	@Autowired
	private UserService userService;

	@Autowired
	private MetricDomainRepository metricDomainRepository;

	/**
	 * Get Metric Risks List
	 *
	 * @return Metric Risks List
	 */
	public List<MetricRisksViewDTO> getListPaged() {
		List<MetricRisks> items = metricRisksRepository.findAll();

		List<MetricRisksViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, MetricRisksViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Metric Risk details
	 *
	 * @return Metric Risk Details
	 */
	public MetricRisksViewDTO getDetails(Long itemId) {

		MetricRisks itemDetails = getEntity(itemId);

		MetricRisksViewDTO itemDTO = new MetricRisksViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Metric Risk details
	 *
	 * @return Metric Risk Details
	 */
	public MetricRisks getEntity(Long itemId) {
		MetricRisks itemDetails;

		try {
			itemDetails = metricRisksRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Metric Risk not found in the database [{0}]", itemId), ApplicationExceptionCodes.METRIC_RISK_NOT_EXISTS);
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Metric Risks List inside current Risk Model
	 *
	 * @return Metric Risks List
	 */
	public FilteredResponse<BaseFilter, MetricRisksViewDTO> getListByRiskModel(Long riskModelId, Collection<Long> metricDomainIds, Pageable pageable) {

		RiskModels riskModel = riskModelService.getRiskModel(riskModelId);

		List<MetricRisks> items = metricRisksRepository.getListByRiskModelId(riskModelId, metricDomainIds, pageable);
		Long count = metricRisksRepository.getCountByRiskModelId(riskModelId, metricDomainIds);
		List<MetricRisksViewDTO> itemDTOs = MetricRisksViewDTO.fromEntitiesList(items, MetricRisksViewDTO.class);

		FilteredResponse<BaseFilter, MetricRisksViewDTO> result = FilteredResponse.of(pageable);
		result.setItems(itemDTOs);
		result.setTotal(Optional.ofNullable(count).orElse(0l).intValue());

		return result;
	}

	/**
	 * Create new Metric Risk
	 *
	 * @return New Metric Risk
	 */
	public MetricRisksViewDTO create(MetricRisksEditDTO newItemDTO) {

		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()), ApplicationExceptionCodes.CREATE_IS_NOT_ALLOWED_FOR_ITEM_WITH_EXISTING_ID);
		}

		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());
		QualitativeQuestions question = qualitativeQuestionService.getQualitativeQuestion(newItemDTO.getQuestion().getId());
		QuestionWeights questionWeight = questionWeightService.getQuestionWeight(newItemDTO.getQuestionWeight().getId());
		Organizations vendor = null;
		if (newItemDTO.getVendor() != null && newItemDTO.getVendor().getId() != null) {
			vendor = organizationService.getOrganization(newItemDTO.getVendor().getId());
		}
		RiskTypes riskType = null;
		if (newItemDTO.getRiskType() != null && newItemDTO.getRiskType().getId() != null) {
			riskType = riskTypeService.getRiskType(newItemDTO.getRiskType().getId());
		}

//		MetricRisks newItem = newItemDTO.toEntity();
		MetricRisks newItem = new MetricRisks();
		newItem.setRiskModelId(riskModel.getId());
		newItem.setMetricDomain(null);
		if (newItemDTO.getMetricDomainId() != null) {
			newItem.setMetricDomainId(newItemDTO.getMetricDomainId());
		} else {
			MetricDomains metricDomain = metricDomainRepository.findFirstByCodeIgnoreCase(newItemDTO.getMetricDomainCode())
				.orElseThrow(() -> new ItemNotFoundException(MessageFormat.format("Metric domain code not found [{0}]", newItemDTO.getMetricDomainCode()), ApplicationExceptionCodes.METRIC_DOMAIN_CODE_NOT_EXISTS));
			newItem.setMetricDomainId(metricDomain.getId());
		}
		newItem.setQuestion(question);
		newItem.setQuestionWeight(questionWeight);
		if (VendorType.Both.equals(question.getVendorType()) || VendorType.System.equals(question.getVendorType())) {
			newItem.setRiskType(riskType);
		}
		if (VendorType.Both.equals(question.getVendorType()) || VendorType.Vendor.equals(question.getVendorType())) {
			newItem.setVendor(vendor);
		}
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setUpdatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());
		MetricRisks saveResult = metricRisksRepository.save(newItem);

		// Save risk types
		metricRisksRepository.flush();
		saveResult = metricRisksRepository.findById(saveResult.getId()).get();

		MetricRisksViewDTO result = new MetricRisksViewDTO(saveResult);

		return result;
	}

	/**
	 * Update Metric Risks
	 *
	 * @return Updated Metric Risks
	 */
	public MetricRisksViewDTO update(MetricRisksEditDTO itemDTO) {

		MetricRisksViewDTO result;

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		MetricRisks existingItem = getEntity(itemDTO.getId());

		// Verify Risk Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());
		QualitativeQuestions question = qualitativeQuestionService.getQualitativeQuestion(itemDTO.getQuestion().getId());
		QuestionWeights questionWeight = questionWeightService.getQuestionWeight(itemDTO.getQuestionWeight().getId());
		Organizations vendor = null;
		if (itemDTO.getVendor() != null && itemDTO.getVendor().getId() != null) {
			vendor = organizationService.getOrganization(itemDTO.getVendor().getId());
		}
		RiskTypes riskType = null;
		if (itemDTO.getRiskType() != null && itemDTO.getRiskType().getId() != null) {
			riskType = riskTypeService.getRiskType(itemDTO.getRiskType().getId());
		}

		existingItem.setQuestion(question);
		existingItem.setQuestionWeight(questionWeight);
		if (VendorType.Both.equals(question.getVendorType()) || VendorType.System.equals(question.getVendorType())) {
			existingItem.setRiskType(riskType);

			if (VendorType.System.equals(question.getVendorType())) {
				existingItem.setVendor(null);
			}
		}
		if (VendorType.Both.equals(question.getVendorType()) || VendorType.Vendor.equals(question.getVendorType())) {
			existingItem.setVendor(vendor);

			if (VendorType.Vendor.equals(question.getVendorType())) {
				existingItem.setRiskType(null);
			}
		}

		// Update item details
		existingItem.setUpdatedBy(userService.getCurrentUserEntity());
		existingItem.setUpdatedAt(new Date());

		// Save to the database
		MetricRisks saveResult = metricRisksRepository.save(existingItem);

		// Save risk types
		saveResult = metricRisksRepository.findById(saveResult.getId()).get();

		result = new MetricRisksViewDTO(saveResult);

		return result;
	}

	/**
	 * Deletes Metric
	 *
	 * @return ID of removed item
	 */
	public Long delete(Long itemId) {

		MetricRisks existingItem = getEntity(itemId);

		metricRisksRepository.delete(existingItem);

		return itemId;
	}

}
