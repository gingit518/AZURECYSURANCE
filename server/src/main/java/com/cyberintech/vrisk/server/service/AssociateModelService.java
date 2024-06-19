package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.associate_models.AssociateModelEditDTO;
import com.cyberintech.vrisk.server.model.dto.associate_models.AssociateModelViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AssociateModels;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.AssociateModelRepository;
import com.cyberintech.vrisk.server.repository.jpa.QualMetricsRepository;
import com.cyberintech.vrisk.server.repository.jpa.QuantMetricsRepository;
import com.cyberintech.vrisk.server.repository.jpa.QuantsRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * Associate Models management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class AssociateModelService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private AssociateModelRepository associateModelRepository;

	@Autowired
	private QuantMetricsRepository quantMetricsRepository;

	@Autowired
	private QuantsRepository quantsRepository;

	@Autowired
	private QualMetricsRepository qualMetricsRepository;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private UserService userService;

	/**
	 * Get Associate Models List
	 *
	 * @return Associate Models List
	 */
	public List<AssociateModelViewDTO> getList() {
		List<AssociateModels> items = associateModelRepository.findAll();

		List<AssociateModelViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, AssociateModelViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Associate Models List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, AssociateModelViewDTO> getListFiltered(Long riskModelId, FilteredRequest<NameFilter> filteredRequest) {
		List<AssociateModels> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, AssociateModelViewDTO> filteredResponse = new FilteredResponse<NameFilter, AssociateModelViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = associateModelRepository.getListByRiskModelIdAndName(riskModelId, namePattern, filteredRequest.toPageRequest());
		count = associateModelRepository.getCountByRiskModelIdAndName(riskModelId, namePattern);

		List<AssociateModelViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, AssociateModelViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Associate Model details
	 *
	 * @return Associate Model Details
	 */
	public AssociateModels getAssociateModel(Long itemId) {
		AssociateModels itemDetails;

		try {
			itemDetails = associateModelRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Associate Model not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Associate Model DTO details
	 *
	 * @return Associate Model Details
	 */
	public AssociateModelEditDTO getDetails(Long itemId) {

		AssociateModels itemDetails = getAssociateModel(itemId);

		AssociateModelEditDTO result = new AssociateModelEditDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Associate Model Domain
	 *
	 * @return New Associate Model
	 */
	public AssociateModelEditDTO create(AssociateModelEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());

//		AssociateModels newItem = newItemDTO.toEntity();
		AssociateModels newItem = new AssociateModels();
		newItem.setRiskModelId(riskModel.getId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		AssociateModels saveResult = associateModelRepository.save(newItem);

		AssociateModelEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.ASSOCIATE_MODELS,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, riskModel.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Associate Model
	 *
	 * @return Updated Qualitative Domains
	 */
	public AssociateModelEditDTO update(AssociateModelEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		AssociateModels existingItem = getAssociateModel(itemDTO.getId());
		AssociateModelEditDTO existingItemDTO = new AssociateModelEditDTO(existingItem);

		// Verify Associate Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		AssociateModels saveResult = associateModelRepository.save(existingItem);

		AssociateModelEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.ASSOCIATE_MODELS,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, riskModel.getOrganizationId())
		);

		return result;
	}

	/**
	 * Apply question changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(AssociateModelEditDTO itemDTO, AssociateModels entity) {

		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());

		// Set Qual Metrics
		Optional.ofNullable(itemDTO.getQualMetrics()).ifPresent(qualMetricsViewDTOList -> {
			entity.setQualMetrics(new HashSet<>());
			qualMetricsViewDTOList.stream().forEach(qualMetricViewDTO -> {
				entity.getQualMetrics().add(qualMetricsRepository.findById(qualMetricViewDTO.getId()).get());
			});
		});

		// Set Quant Metrics
		Optional.ofNullable(itemDTO.getQuantMetrics()).ifPresent(quantsViewDTOList -> {
			entity.setQuantMetrics(new HashSet<>());
			quantsViewDTOList.stream().forEach(quantViewDTO -> {
				entity.getQuantMetrics().add(quantsRepository.findById(quantViewDTO.getId()).get());
			});
		});

		if (itemDTO.getOwner() != null && itemDTO.getOwner().getId() != null) {
			Users owner = userService.getOrganizationUser(itemDTO.getOwner().getId());
			entity.setOwner(owner);
		}

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Associate Model
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		AssociateModels existingItem = getAssociateModel(itemId);

		// Verify Associate Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());
		AssociateModelEditDTO existingItemDTO = new AssociateModelEditDTO(existingItem);
		associateModelRepository.delete(existingItem);
		associateModelRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.ASSOCIATE_MODELS,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, riskModel.getOrganizationId())
		);

		return itemId;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(AssociateModelEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getOwner() != null) logItems.add(AuditLogItemId.of(VItemType.OWNER_USER, existingItemDTO.getOwner().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
