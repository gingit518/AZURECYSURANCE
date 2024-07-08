package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.contract.ContractDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Contract;
import com.cyberintech.vrisk.server.model.jpa.entity.Documents;
import com.cyberintech.vrisk.server.repository.jpa.ContractRepository;
import com.cyberintech.vrisk.server.repository.jpa.DocumentsRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * Contracts management Service. Implements basic CRUD.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2023-05-11
 */
@Service
@Slf4j
public class ContractService {

	@Autowired
	private ContractRepository contractRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private DocumentService documentService;
	@Qualifier("organizationService")
	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Contract List for current Filters
	 *
	 * @return Contract List
	 */
	public FilteredResponse<NameFilter, ContractDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<Contract> items = null;
		Long count = 0l;
		String namePattern = "";
		if (filteredRequest.getFilter() != null && StringUtils.isNotEmpty(filteredRequest.getFilter().getName())) {
			namePattern = filteredRequest.getFilter().getName();
		}
		items = contractRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = contractRepository.getCountByName(namePattern);

		List<ContractDTO> itemsDTOList = DTOBase.fromEntitiesList(items, ContractDTO.class);

		FilteredResponse<NameFilter, ContractDTO> filteredResponse = new FilteredResponse<>(filteredRequest);
		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());
		filteredResponse.setSort(filteredRequest.getSort());

		return filteredResponse;
	}


	/**
	 * Get Contract itemId
	 *
	 * @return Contract Details
	 */
	public Contract getItem(Long itemId) {
		Contract itemDetails;
		try {
			itemDetails = contractRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Contract not found in the database [{0}]", itemId));
		}
		return itemDetails;
	}


	/**
	 * Get Contract details
	 *
	 * @return Contract DTO Details
	 */
	public ContractDTO getDetails(Long itemId) {

		Contract itemDetails = getItem(itemId);
		ContractDTO result = new ContractDTO(itemDetails);

		if (result.getDocument() != null) {
			String downloadUrl = documentService.buildDownloadUrl(result.getDocument());
			result.getDocument().setDownloadUrl(downloadUrl);
		}

		if (CollectionUtils.isNotEmpty(result.getDocuments())) {
			for (DocumentDTO document : result.getDocuments()) {
				String downloadUrl = documentService.buildDownloadUrl(document);
				document.setDownloadUrl(downloadUrl);
			}

		}

		return result;
	}

	/**
	 * Create new Contract Item
	 *
	 * @return new Contract Item
	 */
	public ContractDTO create(ContractDTO contractDTO) {
		// Throw Exception if ID is set in create mode
		if (contractDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", contractDTO.getId()));
		}
		// Verify Number of Contract
		if (StringUtils.isEmpty(contractDTO.getNumber()) || StringUtils.isEmpty(contractDTO.getNumber().trim())) {
			throw new BadRequestException("Contract Number cannot be blank and must be unique.");
		}
		/*
		if (contractRepository.findFirstByNumber(contractDTO.getNumber().trim()).isPresent()) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. The Database already contains Contract with number [{0}] ", contractDTO.getNumber().trim()));
		}
		*/
		Contract contract = new Contract();
		contract.setOrganization(organizationService.getCurrentOrganizationEntity());
		applyEntityChanges(contractDTO, contract);
		contract.setCreatedAt(new Date());
		contract.setCreatedBy(userService.getCurrentUserEntity());
		contractRepository.save(contract);

		return new ContractDTO(contract);
	}

	/**
	 * Apply Contract entity changes
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(ContractDTO itemDTO, Contract entity) {

		// Detach Vendor from existing contract.
		if (itemDTO.getVendor() != null) {
			detachVendor(itemDTO.getVendor());
		}

		if (itemDTO.getName() != null) {
			entity.setName(itemDTO.getName());
		} else entity.setName("");

		if (itemDTO.getDescription() != null) {
			entity.setDescription(itemDTO.getDescription());
		} else entity.setDescription(null);

		/*
		if (contractDTO.getOrganization() != null && contractDTO.getOrganization().getId() != null) {
				contract.setOrganization(organizationRepository.findById(contractDTO.getOrganization().getId()).get());
		} else contract.setOrganization(null);
		 */

		if (itemDTO.getVendor() != null && itemDTO.getVendor().getId() != null) {
				entity.setVendor(organizationRepository.findById(itemDTO.getVendor().getId()).get());
		} else entity.setVendor(null);

		// TODO Remove obsolete dependencies
		if (itemDTO.getDocument() != null) {
			entity.setDocument(documentsRepository.findById(itemDTO.getDocument().getId()).get());
		} else entity.setDocument(null);


		// Set GDPR Documents Mapping
		Optional.ofNullable(itemDTO.getDocuments()).ifPresent(itemsList -> {
			entity.setDocuments(new HashSet<>());
			itemsList.forEach(item -> {
				Optional<Documents> documentOpt = documentsRepository.findByIdAndOrganizationId(item.getId(), entity.getOrganization().getId());
				documentOpt.ifPresent(documents -> entity.getDocuments().add(documents));
			});
		});

		if (itemDTO.getNumber() != null) {
			entity.setNumber(itemDTO.getNumber());
		} else entity.setNumber("");

		if (itemDTO.getStartDate() != null) {
			entity.setStartDate(itemDTO.getStartDate());
		} else entity.setStartDate(null);

		if (itemDTO.getExpiryDate() != null) {
			entity.setExpiryDate(itemDTO.getExpiryDate());
		} else entity.setExpiryDate(null);

		entity.setUpdatedAt(new Date());
		entity.setUpdatedBy(userService.getCurrentUserEntity());
	}

	/**
	 * Detach Vendor from existing contract
	 *
	 * @param vendor
	 */
	public void detachVendor(OrganizationRefDTO vendor) {

		Optional<Contract> vendorOpt = contractRepository.findByVendorId(vendor.getId());
		if (vendorOpt.isPresent()) {
			Contract existingContract = vendorOpt.get();
			existingContract.setVendor(null);
			contractRepository.save(existingContract);
		}
	}

	/**
	 * Detach Vendor from existing contract
	 *
	 * @param vendor
	 */
	public void applyVendor(OrganizationRefDTO vendor, Long contractId) {

		Optional<Contract> contractOpt = contractRepository.findById(contractId);

		Optional<Contract> vendorContractOpt = contractRepository.findByVendorId(vendor.getId());
		if (vendorContractOpt.isPresent() && contractOpt.isPresent() && !vendorContractOpt.get().getId().equals(contractId)) {
			Contract existingContract = vendorContractOpt.get();
			existingContract.setVendor(null);
			contractRepository.save(existingContract);
		}

		if (contractOpt.isPresent()) {
			Contract contract = contractOpt.get();
			contract.setVendor(organizationRepository.findById(vendor.getId()).get());
			contractRepository.save(contract);
		}
	}

	/**
	 * Update Contract Item
	 *
	 * @return Updated Contract Item
	 */
	public ContractDTO update(ContractDTO contractDTO) {

		// Verify Number of Contract
		Optional<Contract> existingContract = contractRepository.findFirstByNumber(contractDTO.getNumber().trim());
		if (!contractDTO.getNumber().isBlank() && existingContract.isPresent() && !existingContract.get().getId().equals(contractDTO.getId())) {
			throw new ConflictException(MessageFormat.format("Conflict while update item. The Database already contains Contract with number [{0}] ", contractDTO.getNumber().trim()));
		}

		Contract contract = contractRepository.findById(contractDTO.getId()).get();
		applyEntityChanges(contractDTO, contract);
		contractRepository.save(contract);

		return new ContractDTO(contract);

	}

	/**
	 * Deletes Contract Item
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Contract existingItem = getItem(itemId);
		contractRepository.delete(existingItem);
		contractRepository.flush();

		return itemId;
	}


}
