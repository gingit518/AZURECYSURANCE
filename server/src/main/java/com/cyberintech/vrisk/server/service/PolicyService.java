package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.policy.PolicyEditDTO;
import com.cyberintech.vrisk.server.model.dto.policy.PolicyReviewDTO;
import com.cyberintech.vrisk.server.model.dto.policy.PolicyReviewViewDTO;
import com.cyberintech.vrisk.server.model.dto.policy.PolicyViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.PolicyReviewType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Policies management Service. Implements basic user CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-01-08
 */
@Service
public class PolicyService {

	@Autowired
	private AssessmentTypesRepository assessmentTypesRepository;

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private CyberRoleService cyberRoleService;

	@Autowired
	private GDPRArticleItemRepository gdprArticleItemRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private PolicyRepository policyRepository;

	@Autowired
	private PolicyReviewsRepository policyReviewsRepository;

	@Autowired
	private PolicyStatementRepository policyStatementRepository;

	@Autowired
	private SecurityRequirementRepository securityRequirementRepository;

	@Autowired
	private UserService userService;

	/**
	 * Get Policies List
	 *
	 * @return Policies List
	 */
	public List<PolicyViewDTO> getList() {
		List<Policies> items = policyRepository.findAll();

		List<PolicyViewDTO> itemsDTOs = DTOBase.fromEntitiesList(items, PolicyViewDTO.class);

		return itemsDTOs;
	}

	/**
	 * Get Policies List
	 *
	 * @return Policies List
	 */
	public FilteredResponse<NameFilter, PolicyViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<Policies> items;
		Long count = 0L;
		FilteredResponse<NameFilter, PolicyViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = policyRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = policyRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<PolicyViewDTO> itemsDTOs = DTOBase.fromEntitiesList(items, PolicyViewDTO.class);

