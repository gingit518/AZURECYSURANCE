package com.cyberintech.vrisk.server.service;

import static com.cyberintech.vrisk.server.service.csv.ProcessCSVImporter.*;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.ProcessModelDAO;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.ProcessFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.process.ProcessEditDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import com.cyberintech.vrisk.server.model.jpa.entity.Processes;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.ProcessRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import com.cyberintech.vrisk.server.service.utils.ExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.*;

/**
 * Process management Service. Implements basic entity CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-28
 */
@Slf4j
@Service
public class ProcessService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private DataTypeClassificationService dataTypeClassificationService;

	@Autowired
	private DataAssetClassificationService dataAssetClassificationService;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private ProcessModelDAO processModelDAO;

	/**
	 * Get Process List
	 *
	 * @return Process List
	 */
	public List<ProcessViewDTO> getList() {
		List<Processes> items = processRepository.findAll();

		List<ProcessViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, ProcessViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Process List
	 *
	 * @return Users List
	 */
	public FilteredResponse<ProcessFilter, ProcessViewDTO> getListFiltered(FilteredRequest<ProcessFilter> filteredRequest) {

		PagedResult<ProcessViewDTO> result = processModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<ProcessFilter, ProcessViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Process details
	 *
	 * @return Process Details
	 */
	public Processes getProcessForCurrentOrganization(Long itemId) {
		Processes itemDetails;

		try {
			itemDetails = processRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Process not found in the database [{0}]", itemId));
		}

		// Verify Process and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Process [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Process DTO details
	 *
	 * @return Process Details
	 */
	public ProcessEditDTO getDetails(Long itemId) {

		Processes itemDetails = getProcessForCurrentOrganization(itemId);

		ProcessEditDTO result = new ProcessEditDTO(itemDetails);

		return result;
	}


	/**
	 * Updates revenue for the list of Processes
	 *
	 * @return Updated Processes
	 */
	public List<ProcessViewDTO> updateProcessRevenue(List<ProcessViewDTO> itemsList) {
		Users currentUser = userService.getCurrentUserEntity();

		List<ProcessViewDTO> result = new ArrayList<>();
		for (ProcessViewDTO itemDTO : itemsList) {

			Processes entity = getProcessForCurrentOrganization(itemDTO.getId());
			ProcessEditDTO existingItemDTO = new ProcessEditDTO(entity);

			// Save only if item was changed
			if (itemDTO.getRevenueProcessed() != null && !itemDTO.getRevenueProcessed().equals(existingItemDTO.getRevenueProcessed())) {
				entity.setRevenueProcessed(itemDTO.getRevenueProcessed());
				entity.setUpdatedAt(new Date());
				entity.setUpdatedBy(currentUser);
				processRepository.save(entity);

				// Save Audit Log UPDATE event
				ProcessEditDTO savedEntityDTO = new ProcessEditDTO(entity);
				auditLogService.update(
					VItemType.PROCESS,
					existingItemDTO.getId(),
					existingItemDTO,
					savedEntityDTO,
					collectAuditLogItems(savedEntityDTO, entity.getOrganizationId())
				);
			}

			result.add(itemDTO);
		}

		return result;
	}

	/**
	 * Create new Process Domain
	 *
	 * @return New Process
	 */
	public ProcessEditDTO create(ProcessEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		Processes newItem = newItemDTO.toEntity();
		Processes newItem = new Processes();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		Processes saveResult = processRepository.save(newItem);

		ProcessEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.PROCESS,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Process
	 *
	 * @return Updated Qualitative Domains
	 */
	public ProcessEditDTO update(ProcessEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		Processes existingItem = getProcessForCurrentOrganization(itemDTO.getId());
		ProcessEditDTO existingItemDTO = new ProcessEditDTO(existingItem);

		// Verify Process and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Process [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Processes saveResult = processRepository.save(existingItem);

		ProcessEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.PROCESS,
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
	private void applyEntityChanges(ProcessEditDTO itemDTO, Processes entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setNotes(itemDTO.getNotes());
		entity.setRevenueProcessed(itemDTO.getRevenueProcessed());

		if (itemDTO.getOwner() != null && itemDTO.getOwner().getId() != null) {
			Users owner = userService.getOrganizationUser(itemDTO.getOwner().getId());
			entity.setOwner(owner);
		}

		if (itemDTO.getBusinessUnit() != null && itemDTO.getBusinessUnit().getId() != null) {
			BusinessUnits businessUnit = businessUnitService.getBusinessUnitForCurrentOrganization(itemDTO.getBusinessUnit().getId());
			entity.setBusinessUnit(businessUnit);
		}

		// Set Business Units
		Optional.ofNullable(itemDTO.getBusinessUnits()).ifPresent(businessUnitRefDTOList -> {
			entity.setBusinessUnitsUsed(new HashSet<>());
			businessUnitRefDTOList.stream().forEach(businessUnitRefDTO -> {
				entity.getBusinessUnitsUsed().add(businessUnitService.getBusinessUnitForCurrentOrganization(businessUnitRefDTO.getId()));
			});
		});

		// Set Data Types
		Optional.ofNullable(itemDTO.getDataTypeClassifications()).ifPresent(dataTypeClassificationRefDTOList -> {
			entity.setDataTypeClassifications(new HashSet<>());
			dataTypeClassificationRefDTOList.stream().forEach(dataTypeClassificationRefDTO -> {
				entity.getDataTypeClassifications().add(dataTypeClassificationService.getDataTypeClassificationForCurrentOrganization(dataTypeClassificationRefDTO.getId()));
			});
		});

		// Set Data Assets
		Optional.ofNullable(itemDTO.getDataAssetClassifications()).ifPresent(dataAssetClassificationRefDTOList -> {
			entity.setDataAssetClassifications(new HashSet<>());
			dataAssetClassificationRefDTOList.stream().forEach(dataAssetClassificationRefDTO -> {
				entity.getDataAssetClassifications().add(dataAssetClassificationService.getDataAssetClassificationForCurrentOrganization(dataAssetClassificationRefDTO.getId()));
			});
		});

		// Set Systems
		Optional.ofNullable(itemDTO.getSystems()).ifPresent(systemRefDTOList -> {
			entity.setSystems(new HashSet<>());
			systemRefDTOList.stream().forEach(systemRefDTO -> {
				entity.getSystems().add(systemsService.getSystemForCurrentOrganization(systemRefDTO.getId()));
			});
		});

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Process
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Processes existingItem = getProcessForCurrentOrganization(itemId);
		ProcessEditDTO existingItemDTO = new ProcessEditDTO(existingItem);
		processRepository.delete(existingItem);
		processRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.PROCESS,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, existingItem.getOrganizationId())
		);

		return itemId;
	}

	/**
	 * Export all Processes as CSV to output stream.
	 * 
	 * @param outputStream
	 */
	public void exportProcesses(OutputStream outputStream) {
		try {
			CSVPrinter csvPrinter = createCsvPrinter(outputStream);

			Long organizationId = organizationService.getCurrentOrganizationId();
			List<Processes> processes = processRepository.getListByOrganizationAndName(organizationId, "",
				PageRequest.of(0, 10000000, Sort.by("name")));
			for (Processes process : processes) {
				String businessUnitPath = businessUnitService.getBusinessUnitPath(process.getBusinessUnit(), true,
					CSVUtils.PATH_SEPARATOR);

				csvPrinter.printRecord(
					process.getName(),
					process.getDescription(),
					(process.getOwner() != null) ? process.getOwner().getFullName() : "",
					(process.getOwner() != null) ? process.getOwner().getEmail() : "",
					process.getRevenueProcessed(),
					businessUnitPath,
					process.getNotes(),
					ExportUtils.businessUnitsAsString(process.getBusinessUnitsUsed(), businessUnitService),
					ExportUtils.dataTypeClassificationAsString(process.getDataTypeClassifications()),
					ExportUtils.dataAssetClassificationsAsString(process.getDataAssetClassifications()),
					ExportUtils.systemsAsString(process.getSystems()));
			}
			csvPrinter.flush();
		} catch (IOException e) {
			log.error("Failed to generate Processes CSV Data file", e);
			throw new InternalServerErrorException("Failed to generate CSV Data file");
		}
	}

	/**
	 * Export Template content for Export.
	 */
	public void exportTemplate(OutputStream outputStream) {
		try {
			CSVPrinter csvPrinter = createCsvPrinter(outputStream);
			csvPrinter.flush();
		} catch (IOException e) {
			log.error("Failed to generate Process CSV Template file", e);
			throw new InternalServerErrorException("Failed to generate CSV Template file");
		}
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(ProcessEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getOwner() != null) logItems.add(AuditLogItemId.of(VItemType.OWNER_USER, existingItemDTO.getOwner().getId()));
		if (existingItemDTO.getBusinessUnit() != null) logItems.add(AuditLogItemId.of(VItemType.BUSINESS_UNIT, existingItemDTO.getBusinessUnit().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Create CSV Printer to build Processes
	 *
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	private CSVPrinter createCsvPrinter(OutputStream outputStream) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVUtils.createCSVFormatBuilder(
			PROCESS_NAME_HEADER,
			PROCESS_DESCRIPTION_HEADER,
			PROCESS_OWNER_NAME_HEADER,
			PROCESS_OWNER_EMAIL_HEADER,
			PROCESS_REVENUE_PROCESSED_HEADER,
			PROCESS_BUSINESS_UNIT_OWNS_PATH_HEADER,
			PROCESS_NOTES_HEADER,
			PROCESS_BUSINESS_UNITS_USED_PATH_HEADER,
			PROCESS_DATA_TYPE_CLASSIFICATIONS_HEADER,
			PROCESS_DATA_ASSET_CLASSIFICATIONS_HEADER,
			PROCESS_SYSTEMS_HEADER).build();
		return new CSVPrinter(writer, csvFormat);
	}

}
