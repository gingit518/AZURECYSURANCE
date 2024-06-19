package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.SecurityRequirementModelDAO;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.SecurityRequirementFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityRequirementDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryMappingViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFrameworkLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.*;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Security Requirement management Service. Implements basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since	 2020-01-24
 */
@Service
@Slf4j
public class SecurityRequirementService {

	public static final String FAMILY_NAME_HEADER = "Security Control Family";
	public static final String FAMILY_DESCRIPTION_HEADER = "Family Description";
	public static final String CONTROL_NAME_HEADER = "Security Control Name";
	public static final String CONTROL_NAME_DESCRIPTION_HEADER = "Control Name Description";
	public static final String ASSESSMENT_LEVEL_NAME_HEADER = "Assessment Level Name";
	public static final String ASSESSMENT_LEVEL_DESCRIPTION_HEADER = "Assessment Level Description";
	public static final String SECURITY_REQUIREMENT_CODE_HEADER = "Security Requirement Code";
	public static final String SECURITY_REQUIREMENT_PROGRAM_AREA_HEADER = "Security Requirement Program Area";
	public static final String SECURITY_REQUIREMENT_DESCRIPTION_HEADER = "Security Requirement Description";
	public static final String SECURITY_REQUIREMENT_TEST_PROCEDURE_HEADER = "Detailed Control Testing Procedure";
	public static final String SECURITY_REQUIREMENT_RISK_STATEMENT_EXAMPLES_HEADER = "Risk Statement Examples";
	public static final String SECURITY_REQUIREMENT_CONTROL_SUBCATEGORIES_HEADER = "Guidelines";
	public static final String SECURITY_REQUIREMENT_IG1 = "IG1";
	public static final String SECURITY_REQUIREMENT_IG2 = "IG2";
	public static final String SECURITY_REQUIREMENT_IG3 = "IG3";

	public static final Set<String> predefinedHeaders = new HashSet<>();

	{
		predefinedHeaders.addAll(Arrays.asList(
			FAMILY_NAME_HEADER,
			FAMILY_DESCRIPTION_HEADER,
			CONTROL_NAME_HEADER,
			CONTROL_NAME_DESCRIPTION_HEADER,
			ASSESSMENT_LEVEL_NAME_HEADER,
			ASSESSMENT_LEVEL_DESCRIPTION_HEADER,
			SECURITY_REQUIREMENT_CODE_HEADER,
			SECURITY_REQUIREMENT_PROGRAM_AREA_HEADER,
			SECURITY_REQUIREMENT_DESCRIPTION_HEADER,
			SECURITY_REQUIREMENT_TEST_PROCEDURE_HEADER,
			SECURITY_REQUIREMENT_RISK_STATEMENT_EXAMPLES_HEADER,
			SECURITY_REQUIREMENT_CONTROL_SUBCATEGORIES_HEADER
		));
	}

	@Autowired
	private SecurityRequirementModelDAO securityRequirementModelDAO;

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private SecurityRequirementRepository securityRequirementRepository;

	@Autowired
	private SecurityControlNamesRepository securityControlNamesRepository;

	@Autowired
	private SecurityControlFamiliesRepository securityControlFamiliesRepository;

	@Autowired
	private SecurityRequirementLevelsRepository securityRequirementLevelsRepository;

	@Autowired
	private ControlSubcategoryService controlSubcategoryService;

	@Autowired
	private ControlSubcategoriesRepository controlSubcategoriesRepository;

	@Autowired
	private AssessmentLevelsRepository assessmentLevelsRepository;

	@Autowired
	private AssessmentTypesRepository assessmentTypesRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Security Requirements List
	 *
	 * @return Security Requirements List
	 */
	public List<SecurityRequirementDTO> getList() {
		List<SecurityRequirements> items = securityRequirementRepository.findAll();

		List<SecurityRequirementDTO> itemsDTOs = DTOBase.fromEntitiesList(items, SecurityRequirementDTO.class);

		return itemsDTOs;
	}

