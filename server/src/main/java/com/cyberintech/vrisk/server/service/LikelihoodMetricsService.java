package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.BaseFilter;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.likelihood.LikelihoodMetricsEditDTO;
import com.cyberintech.vrisk.server.model.dto.likelihood.LikelihoodMetricsViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.LikelihoodMetricsRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskTypeRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Likelihood Metrics management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-08
 */
@Service
public class LikelihoodMetricsService {

	@Autowired
	private LikelihoodMetricsRepository likelihoodMetricsRepository;

	@Autowired
	private RiskTypeRepository riskTypeRepository;

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
	private RiskModelDomainService riskModelDomainService;

	@Autowired
	private UserService userService;

	/**
	 * Get Likelihood Metrics List
	 *
	 * @return Likelihood Metrics List
	 */
	public List<LikelihoodMetricsViewDTO> getListPaged() {
		List<LikelihoodMetrics> items = likelihoodMetricsRepository.findAll();

		List<LikelihoodMetricsViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, LikelihoodMetricsViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Likelihood Metric details
	 *
	 * @return Likelihood Metric Details
	 */
	public LikelihoodMetricsViewDTO getDetails(Long itemId) {

		LikelihoodMetrics itemDetails = getLikelihoodMetric(itemId);

		LikelihoodMetricsViewDTO itemDTO = new LikelihoodMetricsViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Likelihood Metric details
	 *
	 * @return Likelihood Metric Details
	 */
	public LikelihoodMetrics getLikelihoodMetric(Long itemId) {
		LikelihoodMetrics itemDetails;

		try {
			itemDetails = likelihoodMetricsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Likelihood Metric not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Likelihood Metrics List inside current Risk Model
	 *
	 * @return Likelihood Metrics List
	 */
	public FilteredResponse<BaseFilter, LikelihoodMetricsViewDTO> getListByRiskModel(Long riskModelId, Pageable pageable) {

		RiskModels riskModel = riskModelService.getRiskModel(riskModelId);

		List<LikelihoodMetrics> items = likelihoodMetricsRepository.getListByRiskModelId(riskModelId, pageable);
		Long count = likelihoodMetricsRepository.getCountByRiskModelId(riskModelId);
		List<LikelihoodMetricsViewDTO> itemDTOs = LikelihoodMetricsViewDTO.fromEntitiesList(items, LikelihoodMetricsViewDTO.class);

		FilteredResponse<BaseFilter, LikelihoodMetricsViewDTO> result = FilteredResponse.of(pageable);
		result.setItems(itemDTOs);
		result.setTotal(Optional.ofNullable(count).orElse(0l).intValue());

		return result;
	}

	/**
	 * Create new Likelihood Metric
	 *
	 * @return New Likelihood Metric
	 */
	public LikelihoodMetricsViewDTO create(LikelihoodMetricsEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
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

//		LikelihoodMetrics newItem = newItemDTO.toEntity();
		LikelihoodMetrics newItem = new LikelihoodMetrics();
		// if (newItem.getRiskTypes() != null) newItem.getRiskTypes().clear(); // Clear Risk types as it is not usable to get there outside JPA
		newItem.setRiskModelId(riskModel.getId());
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
		LikelihoodMetrics saveResult = likelihoodMetricsRepository.save(newItem);

		// Save risk types
		likelihoodMetricsRepository.flush();
		saveResult = likelihoodMetricsRepository.findById(saveResult.getId()).get();

		LikelihoodMetricsViewDTO result = new LikelihoodMetricsViewDTO(saveResult);

		return result;
	}

	/**
	 * Update Likelihood Metrics
	 *
	 * @return Updated Likelihood Metrics
	 */
	public LikelihoodMetricsViewDTO update(LikelihoodMetricsEditDTO itemDTO) {

		LikelihoodMetricsViewDTO result;

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		LikelihoodMetrics existingItem = getLikelihoodMetric(itemDTO.getId());

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
		LikelihoodMetrics saveResult = likelihoodMetricsRepository.save(existingItem);

		// Save risk types
		saveResult = likelihoodMetricsRepository.findById(saveResult.getId()).get();

		result = new LikelihoodMetricsViewDTO(saveResult);

		return result;
	}

	/**
	 * Deletes Likelihood Metric
	 *
	 * @return ID of removed item
	 */
	public Long delete(Long itemId) {

		LikelihoodMetrics existingItem = getLikelihoodMetric(itemId);

		likelihoodMetricsRepository.delete(existingItem);

		return itemId;
	}

}
