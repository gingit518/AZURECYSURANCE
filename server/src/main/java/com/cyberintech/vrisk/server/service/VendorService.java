package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.VendorEditDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.ReassignScoringToUserDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vendor management Service. Vendor is a kind of Organization, but we need to
 * move vendor-specific operations into the separate module.
 *
 * @author Andrii Iakovenko
 * @since 2022-08-19
 */
@Service("vendorService")
public class VendorService {

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private AssociateVendorService associateVendorService;

	@Autowired
	private AuditLogService auditLogService;

	@Lazy
	@Autowired
	private CyberRiskScoringService cyberRiskScoringService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Lazy
	@Autowired
	private TechnologyService technologyService;

	@Autowired
	private UserAssignedVendorRepository userAssignedVendorRepository;

	@Autowired
	private ContractService contractServise;

	@Autowired
	private ContractRepository contractRepository;

	/**
	 * Create new Organization
	 *
	 * @return New Organization
	 */
	public VendorEditDTO createVendor(VendorEditDTO newItemDTO) {

		Organizations currentOrganization = organizationService.getCurrentOrganizationEntity();

		// Trimming name
		newItemDTO.setName(StringUtils.trim(newItemDTO.getName()));

		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat
				.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()),
				ApplicationExceptionCodes.CREATE_IS_NOT_ALLOWED_FOR_ITEM_WITH_EXISTING_ID);
		}

		// Verify organization with such name not exists
		if (organizationRepository
			.findFirstByNameAndRootParentAndIdIsNotIn(newItemDTO.getName(), currentOrganization, Arrays.asList(0l))
			.isPresent()) {
			throw new ConflictException(MessageFormat
				.format("Vendor with this name already registered in the system [{0}]", newItemDTO.getName()),
				ApplicationExceptionCodes.ORGANIZATION_WITH_NAME_ALREADY_EXISTS);
		}

		Organizations newItem = new Organizations();
		newItem.setOrganizationType(OrganizationType.Vendor);
		newItem.setRootParent(organizationService.getCurrentOrganizationEntity());
		fillVendorEntityRelations(newItemDTO, newItem);
		Organizations saveResult = organizationRepository.save(newItem);

		// Saving contract for the Vendor
		if (newItemDTO.getContract() != null) {
			newItemDTO.getContract().setVendor(new OrganizationRefDTO(saveResult));
			contractServise.update(newItemDTO.getContract());
		}
		// Saving Associate Systems for the Vendor
		if (BooleanUtils.isTrue(newItemDTO.getIsSystemVendor())) {
			if (newItemDTO.getSystems() != null) {
				saveVendorAssociateSystems(saveResult.getId(), newItemDTO.getSystems());
			}
		} else {
			// No system should be associated. Remove all if any
			saveVendorAssociateSystems(saveResult.getId(), Collections.emptyList());
		}

		// Send User Assignment Notification to the User
		if (saveResult.getOwner() != null) {
			sendUserAssignmentNotification(saveResult);
		}

		VendorEditDTO result = new VendorEditDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.VENDOR,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getRootParent().getId()));

		return result;
	}

	/**
	 * Delete Vendor
	 *
	 * @return Removed Vendor
	 */
	@Transactional
	public OrganizationEditDTO deleteVendorById(Long itemId) {

		OrganizationEditDTO result;

		try {
			Long currentOrganizationId = organizationService.getCurrentOrganizationId();

			// Get Existing item from the database
			Organizations vendor = organizationRepository.findById(itemId).get();

			if (!vendor.getOrganizationType().equals(OrganizationType.Vendor)) {
				throw new BadRequestException(
					MessageFormat.format("Only Vendor Organization can be Deleted [{0}]", vendor.getName()),
					ApplicationExceptionCodes.ONLY_SUBSIDIARY_ORGANIZATION_CAN_BE_REMOVED);
			}

			if (!currentOrganizationId.equals(vendor.getRootParent().getId())) {
				throw new ForbiddenException("You are not allowed to remove this vendor in the Organization",
					ApplicationExceptionCodes.ACCESS_TO_ORGANIZATION_FORBIDDEN);
			}

			// Removing dependencies
			associateVendorRepository.deleteAllByVendor(vendor);
			questionAnswersForVendorRepository.deleteAllByVendor(vendor);
			userAssignedVendorRepository.deleteAllByVendor(vendor);

			// Detaching Vendor from Technologies. This is quite deprecated part as
			// Technologies now linked to vendors as MANY-TO-MANY
			technologyService.detachVendor(vendor);

			// Detaching Vendor from Contract
			contractServise.detachVendor(new OrganizationRefDTO(vendor));

			result = new OrganizationEditDTO(vendor);
			OrganizationEditDTO existingItemDTO = result;

			// Save to the database
			organizationRepository.delete(vendor);

			// Save Audit Log DELETE event
			auditLogService.delete(
				VItemType.VENDOR,
				existingItemDTO.getId(),
				existingItemDTO,
				collectAuditLogItems(existingItemDTO, vendor.getRootParent().getId()));
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Vendor not found in the system [{0}]", itemId),
				ApplicationExceptionCodes.VENDOR_NOT_EXISTS);
		} catch (ConstraintViolationException exception) {
			throw new ForbiddenException(MessageFormat
				.format("Failed to delete Vendor [{0}] because it has constraints with other data.", itemId),
				ApplicationExceptionCodes.VENDOR_CANNOT_BE_REMOVED);
		}

		return result;
	}

	/**
	 * Get Vendor organization Entity
	 *
	 * @param id
	 * @return
	 */
	public Organizations getVendor(Long id) {
		Organizations organization = organizationService.getOrganization(id);

		Long currenOrganizationId = organizationService.getCurrentOrganizationId();
		if (currenOrganizationId == null || !currenOrganizationId.equals(id)) {
			// Skip for Current Organization
			if (!organization.getOrganizationType().equals(OrganizationType.Vendor)) {
				throw new BadRequestException(
					MessageFormat.format("Only Vendor Organization can be Edited [{0}]", organization.getName()),
					ApplicationExceptionCodes.ONLY_VENDOR_ORGANIZATION_CAN_BE_UPDATED);
			}
		}

		return organization;
	}

	/**
	 * Get Vendor organization Details to edit
	 *
	 * @param id
	 * @return
	 */
	public VendorEditDTO getVendorDetails(Long id) {
		Organizations organization = organizationService.getOrganization(id);

		if (!organization.getOrganizationType().equals(OrganizationType.Vendor)) {
			throw new BadRequestException(
				MessageFormat.format("Only Vendor Organization can be Edited [{0}]", organization.getName()),
				ApplicationExceptionCodes.ONLY_VENDOR_ORGANIZATION_CAN_BE_UPDATED);
		}

		VendorEditDTO result = new VendorEditDTO(organization);

		List<Systems> associateSystems = associateVendorRepository.getSystemsListForVendor(id);
		if (CollectionUtils.isNotEmpty(associateSystems)) {
			result.setSystems(associateSystems.stream().map(SystemRefDTO::new).collect(Collectors.toList()));
		}

		return result;
	}

	/**
	 * Update Organization
	 *
	 * @return New Organization
	 */
	public VendorEditDTO updateVendor(VendorEditDTO itemDTO) {

		VendorEditDTO result;

		// Trimming name
		itemDTO.setName(StringUtils.trim(itemDTO.getName()));

		try {
			Organizations currentOrganization = organizationService.getCurrentOrganizationEntity();

			// Get Existing item from the database
			Organizations existingItem = organizationRepository.findById(itemDTO.getId()).get();

			if (!existingItem.getOrganizationType().equals(OrganizationType.Vendor)) {
				throw new BadRequestException(
					MessageFormat.format("Only Vendor Organization can be Edited [{0}]", itemDTO.getName()),
					ApplicationExceptionCodes.ONLY_VENDOR_ORGANIZATION_CAN_BE_UPDATED);
			}

			// Verify organization with such name not exists
			if (organizationRepository.findFirstByNameAndRootParentAndIdIsNotIn(itemDTO.getName(), currentOrganization,
				Arrays.asList(itemDTO.getId())).isPresent()) {
				throw new ConflictException(MessageFormat
					.format("Vendor with this name already registered in the system [{0}]", itemDTO.getName()),
					ApplicationExceptionCodes.ORGANIZATION_WITH_NAME_ALREADY_EXISTS);
			}

			// Save Previous Owner
			Users prevOwner = existingItem.getOwner();

			// Update item details
			Organizations updatedItem = existingItem;
			VendorEditDTO existingItemDTO = new VendorEditDTO(existingItem);

			fillVendorEntityRelations(itemDTO, updatedItem);

			// Save to the database
			Organizations saveResult = organizationRepository.save(updatedItem);

			// Saving Associate Systems for the Vendor
			if (BooleanUtils.isTrue(itemDTO.getIsSystemVendor())) {
				if (itemDTO.getSystems() != null) {
					saveVendorAssociateSystems(existingItem.getId(), itemDTO.getSystems());
				}
			} else {
				// No system should be associated. Remove all if any
				saveVendorAssociateSystems(existingItem.getId(), Collections.emptyList());
			}

			// Send notification to new vendor owner if changed
			if ((prevOwner != null && saveResult.getOwner() != null
				&& !prevOwner.getId().equals(saveResult.getOwner().getId()))
				|| (prevOwner == null && saveResult.getOwner() != null)) {
				sendUserAssignmentNotification(saveResult);
			}

			result = new VendorEditDTO(saveResult);

			// Save Audit Log UPDATE event
			auditLogService.update(
				VItemType.VENDOR,
				saveResult.getId(),
				existingItemDTO,
				result,
				collectAuditLogItems(result,
					(existingItem.getRootParent() != null ? existingItem.getRootParent().getId() : null)));

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(
				MessageFormat.format("Organization not found in the system [{0}]", itemDTO.getId()),
				ApplicationExceptionCodes.ORGANIZATION_NOT_EXISTS);
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
	protected AuditLogItemId[] collectAuditLogItems(OrganizationEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getOwner() != null)
			logItems.add(AuditLogItemId.of(VItemType.VENDOR_OWNER, existingItemDTO.getOwner().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Fill the set of entity relations
	 *
	 * @param itemDTO
	 * @param entity
	 */
	protected void fillVendorEntityRelations(VendorEditDTO itemDTO, Organizations entity) {
		organizationService.applyEntityChanges(itemDTO, entity);

		if (itemDTO.getIsCloudVendor() != null)
			entity.setIsCloudVendor(itemDTO.getIsCloudVendor());
		if (itemDTO.getIsServiceVendor() != null)
			entity.setIsServiceVendor(itemDTO.getIsServiceVendor());
		if (itemDTO.getIsSystemVendor() != null)
			entity.setIsSystemVendor(itemDTO.getIsSystemVendor());
		if (itemDTO.getIsTechnologyVendor() != null)
			entity.setIsTechnologyVendor(itemDTO.getIsTechnologyVendor());

		// Set Technologies
		Optional.ofNullable(itemDTO.getTechnologies()).ifPresent(technologiesRefDTOList -> {
			entity.setTechnologies(new HashSet<>());
			technologiesRefDTOList.stream().forEach(technologyRefDTO -> {
				entity.getTechnologies()
					.add(technologyService.getTechnologyForCurrentOrganization(technologyRefDTO.getId()));
			});
		});

		// Set Vendor Contract
		// TODO Fix contracts logic
		if (itemDTO.getContract() != null && itemDTO.getContract().getId() != null) {
			if (itemDTO.getId() != null) {
				Optional<Organizations> organizationOpt = organizationRepository.findById(itemDTO.getId());

				contractServise.applyVendor(new OrganizationRefDTO(entity), itemDTO.getContract().getId());
			}
		} else {
			contractServise.detachVendor(new OrganizationRefDTO(entity));
		}

	}

	private void saveVendorAssociateSystems(Long vendorId, List<SystemRefDTO> associateSystems) {
		List<Systems> originalSystems = associateVendorRepository.getSystemsListForVendor(vendorId);
		Set<Long> originalSystemIdSet = originalSystems.stream().mapToLong(Systems::getId).boxed()
			.collect(Collectors.toSet());
		Set<Long> newSystemIdSet = associateSystems.stream().mapToLong(SystemRefDTO::getId).boxed()
			.collect(Collectors.toSet());

		// Removing associate Systems
		for (Long systemId : originalSystemIdSet) {
			if (!newSystemIdSet.contains(systemId)) {
				associateVendorService.removeSystemFromVendor(vendorId, systemId);
			}
		}

		// Adding Systems to Vendors
		for (Long systemId : newSystemIdSet) {
			associateVendorService.addSystemToVendor(vendorId, systemId);
		}
	}

	/**
	 * Send User assignment notification
	 *
	 * @param vendor
	 */
	private void sendUserAssignmentNotification(Organizations vendor) {
		// Create Reassign DTO Details
		ReassignScoringToUserDTO reassignScoringToUser = new ReassignScoringToUserDTO();
		reassignScoringToUser.setItemType(VendorType.Vendor);
		reassignScoringToUser.setItem(new ItemViewDTO(vendor.getId(), vendor.getName()));
		reassignScoringToUser.setUser(new UserRefDTO(vendor.getOwner()));

		cyberRiskScoringService.reassignToUser(null, reassignScoringToUser);
	}
}