	/**
	 * Get Security Requirements List
	 *
	 * @return Security Requirements List
	 */
	public FilteredResponse<SecurityRequirementFilter, SecurityRequirementDTO> getListFiltered(FilteredRequest<SecurityRequirementFilter> filteredRequest) {

		PagedResult<SecurityRequirementDTO> result = securityRequirementModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<SecurityRequirementFilter, SecurityRequirementDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Security Requirement details
	 *
	 * @return Security Requirement Details
	 */
	public SecurityRequirements getSecurityRequirementForCurrentOrganization(Long itemId) {
		SecurityRequirements itemDetails;

		try {
			itemDetails = securityRequirementRepository.findById(itemId).get();

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Security Requirement not found in the database [{0}]", itemId));
		}

		// Verify Security Requirement and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Security Requirement [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Security Requirement DTO details
	 *
	 * @return Security Requirement Details
	 */
	public SecurityRequirementDTO getDetails(Long itemId) {

		SecurityRequirements itemDetails = getSecurityRequirementForCurrentOrganization(itemId);

		SecurityRequirementDTO result = new SecurityRequirementDTO(itemDetails);

		return result;
	}

	/**
	 * Get Mapping between Security Requirement and Security Frameworks
	 *
	 * @return Security Requirement Mappings
	 */
	public List<ControlSubcategoryMappingViewDTO> getMappings(Long itemId) {

		SecurityRequirements itemDetails = getSecurityRequirementForCurrentOrganization(itemId);

		List<ControlSubcategoryMappingViewDTO> result = itemDetails.getControlSubcategories().stream().map(ControlSubcategoryMappingViewDTO::new).collect(Collectors.toList());

		return result;
	}

	/**
	 * Save Mapping between Security Requirement and Security Frameworks
	 *
	 * @param itemId
	 * @param mappingItems
	 * @return
	 */
	public List<ControlSubcategoryMappingViewDTO> saveMappings(Long itemId, List<ControlSubcategoryMappingViewDTO> mappingItems) {

		SecurityRequirements itemDetails = getSecurityRequirementForCurrentOrganization(itemId);

		itemDetails.setControlSubcategories(new HashSet<>());
		for (ControlSubcategoryMappingViewDTO itemDTO : mappingItems) {
			if (itemDTO.getControlSubcategory() != null && itemDTO.getControlSubcategory().getId() != null) {
				ControlSubcategories controlSubcategory = controlSubcategoryService.getControlSubcategoryForCurrentOrganization(itemDTO.getControlSubcategory().getId());
				itemDetails.getControlSubcategories().add(controlSubcategory);
			}
		}

		// Save item
		SecurityRequirements itemDetailsResult = securityRequirementRepository.save(itemDetails);

		List<ControlSubcategoryMappingViewDTO> result = itemDetailsResult.getControlSubcategories().stream().map(ControlSubcategoryMappingViewDTO::new).collect(Collectors.toList());

		return result;
	}

	/**
	 * Create new Security Requirement
	 *
	 * @return new Security Requirement
	 */
	public SecurityRequirementDTO create (SecurityRequirementDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		SecurityRequirements newItem = newItemDTO.toEntity();
		SecurityRequirements newItem = new SecurityRequirements();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);

		// Save to the database
		SecurityRequirements saveResult = securityRequirementRepository.save(newItem);

		SecurityRequirementDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.SECURITY_REQUIREMENT,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Security Requirement
	 *
	 * @return updated Security Requirement
	 */
	public SecurityRequirementDTO update(SecurityRequirementDTO itemDTO) {

		// Get existing item from the database
		SecurityRequirements existingItem = getSecurityRequirementForCurrentOrganization(itemDTO.getId());
		SecurityRequirementDTO existingItemDTO = new SecurityRequirementDTO(existingItem);

		// Verify Security Requirement and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Security Requirement [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		SecurityRequirements saveResult = securityRequirementRepository.save(existingItem);

		SecurityRequirementDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.SECURITY_REQUIREMENT,
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
	private void applyEntityChanges(SecurityRequirementDTO itemDTO, SecurityRequirements entity) {
		entity.setCode(itemDTO.getCode());
		entity.setProgramArea(itemDTO.getProgramArea());
		entity.setDescription(itemDTO.getDescription());
		entity.setDetailedControlTestingProcedure(itemDTO.getDetailedControlTestingProcedure());
		entity.setRiskStatementExamples(itemDTO.getRiskStatementExamples());

		if(itemDTO.getAssessmentLevel() != null && itemDTO.getAssessmentLevel().getId() != null) {
			AssessmentLevels assessmentLevels = assessmentLevelsRepository.findById(itemDTO.getAssessmentLevel().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Assessment Level not found [{0}]", itemDTO.getAssessmentLevel().getId()), ApplicationExceptionCodes.ASSESSMENT_LEVEL_NOT_EXISTS));
			entity.setAssessmentLevel(assessmentLevels);
		}

		if (itemDTO.getSecurityControlFamily() != null && itemDTO.getSecurityControlFamily().getId() != null) {
			SecurityControlFamilies securityControlFamilies = securityControlFamiliesRepository.findById(itemDTO.getSecurityControlFamily().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Security Control Family not found [{0}]", itemDTO.getSecurityControlFamily().getId()), ApplicationExceptionCodes.SECURITY_CONTROL_FAMILY_NOT_EXISTS));
			entity.setSecurityControlFamily(securityControlFamilies);
		}

		if (itemDTO.getSecurityControlName() != null && itemDTO.getSecurityControlName().getId() != null) {
			SecurityControlNames securityControlNames = securityControlNamesRepository.findById(itemDTO.getSecurityControlName().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Security Control Name not found [{0}]", itemDTO.getSecurityControlName().getId()), ApplicationExceptionCodes.SECURITY_CONTROL_NAME_NOT_EXISTS));
			entity.setSecurityControlName(securityControlNames);
		}

		if (CollectionUtils.isNotEmpty(itemDTO.getSecurityRequirementLevels())) {
			// Apply security requirement levels
			Map<AssessmentFrameworkLevel, SecurityRequirementLevels> levelsMap = entity.getSecurityRequirementLevels().stream().collect(Collectors.toMap(SecurityRequirementLevels::getAssessmentFrameworkLevel, securityRequirementLevels -> securityRequirementLevels));
			entity.setSecurityRequirementLevels(new HashSet<>());
			for (AssessmentFrameworkLevel reqLevel : itemDTO.getSecurityRequirementLevels()) {
				applySecurityRequirementLevel("YES", reqLevel, entity, levelsMap);
			}
		}


//		if (itemDTO.getAssessment() != null && itemDTO.getAssessment().getId() != null) {
//			Assessments assessments = assessmentsRepository.findById(itemDTO.getAssessment().getId())
//				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Assessment not found [{0}]", itemDTO.getAssessment().getId()), ApplicationExceptionCodes.ASSESSMENT_NOT_EXISTS));
//			entity.setAssessment(assessments);
//		}
	}

	/**
	 * Deletes Security Requirement
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		SecurityRequirements existingItem = getSecurityRequirementForCurrentOrganization(itemId);
		SecurityRequirementDTO existingItemDTO = new SecurityRequirementDTO(existingItem);
		securityRequirementRepository.delete(existingItem);
		securityRequirementRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.SECURITY_REQUIREMENT,
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
	private AuditLogItemId[] collectAuditLogItems(SecurityRequirementDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Get content for download
	 */
	public ByteArrayInputStream getDownloadData() {
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			Writer writer = new OutputStreamWriter(outputStream);
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
				SECURITY_REQUIREMENT_CODE_HEADER,
				SECURITY_REQUIREMENT_PROGRAM_AREA_HEADER,
				ASSESSMENT_LEVEL_NAME_HEADER,
				SECURITY_REQUIREMENT_DESCRIPTION_HEADER,
				FAMILY_NAME_HEADER,
				CONTROL_NAME_HEADER,
				SECURITY_REQUIREMENT_TEST_PROCEDURE_HEADER,
				SECURITY_REQUIREMENT_RISK_STATEMENT_EXAMPLES_HEADER,
				SECURITY_REQUIREMENT_CONTROL_SUBCATEGORIES_HEADER
			);
			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
			Set<SecurityRequirements> items = securityRequirementRepository.findAllByOrganizationIdOrderByCodeAsc(organizationService.getCurrentOrganizationId());

			for (SecurityRequirements item: items) {
				SecurityControlFamilies family = Optional.ofNullable(item.getSecurityControlFamily()).orElse(new SecurityControlFamilies());
				SecurityControlNames controlName = Optional.ofNullable(item.getSecurityControlName()).orElse(new SecurityControlNames());
				AssessmentLevels assessmentLevel = Optional.ofNullable(item.getAssessmentLevel()).orElse(new AssessmentLevels());
				Set<ControlSubcategories> controlSubcategories = Optional.ofNullable(item.getControlSubcategories()).orElse(new HashSet<>());

				csvPrinter.printRecord(
					item.getCode() != null ? item.getCode(): "",
					item.getProgramArea() != null ? item.getProgramArea(): "",
					assessmentLevel.getName() != null ? assessmentLevel.getName(): "",
					item.getDescription() != null ? item.getDescription(): "",
					family.getName() != null ? family.getName(): "",
					controlName.getName() != null ? controlName.getName(): "",
					item.getDetailedControlTestingProcedure() != null ? item.getDetailedControlTestingProcedure(): "",
					item.getRiskStatementExamples() != null ? item.getRiskStatementExamples(): "",
					// string array of either codes, or names of controlSubcategories related to the securityRequirement
					controlSubcategories.stream().map(subcategory -> subcategory.getCode() != null ? subcategory.getCode(): subcategory.getName()).collect(Collectors.joining(","))
				);
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException exception) {
			log.warn(exception.getMessage(), exception);
			throw new InternalServerErrorException("Failed to generate CSV Template file for Security Requirements");
		}

		return byteArrayInputStream;
	}

	/**
	 * Insert Security Requirements from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(InputStream fileContentStream) {

		// Check Permissions to Import Security Requirements
		if (!userService.hasRole(RoleType.ADMIN) && !userService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import Security Requirements!", ApplicationExceptionCodes.SECURITY_REQUIREMENT_IMPORT_FORBIDDEN);
		}

		ImportResultDTO result = new ImportResultDTO();

		Organizations organization = organizationService.getCurrentOrganizationEntity();
		log.info(MessageFormat.format("## Importing Security Requirements for Organization: {0}", organization.getName()));
		try {

			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);

			Map<Integer, String> headers = MapUtils.invertMap(csvParser.getHeaderMap());
			System.out.println("\n\n" + headers.toString() + "\n\n");
			Map<Integer, AssessmentTypes> columnNumberToFrameworkMap = new HashMap<>();
			// there are 9 columns predefined for SecurityRequirement,
			// so in case there are more than 9 headers, than some security frameworks also passed with mappings
			for (int i = 0; i < headers.entrySet().size(); i++) {
				if (!predefinedHeaders.contains(headers.get(i).trim())) {
					String frameworkName = headers.get(i).trim();
					Optional<AssessmentTypes> frameworkOptional = assessmentTypesRepository.findFirstByNameIgnoreCaseAndOrganizationId(frameworkName, organization.getId());

					if (frameworkOptional.isPresent()) {
						columnNumberToFrameworkMap.put(i, frameworkOptional.get());
					}
				}
			}

			List<CSVRecord> csvRecordList = csvParser.getRecords();
			result = this.importFromCSVItems(csvRecordList, organization, columnNumberToFrameworkMap);
//			result = new ImportResultDTO();
		} catch (IOException exception) {
			log.warn(exception.getMessage(), exception);
		}

		return result;
	}

	/**
	 * Insert some data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVItems(List<CSVRecord> csvRecordList, Organizations organization, Map<Integer, AssessmentTypes> columnNumberToFrameworkMap) {

		ImportResultDTO result = new ImportResultDTO();

		// Proceed with security requirement management
		for (CSVRecord csvRecord: csvRecordList) {

			// Accessing values by Header names
			String familyName = csvRecord.isMapped(FAMILY_NAME_HEADER) ? Optional.ofNullable(csvRecord.get(FAMILY_NAME_HEADER)).orElse("").trim() : "";
//			String familyDescription = Optional.ofNullable(csvRecord.get(FAMILY_DESCRIPTION_HEADER)).orElse("").trim();
			String controlName = csvRecord.isMapped(CONTROL_NAME_HEADER) ? Optional.ofNullable(csvRecord.get(CONTROL_NAME_HEADER)).orElse("").trim() : "";
//			String controlDescription = Optional.ofNullable(csvRecord.get(CONTROL_NAME_DESCRIPTION_HEADER)).orElse("").trim();
			String assessmentLevelName = Optional.ofNullable(csvRecord.get(ASSESSMENT_LEVEL_NAME_HEADER)).orElse("").trim();
//			String assessmentLevelDescription = Optional.ofNullable(csvRecord.get(ASSESSMENT_LEVEL_DESCRIPTION_HEADER)).orElse("").trim();
			String securityRequirementCode = Optional.ofNullable(csvRecord.get(SECURITY_REQUIREMENT_CODE_HEADER)).orElse("").trim();
			String securityRequirementProgramArea = csvRecord.isMapped(SECURITY_REQUIREMENT_PROGRAM_AREA_HEADER) ? Optional.ofNullable(csvRecord.get(SECURITY_REQUIREMENT_PROGRAM_AREA_HEADER)).orElse("").trim() : "";
			String securityRequirementDescription = csvRecord.isMapped(SECURITY_REQUIREMENT_DESCRIPTION_HEADER) ? Optional.ofNullable(csvRecord.get(SECURITY_REQUIREMENT_DESCRIPTION_HEADER)).orElse("").trim() : "";
			String securityRequirementTestProcedure = csvRecord.isMapped(SECURITY_REQUIREMENT_TEST_PROCEDURE_HEADER) ? Optional.ofNullable(csvRecord.get(SECURITY_REQUIREMENT_TEST_PROCEDURE_HEADER)).orElse("").trim() : "";
			String securityRequirementStatementExamples = csvRecord.isMapped(SECURITY_REQUIREMENT_RISK_STATEMENT_EXAMPLES_HEADER) ? Optional.ofNullable(csvRecord.get(SECURITY_REQUIREMENT_RISK_STATEMENT_EXAMPLES_HEADER)).orElse("").trim() : "";
			String controlSubcategoriesString = csvRecord.isMapped(SECURITY_REQUIREMENT_CONTROL_SUBCATEGORIES_HEADER) ? Optional.ofNullable(csvRecord.get(SECURITY_REQUIREMENT_CONTROL_SUBCATEGORIES_HEADER)).orElse("").trim() : "";

			String ig1String = csvRecord.isMapped(SECURITY_REQUIREMENT_IG1) ? Optional.ofNullable(csvRecord.get(SECURITY_REQUIREMENT_IG1)).orElse("").trim() : "NO";
			String ig2String = csvRecord.isMapped(SECURITY_REQUIREMENT_IG2) ? Optional.ofNullable(csvRecord.get(SECURITY_REQUIREMENT_IG2)).orElse("").trim() : "NO";
			String ig3String = csvRecord.isMapped(SECURITY_REQUIREMENT_IG3) ? Optional.ofNullable(csvRecord.get(SECURITY_REQUIREMENT_IG3)).orElse("").trim() : "NO";

			String[] controlSubcategories = StringUtils.split(controlSubcategoriesString, ",");
			List<String> controlSubcategoriesList = Arrays.stream(controlSubcategories).map(String::trim).filter(StringUtils::isNotEmpty).collect(Collectors.toList());

			Optional<SecurityRequirements> securityRequirementOptional = securityRequirementRepository.findFirstByCodeIgnoreCaseAndOrganizationId(securityRequirementCode, organization.getId());
			SecurityRequirements securityRequirement = null;
			if (securityRequirementOptional.isPresent()) {
				log.info(MessageFormat.format("## Update Security Requirement [{0}]", securityRequirementCode));

				securityRequirement = securityRequirementOptional.get();
			} else {
				log.info(MessageFormat.format("## Create new Security Requirement [{0}]", securityRequirementCode));

				securityRequirement = new SecurityRequirements();
				securityRequirement.setOrganizationId(organization.getId());
			}
			securityRequirement.setCode(securityRequirementCode);
			securityRequirement.setDescription(securityRequirementDescription);
			securityRequirement.setDetailedControlTestingProcedure(securityRequirementTestProcedure);
			securityRequirement.setProgramArea(securityRequirementProgramArea);
			securityRequirement.setRiskStatementExamples(securityRequirementStatementExamples);

			securityRequirement.setControlSubcategories(new HashSet<>());
			for (String controlSubcategoryName: controlSubcategoriesList) {
				// Due to controlSubcategoryName could contain either code or name of controlSubcategory we should made 2 requests to the database
				// (trying to find controlSubcategory by code(which is priority way), or by name)
				Optional<ControlSubcategories> controlSubcategoryByCode = controlSubcategoriesRepository.findFirstByCodeIgnoreCaseAndOrganizationId(controlSubcategoryName, organization.getId());
				Optional<ControlSubcategories> controlSubcategoryByName = controlSubcategoriesRepository.findFirstByNameIgnoreCaseAndOrganizationId(controlSubcategoryName, organization.getId());
				if (controlSubcategoryByCode.isPresent() || controlSubcategoryByName.isPresent()) {
					securityRequirement.getControlSubcategories().add(controlSubcategoryByCode.isPresent() ? controlSubcategoryByCode.get(): controlSubcategoryByName.get());
				}
			}

			Optional<SecurityControlFamilies> familyEntity = securityControlFamiliesRepository.findFirstByNameIgnoreCaseAndOrganizationIdIsNull(familyName);
			if (familyEntity.isEmpty()) familyEntity = securityControlFamiliesRepository.findFirstByNameIgnoreCaseAndOrganizationId(familyName, organization.getId());
			SecurityControlFamilies securityControlFamily = null;
			if (familyEntity.isPresent()) {
				securityRequirement.setSecurityControlFamily(familyEntity.get());
				securityControlFamily = familyEntity.get();
			} else {
				// If current user is System Admin - allow him to create Control Family
				if (StringUtils.isNotEmpty(familyName)) {
					securityControlFamily = new SecurityControlFamilies();
					securityControlFamily.setName(familyName);
					if (!userService.hasRole(RoleType.ADMIN)) {
						securityControlFamily.setOrganizationId(organization.getId());
					}
					securityControlFamily = securityControlFamiliesRepository.save(securityControlFamily);
					securityRequirement.setSecurityControlFamily(securityControlFamily);
				}
			}

			Optional<SecurityControlNames> controlNameEntity = securityControlNamesRepository.findFirstByNameIgnoreCaseAndOrganizationIdIsNull(controlName);
			if (controlNameEntity.isEmpty()) controlNameEntity = securityControlNamesRepository.findFirstByNameIgnoreCaseAndOrganizationId(controlName, organization.getId());
			if (controlNameEntity.isPresent()) {
				securityRequirement.setSecurityControlName(controlNameEntity.get());
			} else {
				// If current user is System Admin - allow him to create Control Name
				if (securityControlFamily != null && StringUtils.isNotEmpty(controlName)) {
					SecurityControlNames securityControlName = new SecurityControlNames();
					if (!userService.hasRole(RoleType.ADMIN)) {
						securityControlName.setOrganizationId(organization.getId());
					}
					securityControlName.setSecurityControlFamily(securityControlFamily);
					securityControlName.setName(controlName);
					securityControlName = securityControlNamesRepository.save(securityControlName);
					securityRequirement.setSecurityControlName(securityControlName);
				}
			}

			// Apply security requirement levels
			Map<AssessmentFrameworkLevel, SecurityRequirementLevels> levelsMap = securityRequirement.getSecurityRequirementLevels().stream().collect(Collectors.toMap(SecurityRequirementLevels::getAssessmentFrameworkLevel, securityRequirementLevels -> securityRequirementLevels));
			securityRequirement.setSecurityRequirementLevels(new HashSet<>());
			applySecurityRequirementLevel(ig1String, AssessmentFrameworkLevel.IG1, securityRequirement, levelsMap);
			applySecurityRequirementLevel(ig2String, AssessmentFrameworkLevel.IG2, securityRequirement, levelsMap);
			applySecurityRequirementLevel(ig3String, AssessmentFrameworkLevel.IG3, securityRequirement, levelsMap);

			Optional<AssessmentLevels> assessmentLevelEntity = assessmentLevelsRepository.findFirstByNameIgnoreCase(assessmentLevelName);
			if (assessmentLevelEntity.isPresent()) {
				securityRequirement.setAssessmentLevel(assessmentLevelEntity.get());
			}

			securityRequirement = securityRequirementRepository.save(securityRequirement);

			boolean isNew = !securityRequirementOptional.isPresent();

			if (isNew) {
				result.getCreated().add(new ItemViewDTO(securityRequirement.getId(), securityRequirement.getCode()));
				result.getMessages().add(MessageFormat.format("Create new Security Requirement: {0}", securityRequirementCode));
			} else {
				result.getUpdated().add(new ItemViewDTO(securityRequirement.getId(), securityRequirement.getCode()));
				result.getMessages().add(MessageFormat.format("Update Security Requirement: {0}", securityRequirementCode));
			}

			// Proceed with frameworks mapping management
			for (Map.Entry<Integer, AssessmentTypes> columnToFramework: columnNumberToFrameworkMap.entrySet()) {
				String controlSubcategoriesMapping = Optional.ofNullable(csvRecord.get(columnToFramework.getKey())).orElse("");
				String[] controlSubcategoriesMappingArray = controlSubcategoriesMapping.split(";");

				for (String controlSubcategoryIdentifier: controlSubcategoriesMappingArray) {
					Optional<ControlSubcategories> controlSubcategoryOptional = controlSubcategoriesRepository.findFirstByCodeIgnoreCaseAndAssessmentType(controlSubcategoryIdentifier.trim(), columnToFramework.getValue());

					// If there is no control subcategory found by code and framework then try to find one by name and framework
					if (!controlSubcategoryOptional.isPresent()) {
						controlSubcategoryOptional = controlSubcategoriesRepository.findFirstByNameIgnoreCaseAndAssessmentType(controlSubcategoryIdentifier.trim(), columnToFramework.getValue());
					}

					if (controlSubcategoryOptional.isPresent()) {
						boolean added = securityRequirement.getControlSubcategories().add(controlSubcategoryOptional.get());

						if (added) {
							result.getMessages().add(MessageFormat.format("Found Framework \"{0}\" mapping to Requirement \"{1}\" through Guideline \"{2}\"",
								columnToFramework.getValue().getName(),
								securityRequirementCode,
								StringUtils.isNotEmpty(controlSubcategoryOptional.get().getCode()) ? controlSubcategoryOptional.get().getCode(): controlSubcategoryOptional.get().getName()
							));
						}
					}
				}
			}
			// save security requirement with updated frameworks mapping
			securityRequirement = securityRequirementRepository.save(securityRequirement);
			System.out.println("");
		}

		return result;
	}

	private void applySecurityRequirementLevel(String ig1String, AssessmentFrameworkLevel level, SecurityRequirements securityRequirement, Map<AssessmentFrameworkLevel, SecurityRequirementLevels> levelsMap) {
		if ("YES".equalsIgnoreCase(ig1String)) {
			if (levelsMap.containsKey(level)) {
				securityRequirement.getSecurityRequirementLevels().add(levelsMap.get(level));
			} else {
				SecurityRequirementLevels securityRequirementLevels = new SecurityRequirementLevels();
				securityRequirementLevels.setAssessmentFrameworkLevel(level);
				securityRequirement.getSecurityRequirementLevels().add(securityRequirementLevelsRepository.save(securityRequirementLevels));
			}
		}
	}
}
