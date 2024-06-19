package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.external_analytics.ExternalAnalyticsDTO;
import com.cyberintech.vrisk.server.model.dto.external_analytics.ExternalAnalyticsViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * External Analytics business logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@Service
public class ExternalAnalyticsService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private ExternalAnalyticsRepository externalAnalyticsRepository;

	@Autowired
	private ExternalAnalyticsParametersRepository externalAnalyticsParametersRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private DocumentService documentService;

	/**
	 * Get External Analytics List
	 *
	 * @return External Analytics List
	 */
	public List<ExternalAnalyticsDTO> getList() {

		List<ExternalAnalytics> items = externalAnalyticsRepository.findAll();

		List<ExternalAnalyticsDTO> itemDTOs = DTOBase.fromEntitiesList(items, ExternalAnalyticsDTO.class);

		return itemDTOs;
	}

	/**
	 * Get External Analytics List
	 *
	 * @return External Analytics List
	 */
	public List<ExternalAnalyticsViewDTO> getSelfListRandom() {

		Long organizationId = organizationService.getCurrentOrganizationId();
		Users user = userService.getCurrentUserEntity();
		List<Long> userRoles = user.getRoles().stream().map(Roles::getId).collect(Collectors.toList());

		// List<ExternalAnalytics> externalAnalyticsItems = externalAnalyticsRepository.findAllByOrganizationId(organizationId);
		List<ExternalAnalytics> items = externalAnalyticsRepository.getRandomListByRoles(userRoles, organizationId, 6l);

		List<ExternalAnalyticsViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, ExternalAnalyticsViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Data Items List filtered
	 *
	 * @return Items List
	 */
	public FilteredResponse<NameFilter, ExternalAnalyticsDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		FilteredResponse<NameFilter, ExternalAnalyticsDTO> filteredResponse = new FilteredResponse<NameFilter, ExternalAnalyticsDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		List<ExternalAnalytics> items;
		Long count;
		Long organizationId = organizationService.getCurrentOrganizationId();

		if (organizationId != null) {
			items = externalAnalyticsRepository.getListByNameAndOrganization(namePattern, organizationId, filteredRequest.toPageRequest());
			count = externalAnalyticsRepository.getCountByNameAndOrganization(namePattern, organizationId);
		} else if (userService.isSuperAdmin()) {
			items = externalAnalyticsRepository.getListByName(namePattern, filteredRequest.toPageRequest());
			count = externalAnalyticsRepository.getCountByName(namePattern);
		} else {
			throw new BadRequestException("Organization is not defined. Please contact support.");
		}

		List<ExternalAnalyticsDTO> itemsDTOList = ExternalAnalyticsDTO.fromEntitiesList(items, ExternalAnalyticsDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get External Analytics details
	 *
	 * @param itemId
	 * @return External Analytics Details
	 */
	public ExternalAnalyticsDTO getDetails(Long itemId) {
		ExternalAnalytics itemDetails = getItem(itemId);

		ExternalAnalyticsDTO result = new ExternalAnalyticsDTO(itemDetails);

		return result;
	}

	/**
	 * Get External Analytics details
	 *
	 * @param itemId
	 * @return External Analytics Details
	 */
	public ExternalAnalytics getItem(Long itemId) {
		ExternalAnalytics itemDetails;

		try {
			itemDetails = externalAnalyticsRepository.findById(itemId).get();

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("External Analytics not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Create new External Analytics
	 *
	 * @param newItemDTO
	 * @return New External Analytics Details
	 */
	public ExternalAnalyticsDTO create(ExternalAnalyticsDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		Long organizationId = organizationService.getCurrentOrganizationId();
		if (organizationId == null && userService.isSuperAdmin() && newItemDTO.getOrganization() != null) organizationId = newItemDTO.getOrganization().getId();

		ExternalAnalytics newItem = new ExternalAnalytics();
		newItem.setOrganizationId(organizationId);

		applyEntityChanges(newItemDTO, newItem);

		// Save to the database
		ExternalAnalytics savedResult = externalAnalyticsRepository.save(newItem);

		ExternalAnalyticsDTO result = new ExternalAnalyticsDTO(savedResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.EXTERNAL_ANALYTICS,
			savedResult.getId(),
			result
		);

		return result;
	}

	/**
	 * Update External Analytics
	 *
	 * @param itemDTO
	 * @return Updated External Analytics Details
	 */
	public ExternalAnalyticsDTO update(ExternalAnalyticsDTO itemDTO) {

		// Get Existing item from the database
		ExternalAnalytics existingItem = getItem(itemDTO.getId());
		ExternalAnalyticsDTO existingItemDTO = new ExternalAnalyticsDTO(existingItem);

		// Verify Item and Organization
		if (!userService.isSuperAdmin() && !organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for External Analytics Item [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Updating organization
		if (userService.isSuperAdmin() && itemDTO.getOrganization() != null && itemDTO.getOrganization().getId() != null) {
			existingItem.setOrganizationId(itemDTO.getOrganization().getId());
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		ExternalAnalytics savedResult = externalAnalyticsRepository.save(existingItem);

		ExternalAnalyticsDTO result = new ExternalAnalyticsDTO(savedResult);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.EXTERNAL_ANALYTICS,
			savedResult.getId(),
			existingItemDTO,
			result
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(ExternalAnalyticsDTO itemDTO, ExternalAnalytics entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setExternalAnalyticsType(itemDTO.getExternalAnalyticsType());
		if (itemDTO.getIsPublic() != null) entity.setIsPublic(itemDTO.getIsPublic());

		// Update External Analytics Parameters
		Optional.ofNullable(itemDTO.getExternalAnalyticsParameters()).ifPresent(itemsList -> {
			entity.setExternalAnalyticsParameters(new HashSet<>());
			itemsList.stream().forEach(itemRef -> {
				if (itemRef.getId() != null) {
					entity.getExternalAnalyticsParameters().add(externalAnalyticsParametersRepository.findById(itemRef.getId()).get());
				} else {
					ExternalAnalyticsParameters parameter = new ExternalAnalyticsParameters();
					parameter.setName(itemRef.getName());
					parameter.setValue(itemRef.getValue());
					parameter.setOrganizationId(entity.getOrganizationId());
					ExternalAnalyticsParameters newItem = externalAnalyticsParametersRepository.save(parameter);
					entity.getExternalAnalyticsParameters().add(newItem);
				}
			});
		});

		// Set Role-based Access
		if (itemDTO.getRoles() != null) {
			Optional.ofNullable(itemDTO.getRoles()).ifPresent(rolesList -> {
				entity.setRoles(new HashSet<>());
				itemDTO.getRoles().stream().forEach(role -> {
					entity.getRoles().add(roleRepository.findById(role.getId()).get());
				});
			});
		}

		// Manage Logo document
		if (itemDTO.getLogoDocument() != null) {
			Documents logoDocument = documentsRepository.findById(itemDTO.getLogoDocument().getId()).get();
			entity.setLogoDocument(logoDocument);

			DocumentDTO logoDocumentDTO = new DocumentDTO(logoDocument, true);
			entity.setLogo(documentService.buildStorageUrl(logoDocumentDTO));
		} else if (Boolean.TRUE.equals(itemDTO.getRemoveLogo())) {
			entity.setLogoDocument(null);
		}
	}

	/**
	 * Delete External Analytics
	 *
	 * @param itemId
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		ExternalAnalytics existingItem = getItem(itemId);
		ExternalAnalyticsDTO existingItemDTO = new ExternalAnalyticsDTO(existingItem);

		// Verify Item and Organization
		if (!userService.isSuperAdmin() && !organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for External Analytics Item [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		externalAnalyticsRepository.delete(existingItem);
		externalAnalyticsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.EXTERNAL_ANALYTICS,
			existingItemDTO.getId(),
			existingItemDTO
		);

		return itemId;
	}
}
