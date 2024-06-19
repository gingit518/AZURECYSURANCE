package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.AssociateVendorModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.associate_vendors.AssociateVendorEditDTO;
import com.cyberintech.vrisk.server.model.dto.associate_vendors.AssociateVendorViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.VendorViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AssociateVendors;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.AssociateVendorRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.repository.results.AssociateVendorResult;
import com.cyberintech.vrisk.server.rest.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.text.MessageFormat;
import java.util.*;

/**
 * Associate Vendors management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-15
 */
@Service
public class AssociateVendorService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	@Autowired
	private AssociateVendorModelDAO associateVendorModelDAO;

	/**
	 * Get Associate Vendors List
	 *
	 * @return Associate Vendors List
	 */
	public List<AssociateVendorViewDTO> getList() {
		List<AssociateVendors> items = associateVendorRepository.findAll();

		List<AssociateVendorViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, AssociateVendorViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Associate Vendors List
	 *
	 * @return Associate Vendors List
	 */
	public FilteredResponse<NameFilter, AssociateVendorResult> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {

		PagedResult<AssociateVendorResult> result = associateVendorModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<NameFilter, AssociateVendorResult> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Associate Vendor details
	 *
	 * @return Associate Vendor Details
	 */
	public AssociateVendors getAssociateVendorForCurrentOrganization(Long itemId) {
		AssociateVendors itemDetails;

		try {
			itemDetails = associateVendorRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Associate Vendor not found in the database [{0}]", itemId));
		}

		// Verify Associate Vendor and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Associate Vendor [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Associate Vendor DTO details
	 *
	 * @return Associate Vendor Details
	 */
	public AssociateVendorEditDTO getDetails(Long itemId) {

		AssociateVendors itemDetails = getAssociateVendorForCurrentOrganization(itemId);

		AssociateVendorEditDTO result = new AssociateVendorEditDTO(itemDetails);

		return result;
	}

	/**
	 * Create new Associate Vendor Domain
	 *
	 * @return New Associate Vendor
	 */
	public AssociateVendorEditDTO create(AssociateVendorEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		if (newItemDTO.getVendor() == null || newItemDTO.getVendor().getId() == null) {
			throw new BadRequestException("Vendor is required", ApplicationExceptionCodes.VENDOR_REQUIRED);
		}
		Long organizationId = organizationService.getCurrentOrganizationId();
		Organizations vendor = getVendor(newItemDTO.getVendor().getId());

		AssociateVendors newItem = associateVendorRepository.findByOrganizationIdAndVendor(organizationId, vendor).orElse(new AssociateVendors());

		if (newItem.getId() == null) {
			newItem.setOrganizationId(organizationId);
			newItem.setVendor(vendor);
			newItem.setCreatedBy(userService.getCurrentUserEntity());
			newItem.setCreatedAt(new Date());
		}
		applyEntityChanges(newItemDTO, newItem);
		AssociateVendors saveResult = associateVendorRepository.save(newItem);

		AssociateVendorEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.VENDOR_ASSOCIATED_SYSTEMS,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Associate Vendor
	 *
	 * @return Updated Associate Vendor
	 */
	public AssociateVendorEditDTO update(AssociateVendorEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		AssociateVendors existingItem = getAssociateVendorForCurrentOrganization(itemDTO.getId());
		AssociateVendorEditDTO existingItemDTO = new AssociateVendorEditDTO(existingItem);

		// Verify Business Unit and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Associate Vendor [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		AssociateVendors saveResult = associateVendorRepository.save(existingItem);

		AssociateVendorEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.VENDOR_ASSOCIATED_SYSTEMS,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, existingItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Create new Associate Vendor Domain
	 *
	 * @return New Associate Vendor
	 */
	public AssociateVendorEditDTO addSystemToVendor(Long vendorId, Long systemId) {

		Organizations vendor = getVendor(vendorId);
		Long organationId = organizationService.getCurrentOrganizationId();

		AssociateVendors associateVendor = associateVendorRepository.findByOrganizationIdAndVendor(organationId, vendor).orElse(new AssociateVendors());
		AssociateVendorEditDTO associateVendorDTO = null;
		if (associateVendor.getId() == null) {
			associateVendorDTO = new AssociateVendorEditDTO();
			associateVendorDTO.setVendor(new VendorViewDTO(vendor));
			associateVendorDTO.setSystems(new ArrayList<>());
		} else {
			associateVendorDTO = new AssociateVendorEditDTO(associateVendor);

			// Vendor / System Association exists - SKIPPING update
			Optional<Organizations> vendorOptional = associateVendorRepository.getSystemVendor(systemId, vendorId);
			if (vendorOptional.isPresent()) {
				return  associateVendorDTO;
			}
		}

		// Systems system = systemRepository.findById(systemId).get();
		associateVendorDTO.getSystems().add(SystemRefDTO.of(systemId, null));

		return associateVendorDTO.getId() != null ? update(associateVendorDTO) : create(associateVendorDTO);
	}

	/**
	 * Removes System from the Organization Vendor
	 *
	 * @return New Associate Vendor
	 */
	public AssociateVendorEditDTO removeSystemFromVendor(Long vendorId, Long systemId) {

		Organizations vendor = getVendor(vendorId);
		Long organizationId = organizationService.getCurrentOrganizationId();

		AssociateVendors associateVendor = associateVendorRepository.findByOrganizationIdAndVendor(organizationId, vendor).orElse(new AssociateVendors());
		AssociateVendorEditDTO associateVendorDTO = null;
		if (associateVendor.getId() == null) {
			return null;
		} else {
			associateVendorDTO = new AssociateVendorEditDTO(associateVendor);
		}

		Systems system = systemRepository.findById(systemId).get();
		associateVendorDTO.getSystems().remove(new SystemRefDTO(system));

		return update(associateVendorDTO);
	}

	/**
	 * Apply question changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(AssociateVendorEditDTO itemDTO, AssociateVendors entity) {
		// Set associated systems
		Optional.ofNullable(itemDTO.getSystems()).ifPresent(systemRefDTOList -> {
			entity.setSystems(new HashSet<>());
			systemRefDTOList.stream().forEach(systemRefDTO -> {
				entity.getSystems().add(systemRepository.findById(systemRefDTO.getId()).get());
			});
		});

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Associate Vendor
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		AssociateVendors existingItem = getAssociateVendorForCurrentOrganization(itemId);
		AssociateVendorEditDTO existingItemDTO = new AssociateVendorEditDTO(existingItem);
		associateVendorRepository.delete(existingItem);
		associateVendorRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.VENDOR_ASSOCIATED_SYSTEMS,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, existingItem.getOrganizationId())
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
	private AuditLogItemId[] collectAuditLogItems(AssociateVendorEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getVendor() != null) logItems.add(AuditLogItemId.of(VItemType.VENDOR, existingItemDTO.getVendor().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	private Organizations getVendor(Long id) {
		Long organizationId = organizationService.getCurrentOrganizationId();
		Optional<Organizations> vendorOpt = organizationRepository.getByIdAndOrganizationType(id, organizationId, OrganizationType.Vendor);
		if (vendorOpt.isEmpty()) {
			throw new ItemNotFoundException("Vendor is not found", ApplicationExceptionCodes.VENDOR_NOT_EXISTS);
		}
		return vendorOpt.get();
	}

}
