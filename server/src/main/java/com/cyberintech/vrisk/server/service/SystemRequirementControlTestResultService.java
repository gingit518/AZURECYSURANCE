package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.SystemRequirementControlTestResultModelDAO;
import com.cyberintech.vrisk.server.model.data.ControlTestResultFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityAuditCommentDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.SystemRequirementControlTestResultDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.SecurityAuditCommentRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRequirementControlTestResultsRepository;
import com.cyberintech.vrisk.server.repository.jpa.TaskRepository;
import com.cyberintech.vrisk.server.rest.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * System Requirement Control Test Result management Service. Implements basic entity CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.01.31
 */
@Service
@Slf4j
public class SystemRequirementControlTestResultService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private SecurityAuditCommentRepository securityAuditCommentRepository;

	@Autowired
	private SecurityRequirementService securityRequirementService;

	@Autowired
	private SystemRequirementControlTestResultModelDAO systemRequirementControlTestResultModelDAO;

	@Autowired
	private SystemRequirementControlTestResultsRepository systemRequirementControlTestResultsRepository;


	@Autowired
	private SystemControlTestResultService systemControlTestResultService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SystemsService systemService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private ControlMaturityService controlMaturityService;

	@Autowired
	private UserService userService;

	/**
	 * Get System Requirement Control Test Result List
	 *
	 * @return items list
	 */
	public List<SystemRequirementControlTestResultDTO> getList() {
		List<SystemRequirementControlTestResults> items = systemRequirementControlTestResultsRepository.findAll();

		List<SystemRequirementControlTestResultDTO> result = DTOBase.fromEntitiesList(items, SystemRequirementControlTestResultDTO.class);

		return result;
	}

	/**
	 * Get System Requirement Control Test Result list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<ControlTestResultFilter, SystemRequirementControlTestResultDTO> getListFiltered(FilteredRequest<ControlTestResultFilter> filteredRequest) {

		PagedResult<SystemRequirementControlTestResultDTO> result = systemRequirementControlTestResultModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<ControlTestResultFilter, SystemRequirementControlTestResultDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get System Requirement Control Test Result details
	 *
	 * @return System Requirement Control Test Result Details
	 */
	public SystemRequirementControlTestResults getItemForCurrentOrganization(Long itemId) {
		SystemRequirementControlTestResults itemDetails;

		try {
			itemDetails = systemRequirementControlTestResultsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("System Requirement Control Test Result not found in the database [{0}]", itemId));
		}

		// Verify System Requirement Control Test Result and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System Requirement Control Test Result [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get System Requirement Control Test Result DTO details
	 *
	 * @return System Requirement Control Test Result Details
	 */
	public SystemRequirementControlTestResultDTO getDetails(Long itemId) {
		SystemRequirementControlTestResults itemDetails = getItemForCurrentOrganization(itemId);

		SystemRequirementControlTestResultDTO result = new SystemRequirementControlTestResultDTO(itemDetails);

		if (result.getDocument() != null) {
			String downloadUrl = documentService.buildDownloadUrl(result.getDocument());
			result.getDocument().setDownloadUrl(downloadUrl);
		}

		return result;
	}

	/**
	 * Create new System Requirement Control Test Result
	 *
	 * @return New System Requirement Control Test Result
	 */
	public SystemRequirementControlTestResultDTO create(SystemRequirementControlTestResultDTO newItemDTO) {
		// Throw exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		// Detect Assessment Weight
		Double assessmentWeight = newItemDTO.getControlMaturity() != null ? newItemDTO.getControlMaturity().getWeight() : newItemDTO.getAssessmentWeight();

//		SystemRequirementControlTestResults newItem = newItemDTO.toEntity(); // Breaks
		SystemRequirementControlTestResults newItem = new SystemRequirementControlTestResults();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setAssessmentWeight(assessmentWeight);
		newItem.setComments(newItemDTO.getComments());
		applyEntityChanges(newItemDTO, newItem);
		SystemRequirementControlTestResults saveResult = systemRequirementControlTestResultsRepository.save(newItem);

		systemControlTestResultService.recalculateAssessmentWeight(saveResult.getSystemControlTestResult().getId());

		SystemRequirementControlTestResultDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.SYSTEM_REQUIREMENT_CONTROL_TEST_RESULT,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Get or Create if Not Exist System Requirement Control Test Result
	 *
	 * @return System Requirement Control Test Result
	 */
	public SystemRequirementControlTestResultDTO getOrCreateIfNotExist(SystemRequirementControlTestResultDTO itemDTO) {

		if (itemDTO.getId() != null) {
			return getDetails(itemDTO.getId());
		}

		Optional<SystemRequirementControlTestResults> itemFromDB;
		SystemRequirementControlTestResultDTO result;
		if (itemDTO.getSystem() != null && itemDTO.getSystem().getId() != null) {
			if (itemDTO.getSecurityRequirement() != null && itemDTO.getSecurityRequirement().getId() != null) {
				itemFromDB = systemRequirementControlTestResultsRepository.findBySystemIdAndRequirementId(itemDTO.getSystem().getId(), itemDTO.getSecurityRequirement().getId());
			} else {
				throw new BadRequestException("Security Requirement is required!", ApplicationExceptionCodes.SECURITY_REQUIREMENT_REQUIRED);
			}
		} else {
			throw new BadRequestException("System is required!", ApplicationExceptionCodes.SYSTEM_REQUIRED);
		}

		if (itemFromDB.isPresent()) {
			result = new SystemRequirementControlTestResultDTO(itemFromDB.get());
		} else {
			result = create(itemDTO);
		}

		return result;
	}

	/**
	 * Update System Requirement Control Test Result
	 *
	 * @return Updated System Requirement Control Test Result
	 */
	public SystemRequirementControlTestResultDTO update(SystemRequirementControlTestResultDTO itemDTO) {

		// Get Existing item from the database
		SystemRequirementControlTestResults existingItem = getItemForCurrentOrganization(itemDTO.getId());
		SystemRequirementControlTestResultDTO existingItemDTO = new SystemRequirementControlTestResultDTO(existingItem);

		// Verify System Requirement Control Test Result and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System Requirement Control Test Result [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Detect Assessment Weight
		Double assessmentWeight = itemDTO.getControlMaturity() != null ? itemDTO.getControlMaturity().getWeight() : itemDTO.getAssessmentWeight();

		// Update item details
		existingItem.setAssessmentWeight(assessmentWeight);
		existingItem.setComments(itemDTO.getComments());
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		SystemRequirementControlTestResults saveResult = systemRequirementControlTestResultsRepository.save(existingItem);

		systemControlTestResultService.recalculateAssessmentWeight(saveResult.getSystemControlTestResult().getId());

		SystemRequirementControlTestResultDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.SYSTEM_REQUIREMENT_CONTROL_TEST_RESULT,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, existingItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Create Audit Comment
	 *
	 * @param securityAuditComment
	 * @param systemRequirementControlTestResultId
	 * @return
	 */
	public SecurityAuditCommentDTO createAuditComment(SecurityAuditCommentDTO securityAuditComment, Long systemRequirementControlTestResultId) {

		// Get Existing item from the database
		SystemRequirementControlTestResults existingItem = getItemForCurrentOrganization(systemRequirementControlTestResultId);

		// Verify System Requirement Control Test Result and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System Requirement Control Test Result [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		Users currentUser = userService.getCurrentUserEntity();

		SecurityAuditComments item = new SecurityAuditComments();
		item.setComment(securityAuditComment.getComment());
		item.setCreatedAt(new Date());
		item.setUpdatedAt(new Date());
		item.setCreatedBy(currentUser);
		item.setUpdatedBy(currentUser);
		SecurityAuditComments newItem = securityAuditCommentRepository.save(item);

		SecurityAuditCommentDTO result = new SecurityAuditCommentDTO(newItem);

		// Add comment
		existingItem.getSecurityAuditComments().add(newItem);
		SystemRequirementControlTestResults saveResult = systemRequirementControlTestResultsRepository.save(existingItem);

		// Save Audit Log UPDATE event
		auditLogService.create(
			VItemType.SECURITY_AUDIT_COMMENT,
			result.getId(),
			result,
			AuditLogItemId.of(VItemType.ORGANIZATION, existingItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Get list of Audit Comments
	 *
	 * @param systemRequirementControlTestResultId
	 * @return
	 */
	public List<SecurityAuditCommentDTO> getAuditComments(Long systemRequirementControlTestResultId) {

		// Get Existing item from the database
		SystemRequirementControlTestResults existingItem = getItemForCurrentOrganization(systemRequirementControlTestResultId);

		// Verify System Requirement Control Test Result and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System Requirement Control Test Result [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		List<SecurityAuditCommentDTO> result = existingItem.getSecurityAuditComments().stream().map(SecurityAuditCommentDTO::new).collect(Collectors.toList());

		return result;
	}

		/**
		 * Apply entity changes and linkages
		 *
		 * @param itemDTO
		 * @param entity
		 */
	private void applyEntityChanges(SystemRequirementControlTestResultDTO itemDTO, SystemRequirementControlTestResults entity) {

		if (itemDTO.getSecurityRequirement() != null && itemDTO.getSecurityRequirement().getId() != null) {
			SecurityRequirements securityRequirements = securityRequirementService.getSecurityRequirementForCurrentOrganization(itemDTO.getSecurityRequirement().getId());
			entity.setSecurityRequirement(securityRequirements);
		}

		if (itemDTO.getSystem() != null && itemDTO.getSystem().getId() != null) {
			Systems systems = systemService.getSystemForCurrentOrganization(itemDTO.getSystem().getId());
			entity.setSystem(systems);

			SystemControlTestResults systemControlTestResults = systemControlTestResultService.getOrCreateItemForCurrentOrganizationAndSystem(itemDTO.getSystem().getId());
			entity.setSystemControlTestResult(systemControlTestResults);
		} else {
			throw new BadRequestException("System is required", ApplicationExceptionCodes.SYSTEM_REQUIRED);
		}

		if (itemDTO.getDocument() != null && itemDTO.getDocument().getId() != null) {
			Documents document = documentService.getItemForCurrentOrganization(itemDTO.getDocument().getId());
			entity.setDocument(document);
		} else {
			entity.setDocument(null);
		}

		if (itemDTO.getControlMaturity() != null && itemDTO.getControlMaturity().getId() != null) {
			ControlMaturities controlMaturity = controlMaturityService.getControlMaturityForCurrentOrganization(itemDTO.getControlMaturity().getId());
			entity.setControlMaturity(controlMaturity);
		} else {
			entity.setControlMaturity(null);
		}

		Optional.ofNullable(itemDTO.getTasks()).ifPresent(taskViewDTOList -> {
			entity.setTasks(new HashSet<>());
			taskViewDTOList.stream().forEach(taskViewDTO -> {
				entity.getTasks().add(taskRepository.findById(taskViewDTO.getId()).get());
			});
		});

	}

	/**
	 * Get CSV content
	 *
	 * @param systemId
	 * @param assessmentId
	 */
	@Transactional
	public ByteArrayInputStream buildCsvContent(Long systemId, Long assessmentId) {
		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(outputStream);
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
				"Security Requirement",
				"Security Control Family",
				"Security Control Name",
				"Comments",
				"Tasks",
				"Control Maturity",
				"Document"
			);
			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);;

			ControlTestResultFilter filter = new ControlTestResultFilter();
			filter.setSystemId(systemId);
			filter.setAssessmentId(assessmentId);
			List<SystemRequirementControlTestResultDTO> items = systemRequirementControlTestResultModelDAO.getItemsPageable(filter, PageRequest.of(0, Integer.MAX_VALUE), null).getItems();

			for (SystemRequirementControlTestResultDTO testResultDTO : items) {
				String tasks = Optional.ofNullable(testResultDTO.getTasks()).orElse(new ArrayList<>()).stream().map(TaskViewDTO::getName).collect(Collectors.joining(", "));
				csvPrinter.printRecord(
					testResultDTO.getSecurityRequirement().getCode(),
					testResultDTO.getSecurityRequirement().getSecurityControlFamily().getName(),
					testResultDTO.getSecurityRequirement().getSecurityControlName().getName(),
					testResultDTO.getComments(),
					tasks,
					testResultDTO.getControlMaturity() != null ? testResultDTO.getControlMaturity().getName() : "",
					testResultDTO.getDocument() != null ? testResultDTO.getDocument().getFileName() : ""
				);
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to generate CSV Template file");
		}

		return byteArrayInputStream;
	}

	/**
	 * Apply Evidence Eligibility of System Requirement Control Test Result
	 *
	 * @param itemId
	 * @param evidenceEligible
	 */
	@Transactional
	public void setEvidenceEligible(Long itemId, Boolean evidenceEligible) {

		// Get Existing item from the database
		SystemRequirementControlTestResults existingItem = getItemForCurrentOrganization(itemId);
		SystemRequirementControlTestResultDTO existingItemDTO = new SystemRequirementControlTestResultDTO(existingItem);

		existingItem.setEvidenceEligible(evidenceEligible);

		// Save to the database
		SystemRequirementControlTestResults saveResult = systemRequirementControlTestResultsRepository.save(existingItem);
		SystemRequirementControlTestResultDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.SYSTEM_REQUIREMENT_CONTROL_TEST_RESULT,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, existingItem.getOrganizationId())
		);
	}

	/**
	 * Deletes System Requirement Control Test Result
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		SystemRequirementControlTestResults existingItem = getItemForCurrentOrganization(itemId);
		SystemRequirementControlTestResultDTO existingItemDTO = new SystemRequirementControlTestResultDTO(existingItem);
		systemRequirementControlTestResultsRepository.delete(existingItem);
		systemRequirementControlTestResultsRepository.flush();

		systemControlTestResultService.recalculateAssessmentWeight(existingItem.getSystemControlTestResult().getId());

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.SYSTEM_REQUIREMENT_CONTROL_TEST_RESULT,
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
	private AuditLogItemId[] collectAuditLogItems(SystemRequirementControlTestResultDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		if (existingItemDTO.getSecurityRequirement() != null && existingItemDTO.getSecurityRequirement().getId() != null) {
			logItems.add(AuditLogItemId.of(VItemType.SECURITY_REQUIREMENT, existingItemDTO.getSecurityRequirement().getId()));
		}
		if (existingItemDTO.getSystem() != null && existingItemDTO.getSystem().getId() != null) {
			logItems.add(AuditLogItemId.of(VItemType.SYSTEM, existingItemDTO.getSystem().getId()));
		}

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}


}