		filteredResponse.setItems(itemsDTOs);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Policy Item details
	 *
	 * @return Policy Item Detail
	 */
	public Policies getPolicyForCurrentOrganization(Long itemId) {
		Policies itemDetails;

		try {
			itemDetails = policyRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Policy Item not found in the database [{0}]", itemId), ApplicationExceptionCodes.POLICY_NOT_EXIST);
		}

		// Verify Policy Item and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Policy Item [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Policy Item DTO details
	 *
	 * @return Policy Item Details
	 */
	public PolicyEditDTO getDetails(Long itemId) {

		Policies itemDetails = getPolicyForCurrentOrganization(itemId);

		PolicyEditDTO result = new PolicyEditDTO(itemDetails);

		if (result.getDocument() != null) {
			String downloadUrl = documentService.buildDownloadUrl(result.getDocument());
			result.getDocument().setDownloadUrl(downloadUrl);
		}

		if (result.getSecurityRequirements() != null) {
			result.getSecurityRequirements();
		}

		return result;
	}

	/**
	 * Create new Policy Item
	 *
	 * @return New Policy Item
	 */
	public PolicyEditDTO create(PolicyEditDTO newItemDTO) {
		// Throw exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		Policies newItem = newItemDTO.toEntity();
		Policies newItem = new Policies();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		Policies saveResult = policyRepository.save(newItem);

		PolicyEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.POLICY,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Policy Item
	 *
	 * @return Updated Policy Item
	 */
	public PolicyEditDTO update(PolicyEditDTO itemDTO) {

		// Get Existing item from the database
		Policies existingItem = getPolicyForCurrentOrganization(itemDTO.getId());
		PolicyEditDTO existingItemDTO = new PolicyEditDTO(existingItem);

		// Verify Policy and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Policy [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// save to the database
		Policies saveResult = policyRepository.save(existingItem);

		PolicyEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.POLICY,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, existingItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(PolicyEditDTO itemDTO, Policies entity) {

		entity.setName(StringUtils.trim(itemDTO.getName()));
		entity.setVersion(StringUtils.defaultIfEmpty(itemDTO.getVersion(), ""));
		entity.setApprovedAt(itemDTO.getApprovedAt());
		entity.setAnnualReviewDate(itemDTO.getAnnualReviewDate());
		//entity.setSecurityRequirements(itemDTO.toEntity().getSecurityRequirements());

		entity.setOverview(itemDTO.getOverview());
		entity.setPurpose(itemDTO.getPurpose());
		entity.setScope(itemDTO.getScope());
		entity.setEnforcement(itemDTO.getEnforcement());
		entity.setExceptions(itemDTO.getExceptions());
		entity.setDefinitions(itemDTO.getDefinitions());

		if(itemDTO.getApprovedBy() != null && itemDTO.getApprovedBy().getId() != null) {
			Users approvedBy = userService.getOrganizationUser(itemDTO.getApprovedBy().getId());
			entity.setApprovedBy(approvedBy);
		}

		// ask should we copy this field or not
		if(itemDTO.getCreatedBy() != null && itemDTO.getCreatedBy().getId() != null) {
			Users createdBy = userService.getOrganizationUser(itemDTO.getCreatedBy().getId());
			entity.setCreatedBy(createdBy);
		}

		Optional.ofNullable(itemDTO.getAssessmentTypes()).ifPresent(assessmentTypesViewDTOList -> {
			entity.setAssessmentTypes(new HashSet<>());
			assessmentTypesViewDTOList.stream().forEach(assessmentTypeViewDTO -> {
				if (assessmentTypeViewDTO.getId() != null) {
					AssessmentTypes assessmentType = assessmentTypesRepository.findById(assessmentTypeViewDTO.getId()).get();
//					assessmentTypeViewDTO.toEntity(assessmentType);
					assessmentType.setName(assessmentTypeViewDTO.getName());
					assessmentType.setDescription(assessmentTypeViewDTO.getDescription());
					entity.getAssessmentTypes().add(assessmentType);
				} else {
					AssessmentTypes assessmentType = new AssessmentTypes();
					assessmentType.setName(assessmentTypeViewDTO.getName());
					assessmentType.setDescription(assessmentTypeViewDTO.getDescription());
					assessmentTypesRepository.save(assessmentType);
					entity.getAssessmentTypes().add(assessmentType);
				}
			});
		});

		Optional.ofNullable(itemDTO.getGdprArticles()).ifPresent(itemsList -> {
			entity.setGdprArticles(new HashSet<>());
			itemsList.stream().forEach(item -> {
				entity.getGdprArticles().add(gdprArticleItemRepository.findFirstByIdAndOrganizationId(item.getId(), entity.getOrganizationId()).get());
			});
		});

		Optional.ofNullable(itemDTO.getRolesAndResponsibilities()).ifPresent(itemsList -> {
			entity.setRolesAndResponsibilities(new HashSet<>());
			itemsList.stream().forEach(item -> {
				entity.getRolesAndResponsibilities().add(cyberRoleService.getCyberRoleForCurrentOrganization(item.getId()));
			});
		});

		Optional.ofNullable(itemDTO.getStatements()).ifPresent(itemsList -> {
			entity.setStatements(new HashSet<>());
			itemsList.stream().forEach(item -> {
				final PolicyStatements policyStatements = new PolicyStatements();
				policyStatements.setId(item.getId());
				policyStatements.setStatement(item.getStatement());
				policyStatements.setSecurityRequirements(new HashSet<>());
				item.getSecurityRequirements().stream().forEach(requirementDTO -> {
					policyStatements.getSecurityRequirements().add(securityRequirementRepository.findById(requirementDTO.getId()).get());
				});
				policyStatementRepository.save(policyStatements);

				entity.getStatements().add(policyStatements);
			});
		});

		if (itemDTO.getDocument() != null && itemDTO.getDocument().getId() != null) {
			Documents document = documentService.getItemForCurrentOrganization(itemDTO.getDocument().getId());
			entity.setDocument(document);

		} else {
			entity.setDocument(null);
		}

		Optional.ofNullable(itemDTO.getRelatedPolicies()).ifPresent(itemsList -> {
			entity.setRelatedPolicies(new HashSet<>());
			itemsList.stream().forEach(item -> {
				entity.getRelatedPolicies().add(getPolicyForCurrentOrganization(item.getId()));
			});
		});
	}

	/**
	 * Deletes Policy
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Policies existingItem = getPolicyForCurrentOrganization(itemId);
		PolicyEditDTO existingItemDTO = new PolicyEditDTO(existingItem);

		// Remove all related Policies
		if (CollectionUtils.isNotEmpty(existingItem.getRelatedPolicies())) {
			for (Policies relatedPolicy: existingItem.getRelatedPolicies()) {
				delete(relatedPolicy.getId());
			}
		}

		policyRepository.delete(existingItem);
		policyRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.POLICY,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, existingItem.getOrganizationId())
		);

		return itemId;
	}

	/**
	 * Get Policy Item DTO details
	 *
	 * @return Policy Item Details
	 */
	public PolicyReviewDTO getReviewDetails(Long itemId) {

		Policies itemDetails = getPolicyForCurrentOrganization(itemId);

		PolicyReviewDTO result = new PolicyReviewDTO(itemDetails);

		if (result.getDocument() != null) {
			String downloadUrl = documentService.buildDownloadUrl(result.getDocument());
			result.getDocument().setDownloadUrl(downloadUrl);
		}

		List<PolicyReviews> policyReviews = policyReviewsRepository.findAllByPolicyId(itemId);
		List<PolicyReviewViewDTO> policyReviewsDto = policyReviews.stream().map(PolicyReviewViewDTO::new).toList();
		result.setReviews(policyReviewsDto);

		return result;
	}

	/**
	 * Create new Policy Item
	 *
	 * @return New Policy Item
	 */
	public PolicyReviewDTO createReview(PolicyReviewDTO newItemDTO) {
		// Throw exception if ID is set in create mode
		if (newItemDTO.getId() == null) {
			throw new ItemNotFoundException("Review item required");
		}

		// Date currentDate = new Date();
		LocalDate currentDate = LocalDate.now();

		PolicyReviews policyReview = new PolicyReviews();
		policyReview.setPolicyId(newItemDTO.getId());
		policyReview.setOrganizationId(organizationService.getCurrentOrganizationId());
		policyReview.setNotes(newItemDTO.getReviewText());
		policyReview.setReviewType(PolicyReviewType.Annual);
		policyReview.setReviewerId(userService.getCurrentUser().getUserId());
		policyReview.setReviewDate(Date.from(currentDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
		policyReview.setReviewYear((long) currentDate.getYear());
		policyReview.setReviewMonth((long) currentDate.getMonthValue());
		policyReview.setReviewDay((long) currentDate.getDayOfMonth());

		policyReviewsRepository.save(policyReview);

		PolicyReviewDTO result = getReviewDetails(newItemDTO.getId());

		// Save Audit Log CREATE event
		/*
		auditLogService.create(
			VItemType.POLICY,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);
		*/

		return result;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(PolicyEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}
}
