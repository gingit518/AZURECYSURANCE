package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentTypeEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentTypeViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import com.cyberintech.vrisk.server.repository.jpa.AssessmentTypesRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Assessment Types management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class AssessmentTypeService {

	public static final String FRAMEWORK_NAME_NIST_SP_800_53 = "NIST SP 800-53";
	public static final String FRAMEWORK_NAME_NIST_CSF = "NIST CSF";
	public static final String FRAMEWORK_NAME_ISO_IEC_27001_2005 = "ISO/IEC 27001:2005";
	public static final String FRAMEWORK_NAME_ISO_IEC_27001_2013 = "ISO/IEC 27001:2013";
	public static final String FRAMEWORK_NAME_PCI_DSS = "PCI DSS";

	@Autowired
	private AssessmentTypesRepository assessmentTypesRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Assessment Types List
	 *
	 * @return Assessment Types List
	 */
	public List<AssessmentTypeViewDTO> getList() {
		List<AssessmentTypes> items = assessmentTypesRepository.findAll();

		List<AssessmentTypeViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, AssessmentTypeViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Assessment Types List
	 *
	 * @return Assessment Types List
	 */
	public FilteredResponse<NameFilter, AssessmentTypeViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<AssessmentTypes> items;
		Long count;
		FilteredResponse<NameFilter, AssessmentTypeViewDTO> filteredResponse = new FilteredResponse<NameFilter, AssessmentTypeViewDTO>(filteredRequest);

		List<Long> excludeIds = Arrays.asList(0L);
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getExcludeIds() != null && filteredRequest.getFilter().getExcludeIds().size() > 0) {
			excludeIds = filteredRequest.getFilter().getExcludeIds();
		}

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = assessmentTypesRepository.getListByOrganizationAndName(organizationId, namePattern, excludeIds, filteredRequest.toPageRequest());
		count = assessmentTypesRepository.getCountByOrganizationAndName(organizationId, namePattern, excludeIds);

		List<AssessmentTypeViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, AssessmentTypeViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Assessment Type details
	 *
	 * @return Assessment Type Details
	 */
	public AssessmentTypes getAssessmentTypeForCurrentOrganization(Long itemId) {
		AssessmentTypes itemDetails;

		itemDetails = assessmentTypesRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(MessageFormat.format("Assessment Type not found in the database [{0}]", itemId)));

		// Verify Assessment Type and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Assessment Type [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Ensure that framework exists or create new otherwise
	 *
	 * @param frameworkName
	 * @return
	 */
	public AssessmentTypes ensureFramework(String frameworkName) {
		Long organizationId = organizationService.getCurrentOrganizationId();

		AssessmentTypes result = null;
		Optional<AssessmentTypes> frameworkOptional = assessmentTypesRepository.findFirstByNameIgnoreCaseAndOrganizationId(frameworkName, organizationId);
		if (frameworkOptional.isEmpty()) {
			AssessmentTypeEditDTO newItemDTO = new AssessmentTypeEditDTO();
			newItemDTO.setName(frameworkName);
			newItemDTO.setOrganizationId(organizationId);
			AssessmentTypeEditDTO createResult = create(newItemDTO);
			result = assessmentTypesRepository.findById(createResult.getId()).get();
		} else {
			result = frameworkOptional.get();
		}

		return result;
	}

	/**
	 * Get Assessment Type DTO details
	 *
	 * @return Assessment Type Details
	 */
	public AssessmentTypeEditDTO getDetails(Long itemId) {

		AssessmentTypes itemDetails = getAssessmentTypeForCurrentOrganization(itemId);

		AssessmentTypeEditDTO result = new AssessmentTypeEditDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Assessment Type Domain
	 *
	 * @return New Assessment Type
	 */
	public AssessmentTypeEditDTO create(AssessmentTypeEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		AssessmentTypes newItem = newItemDTO.toEntity();
		AssessmentTypes newItem = new AssessmentTypes();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		// newItem.setCreatedBy(userService.getCurrentUserEntity());
		// newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		AssessmentTypes saveResult = assessmentTypesRepository.save(newItem);

		return getDetails(saveResult.getId());
	}

	/**
	 * Update Assessment Type
	 *
	 * @return Updated Assessment Type
	 */
	public AssessmentTypeEditDTO update(AssessmentTypeEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		AssessmentTypes existingItem = getAssessmentTypeForCurrentOrganization(itemDTO.getId());

		// Verify Assessment Type and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Assessment Type [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		AssessmentTypes saveResult = assessmentTypesRepository.save(existingItem);

		return getDetails(saveResult.getId());
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(AssessmentTypeEditDTO itemDTO, AssessmentTypes entity) {
//		entity.setUpdatedBy(userService.getCurrentUserEntity());
//		entity.setUpdatedAt(new Date());
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
	}

	/**
	 * Deletes Assessment Type
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		AssessmentTypes existingItem = getAssessmentTypeForCurrentOrganization(itemId);
		assessmentTypesRepository.delete(existingItem);
		assessmentTypesRepository.flush();

		return itemId;
	}

}
