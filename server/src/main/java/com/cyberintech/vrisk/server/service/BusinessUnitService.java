package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.BusinessUnitModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitEditDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitViewExtDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessEditDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.BusinessUnitRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.ProcessRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.*;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import com.cyberintech.vrisk.server.service.utils.ExportUtils;
import com.cyberintech.vrisk.server.service.utils.ImportUtils;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business Units management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
@Slf4j
public class BusinessUnitService {

	public static final String BUSINESS_UNIT_NAME_HEADER = "Business Unit Name";
	public static final String BUSINESS_UNIT_CONTACT_EMAIL_HEADER = "Contact Email";
	public static final String BUSINESS_UNIT_CONTACT_NAME_HEADER = "Contact Name";
	public static final String BUSINESS_UNIT_DESCRIPTION_HEADER = "Business Unit Description";
	public static final String BUSINESS_UNIT_INFOSEC_EMAIL_HEADER = "Infosec Focal Person Email";
	public static final String BUSINESS_UNIT_INFOSEC_NAME_HEADER = "Infosec Focal Person Name";
	public static final String BUSINESS_UNIT_OWNED_PROCESSES_HEADER = "Owned Processes";
	public static final String BUSINESS_UNIT_PARENT_NAME_HEADER = "Parent Business Unit";
	public static final String BUSINESS_UNIT_SUBSIDIARY_HEADER = "Subsidiary";
	public static final String BUSINESS_UNIT_USED_PROCESS_HEADER = "Used Processes";

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private BusinessUnitRepository businessUnitRepository;

	@Autowired
	private BusinessUnitLevelService businessUnitLevelService;

	@Autowired
	private BusinessUnitModelDAO businessUnitModelDAO;

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ProcessRepository processRepository;

	@Lazy
	@Autowired
	private ProcessService processService;

	@Lazy
	@Autowired
	private SystemsService systemsService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Get Business Units List
	 *
	 * @return Business Units List
	 */
	public List<BusinessUnitViewDTO> getList() {
		List<BusinessUnits> items = businessUnitRepository.findAll();

		List<BusinessUnitViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, BusinessUnitViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Business Units List
	 *
	 * @return Business Units List
	 */
	public FilteredResponse<NameFilter, BusinessUnitViewExtDTO> getListFilteredSimple(FilteredRequest<NameFilter> filteredRequest) {
		List<BusinessUnits> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, BusinessUnitViewExtDTO> filteredResponse = new FilteredResponse<NameFilter, BusinessUnitViewExtDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = businessUnitRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = businessUnitRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<BusinessUnitViewExtDTO> itemsDTOList = DTOBase.fromEntitiesList(items, BusinessUnitViewExtDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Business Units List
	 *
	 * @return Business Units List
	 */
	public FilteredResponse<NameFilter, BusinessUnitViewExtDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {

		PagedResult<BusinessUnitViewExtDTO> result = businessUnitModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<NameFilter, BusinessUnitViewExtDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Business Unit details
	 *
	 * @return Business Unit Details
	 */
	public BusinessUnits getBusinessUnitForCurrentOrganization(Long itemId) {
		BusinessUnits itemDetails;

		try {
			itemDetails = businessUnitRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Business Unit not found in the database [{0}]", itemId));
		}

		// Verify Business Unit and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Business Unit [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Business Unit DTO details
	 *
	 * @return Business Unit Details
	 */
	public BusinessUnitEditDTO getDetails(Long itemId) {

		BusinessUnits itemDetails = getBusinessUnitForCurrentOrganization(itemId);

		BusinessUnitEditDTO result = new BusinessUnitEditDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Business Unit Domain
	 *
	 * @return New Business Unit
	 */
	public BusinessUnitEditDTO create(BusinessUnitEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		BusinessUnits newItem = newItemDTO.toEntity();
		BusinessUnits newItem = new BusinessUnits();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		BusinessUnits saveResult = businessUnitRepository.save(newItem);

		businessUnitLevelService.buildBusinessUnitLevels(saveResult.getId());

		BusinessUnitEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.BUSINESS_UNIT,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Business Unit
	 *
	 * @return Updated Business Unit
	 */
	public BusinessUnitEditDTO update(BusinessUnitEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		BusinessUnits existingItem = getBusinessUnitForCurrentOrganization(itemDTO.getId());
		BusinessUnitEditDTO existingItemDTO = new BusinessUnitEditDTO(existingItem);

		// Verify Business Unit and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Business Unit [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		existingItem.setName(itemDTO.getName());
		existingItem.setDescription(itemDTO.getDescription());
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		BusinessUnits saveResult = businessUnitRepository.save(existingItem);

		businessUnitLevelService.buildBusinessUnitLevels(saveResult.getId());

		BusinessUnitEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.BUSINESS_UNIT,
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
	private void applyEntityChanges(BusinessUnitEditDTO itemDTO, BusinessUnits entity) {

		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());

		if (itemDTO.getOwner() != null && itemDTO.getOwner().getId() != null) {
			Users owner = userService.getOrganizationUser(itemDTO.getOwner().getId());
			entity.setOwner(owner);
		}

		if (itemDTO.getInfosecFocalPerson() != null && itemDTO.getInfosecFocalPerson().getId() != null) {
			Users infosecFocalPerson = userService.getOrganizationUser(itemDTO.getInfosecFocalPerson().getId());
			entity.setInfosecFocalPerson(infosecFocalPerson);
		}

		// Setup Parent Business Unit
		if (itemDTO.getParent() != null && itemDTO.getParent().getId() != null) {
			BusinessUnits parent = getBusinessUnitForCurrentOrganization(itemDTO.getParent().getId());
			entity.setParent(parent);
		} else {
			entity.setParent(null);
		}

		// Setup Subsidiary Organization
		if (itemDTO.getSubsidiaryOrganization() != null && itemDTO.getSubsidiaryOrganization().getId() != null) {
			Organizations subsidiary = organizationService.getSubsidiaryForCurrentOrganization(itemDTO.getSubsidiaryOrganization().getId());
			entity.setSubsidiaryOrganization(subsidiary);
		} else {
			entity.setSubsidiaryOrganization(null);
		}

		// Owned Processes should be managed from Process side only. Skip ownedProcesses field processing

		// Setup Used Processes
		entity.setUsedProcesses(collectProcesses(itemDTO.getUsedProcesses()));

		// Set Owned Processes
		Optional.ofNullable(itemDTO.getOwnedProcesses()).ifPresent(processRefDTOList -> {
			entity.setOwnedProcesses(new HashSet<>());
			processRefDTOList.stream().forEach(processRefDTO -> {
				entity.getOwnedProcesses().add(processService.getProcessForCurrentOrganization(processRefDTO.getId()));
			});
		});

		// Set Owned Systems
		Optional.ofNullable(itemDTO.getOwnedSystems()).ifPresent(systemRefDTOList -> {
			entity.setOwnedSystems(new HashSet<>());
			systemRefDTOList.stream().forEach(systemRefDTO -> {
				entity.getOwnedSystems().add(systemsService.getSystemForCurrentOrganization(systemRefDTO.getId()));
			});
		});

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Business Unit
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		BusinessUnits existingItem = getBusinessUnitForCurrentOrganization(itemId);
		BusinessUnitEditDTO existingItemDTO = new BusinessUnitEditDTO(existingItem);

		List<BusinessUnitLevels> childs = businessUnitLevelService.getListByParent(itemId);
		if (!childs.isEmpty()) {
			String message = clientMessage.getMessage(SLCT.VALIDATION$BUSINESS_UNIT$DELETE$HAS_CHILDS);
			message += ":\\n" + childs.stream().map(businessUnitLevels -> businessUnitLevels.getChild() != null ? businessUnitLevels.getChild().getName() : null).filter(Objects::nonNull).collect(Collectors.joining("\\n"));
			throw new ConflictException(message, ApplicationExceptionCodes.BUSINESS_UNIT_HAS_CHILDS);
		}

		businessUnitLevelService.deleteBusinessUnitLevels(existingItem.getId());

		businessUnitRepository.delete(existingItem);
		businessUnitRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.BUSINESS_UNIT,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, existingItem.getOrganizationId())
		);

		return itemId;
	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getDownloadTemplate() {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createCsvPrinter(outputStream);
			// csvPrinter.printRecord("", "", "");
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.error("Failed to generate CSV Template file", e);
			throw new InternalServerErrorException("Failed to generate CSV Template file");
		}

		return byteArrayInputStream;
	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getDownloadData() {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createCsvPrinter(outputStream);

			Long organizationId = organizationService.getCurrentOrganizationId();
			List<BusinessUnits> businessUnits = businessUnitRepository.getListByOrganizationAndName(organizationId, "", PageRequest.of(0, 10000000, Sort.by("name")));
			for (BusinessUnits businessUnit : businessUnits) {
				csvPrinter.printRecord(
					businessUnit.getName(),
					businessUnit.getDescription(),
					ExportUtils.asString(businessUnit.getParent(), this),
					ExportUtils.userEmailAsString(businessUnit.getOwner()),
					ExportUtils.userFullNameAsString(businessUnit.getOwner()),
					ExportUtils.userEmailAsString(businessUnit.getInfosecFocalPerson()),
					ExportUtils.userFullNameAsString(businessUnit.getInfosecFocalPerson()),
					ExportUtils.processesAsString(businessUnit.getUsedProcesses()),
					ExportUtils.processesAsString(businessUnit.getOwnedProcesses()),
					ExportUtils.asString(businessUnit.getSubsidiaryOrganization())
				);
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.error("Failed to generate CSV Data file for Business Unit", e);
			throw new InternalServerErrorException("Failed to generate CSV Data file");
		}

		return byteArrayInputStream;
	}

	/**
	 * Create CSV Printer to build Business Units
	 *
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	private CSVPrinter createCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
		return CSVUtils.createCSVPrinter(outputStream,
			BUSINESS_UNIT_NAME_HEADER,
			BUSINESS_UNIT_DESCRIPTION_HEADER,
			BUSINESS_UNIT_PARENT_NAME_HEADER,
			BUSINESS_UNIT_CONTACT_EMAIL_HEADER,
			BUSINESS_UNIT_CONTACT_NAME_HEADER,
			BUSINESS_UNIT_INFOSEC_EMAIL_HEADER,
			BUSINESS_UNIT_INFOSEC_NAME_HEADER,
			BUSINESS_UNIT_USED_PROCESS_HEADER,
			BUSINESS_UNIT_OWNED_PROCESSES_HEADER,
			BUSINESS_UNIT_SUBSIDIARY_HEADER
		);
	}

	/**
	 * Get full Path for Business Unit
	 *
	 * @param entity
	 * @param reverse
	 * @return
	 */
	public String getBusinessUnitPath(BusinessUnits entity, boolean reverse) {
		return getBusinessUnitPath(entity, reverse, "/");
	}

	/**
	 * Get full Path for Business Unit
	 *
	 * @param entity
	 * @param reverse
	 * @param separator
	 * @return
	 */
	public String getBusinessUnitPath(BusinessUnits entity, boolean reverse, String separator) {
		String result = "";

		if (entity != null) {
			int count = 0;
			BusinessUnits currentParent = entity;
			List<String> itemsList = new ArrayList<>();
			while (currentParent != null) {
				itemsList.add(currentParent.getName());

				currentParent = currentParent.getParent();
				if (count++ > 16) break;
			}

			// Reverse Array if Needed
			if (reverse) Collections.reverse(itemsList);

			result = StringUtils.join(itemsList, separator);
		}

		return result;
	}

	/**
	 * Get Path for Parent and Organization
	 *
	 * @param parentPath
	 * @param organizationId
	 * @return
	 */
	public BusinessUnits getParentByPath(String parentPath, Long organizationId) {
		return getParentByPath(parentPath, organizationId, true);
	}

	/**
	 * Get Path for Parent and Organization
	 *
	 * @param parentPath
	 * @param organizationId
	 * @param reverse
	 * @return
	 */
	public BusinessUnits getParentByPath(String parentPath, Long organizationId, boolean reverse) {
		return getParentByPath(parentPath, organizationId, reverse, ";");
	}

	/**
	 * Get Path for Parent and Organization
	 *
	 * @param parentPath
	 * @param organizationId
	 * @param reverse
	 * @return
	 */
	public BusinessUnits getParentByPath(String parentPath, Long organizationId, boolean reverse, String separator) {
		BusinessUnits result = null;

		if (StringUtils.isNotEmpty(parentPath)) {
			String[] parents = StringUtils.split(parentPath, separator);
			// Reverify business unit path
			if (!(";".equals(separator)) && parents.length < 2 && parentPath.contains(";")) {
				parents = StringUtils.split(parentPath, ";");
			}
			List<String> parentsList = Arrays.stream(parents).map(parentName -> parentName.trim()).collect(Collectors.toList());
			if (reverse) Collections.reverse(parentsList);

			Optional<BusinessUnits> currentParent = Optional.empty();
			if (parentsList.size() > 0) {
				currentParent = businessUnitRepository.getByOrganizationAndNameAndNoParent(parentsList.get(0), organizationId);

				if (currentParent.isPresent()) {
					for (int i = 1; i < parentsList.size(); i++) {
						String parentName = parentsList.get(i).trim();
						currentParent = businessUnitRepository.getByOrganizationAndNameAndParentId(parentName, currentParent.get().getId(), organizationId);

						if (currentParent.isEmpty()) return null;
					}

					result = currentParent.get();
				}
			}
		}

		return result;
	}

	/**
	 * Insert business unit data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(InputStream fileContentStream) {

		ImportResultDTO result = new ImportResultDTO();
		Organizations organization = organizationService.getCurrentOrganizationEntity();

		try {
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();

			List<BusinessUnitWithParent> updateSequence = new ArrayList<>();
			Map<String, BusinessUnits> unitsMap = new HashMap<>();
			Map<Long, BusinessUnitEditDTO> existingItemsMap = new HashMap<>();
			for (CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				String name = CSVUtils.getAsString(csvRecord, BUSINESS_UNIT_NAME_HEADER);
				String scopeName = MessageFormat.format("Business Unit [{0}]", name);

				try {
					String description = CSVUtils.getAsString(csvRecord, BUSINESS_UNIT_DESCRIPTION_HEADER);
					String ownerEmail = CSVUtils.getAsString(csvRecord, BUSINESS_UNIT_CONTACT_EMAIL_HEADER);
					String ownerName = CSVUtils.getAsString(csvRecord, BUSINESS_UNIT_CONTACT_NAME_HEADER);
					String infosecOwnerEmail = CSVUtils.getAsString(csvRecord, BUSINESS_UNIT_INFOSEC_EMAIL_HEADER);
					String infosecOwnerName = CSVUtils.getAsString(csvRecord, BUSINESS_UNIT_INFOSEC_NAME_HEADER);
					String parentName = CSVUtils.getAsString(csvRecord, BUSINESS_UNIT_PARENT_NAME_HEADER);
					String[] ownedProcesses = CSVUtils.getAsStrings(csvRecord, BUSINESS_UNIT_OWNED_PROCESSES_HEADER);
					String[] usedProcesses = CSVUtils.getAsStrings(csvRecord, BUSINESS_UNIT_USED_PROCESS_HEADER);

					BusinessUnits businessUnitParent = getParentByPath(parentName, organization.getId(), false, CSVUtils.PATH_SEPARATOR);
					if (businessUnitParent == null)
						businessUnitParent = getParentByPath(parentName, organization.getId(), true, CSVUtils.PATH_SEPARATOR);
					Optional<BusinessUnits> businessUnitItem = Optional.empty();
					if (businessUnitParent != null) {
						businessUnitItem = businessUnitRepository.getByOrganizationAndNameAndParentId(name,
							businessUnitParent.getId(), organization.getId());
					} else {
						businessUnitItem = businessUnitRepository.getByOrganizationAndNameAndNoParent(name,
							organization.getId());
					}
					BusinessUnits businessUnit;
					if (businessUnitItem.isEmpty()) {
						businessUnit = new BusinessUnits();
						businessUnit.setName(name);
						businessUnit.setOrganizationId(organization.getId());
						businessUnit.setCreatedBy(userService.getCurrentUserEntity());
						businessUnit.setCreatedAt(new Date());
					} else {
						businessUnit = businessUnitItem.get();
						existingItemsMap.put(businessUnit.getId(), new BusinessUnitEditDTO(businessUnit));
					}

					// Get Business Unit Data
					Optional<Users> owner = (StringUtils.isNotEmpty(ownerEmail))
						? userRepository.findFirstByEmailIgnoreCaseAndOrganization(ownerEmail, organization)
						: Optional.empty();
					if (owner.isEmpty() && StringUtils.isNotEmpty(ownerName))
						owner = userRepository.findFirstByFullNameAndOrganization(ownerName, organization);
					Optional<Users> infosecOwner = (StringUtils.isNotEmpty(infosecOwnerEmail))
						? userRepository.findFirstByEmailIgnoreCaseAndOrganization(infosecOwnerEmail, organization)
						: Optional.empty();
					if (infosecOwner.isEmpty() && StringUtils.isNotEmpty(infosecOwnerName))
						owner = userRepository.findFirstByFullNameAndOrganization(infosecOwnerName, organization);

					// Fill item info
					businessUnit.setDescription(description);
					if (owner.isPresent())
						businessUnit.setOwner(owner.get());
					if (infosecOwner.isPresent())
						businessUnit.setInfosecFocalPerson(infosecOwner.get());

					businessUnit.setUpdatedBy(userService.getCurrentUserEntity());
					businessUnit.setUpdatedAt(new Date());

					// Create Item
					// BusinessUnitEditDTO businessUnitEditDTO = businessUnitItem.isEmpty() ?
					// create(businessUnits) : update(businessUnits);
					// BusinessUnits businessUnit =
					// businessUnitRepository.findById(businessUnitEditDTO.getId()).get();

					updateSequence.add(new BusinessUnitWithParent(businessUnit, parentName));

					// Put item to the map
					// if (StringUtils.isNotEmpty(parentName)) unitsMap.put(name + "_" + parentName,
					// businessUnit);

					// Save owned processes
					for (String ownedProcessName : ownedProcesses) {
						Optional<Processes> ownedProcessOptional = processRepository
							.findFirstByNameAndOrganizationId(ownedProcessName, organization.getId());
						if (ownedProcessOptional.isPresent()) {
							businessUnit.getOwnedProcesses().add(ownedProcessOptional.get());
						} else {
							ProcessEditDTO processEditDTO = new ProcessEditDTO();
							processEditDTO.setName(ownedProcessName);
							processEditDTO = processService.create(processEditDTO);
							businessUnit.getOwnedProcesses()
								.add(processRepository.findById(processEditDTO.getId()).get());
						}
					}

					// Save used processes
					for (String usedProcessName : usedProcesses) {
						Optional<Processes> usedProcessOptional = processRepository
							.findFirstByNameAndOrganizationId(usedProcessName, organization.getId());
						if (usedProcessOptional.isPresent()) {
							businessUnit.getUsedProcesses().add(usedProcessOptional.get());
						} else {
							ProcessEditDTO processEditDTO = new ProcessEditDTO();
							processEditDTO.setName(usedProcessName);
							processEditDTO = processService.create(processEditDTO);
							businessUnit.getUsedProcesses()
								.add(processRepository.findById(processEditDTO.getId()).get());
						}
					}

					Pair<OrganizationRefDTO, List<String>> subsidiaryPair = ImportUtils.loadOrganization(csvRecord,
						BUSINESS_UNIT_SUBSIDIARY_HEADER, OrganizationType.Subsidiary, scopeName, organization,
						organizationRepository);
					if (subsidiaryPair.getLeft() != null) {
						Optional<Organizations> subsidiary = organizationRepository
							.findById(subsidiaryPair.getLeft().getId());
						if (subsidiary.isPresent()) {
							businessUnit.setSubsidiaryOrganization(subsidiary.get());
						} else {
							log.warn("{}. Subsidiary is not found", scopeName);
						}
					} else {
						businessUnit.setSubsidiaryOrganization(null);
					}
					result.getMessages().addAll(subsidiaryPair.getRight());
				} catch (Exception e) {
					log.error("Failed to process Bisiness Unit [{}]", name);
				}
			}

			updateSequence.sort((o1, o2) -> {
				return o1.parentsNum - o2.parentsNum;
			});

			for (BusinessUnitWithParent businessUnitWithParent : updateSequence) {
				BusinessUnits businessUnitParentItem = getParentByPath(businessUnitWithParent.getParentName(), organization.getId(), true);
				if (businessUnitParentItem == null) businessUnitParentItem = getParentByPath(businessUnitWithParent.getParentName(), organization.getId(), false);
				BusinessUnits businessUnit = businessUnitWithParent.getBusinessUnit();
				if (businessUnitParentItem != null) {
					businessUnit.setParent(businessUnitParentItem);
				} else if (StringUtils.isNotEmpty(businessUnitWithParent.getParentName())) {
					result.getMessages().add(MessageFormat.format("Parent [{0}] for business unit [{1}] not found in the organization. Skipping.", businessUnitWithParent.getParentName(), businessUnit.getName()));
				}

				boolean isExists = businessUnit.getId() != null;
				businessUnit = businessUnitRepository.save(businessUnit);
				BusinessUnitEditDTO businessUnitItemDTO = new BusinessUnitEditDTO(businessUnit);
				if (!isExists) {
					result.getCreated().add(new ItemViewDTO(businessUnit.getId(), getBusinessUnitPath(businessUnit, true, " \\ ")));
					result.getMessages().add(MessageFormat.format("New Business unit [{0}] created successfully.", businessUnit.getName()));

					// Save Audit Log CREATE event
					auditLogService.create(
						VItemType.BUSINESS_UNIT,
						businessUnitItemDTO.getId(),
						businessUnitItemDTO,
						collectAuditLogItems(businessUnitItemDTO, businessUnit.getOrganizationId())
					);
				} else {
					result.getMessages().add(MessageFormat.format("Business unit [{0}] already exists in the organization. Updating.", businessUnit.getName()));
					result.getUpdated().add(new ItemViewDTO(businessUnit.getId(), getBusinessUnitPath(businessUnit, true, " \\ ")));

					BusinessUnitEditDTO existingItemDTO = existingItemsMap.get(businessUnit.getId());
					// Save Audit Log UPDATE event
					auditLogService.update(
						VItemType.BUSINESS_UNIT,
						existingItemDTO.getId(),
						existingItemDTO,
						businessUnitItemDTO,
						collectAuditLogItems(businessUnitItemDTO, businessUnit.getOrganizationId())
					);
				}
			}

		} catch (IOException e) {
			log.error("Import of Business Units from CSV file failed", e);
		}

		return result;
	}

	/**
	 * Insert business unit data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(MultipartFile file) {

		InputStream fileContentStream = null;

		try {
			fileContentStream = file.getInputStream();
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return importFromCSVFile(fileContentStream);
	}

	@Getter
	public static class BusinessUnitWithParent {
		private BusinessUnits businessUnit;
		private String parentName;
		private String[] parents;
		private int parentsNum = 0;

		public BusinessUnitWithParent(BusinessUnits businessUnit, String parentName) {
			this.businessUnit = businessUnit;
			this.parentName = parentName;

			if (StringUtils.isNotEmpty(parentName)) {
				String[] parents = StringUtils.split(parentName, ";");
				this.parents = parents;
			} else {
				this.parents = new String[0];
			}

			this.parentsNum = (parents != null) ? parents.length : 0;
		}
	}


	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(BusinessUnitEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getOwner() != null) logItems.add(AuditLogItemId.of(VItemType.BUSINESS_UNIT_OWNER, existingItemDTO.getOwner().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Collect {@linkplain Processes} that listed at the collection of DTO's. Every
	 * process should be of current organization.
	 *
	 * @param processDtos the collection of DTO's.
	 * @return
	 */
	private Set<Processes> collectProcesses(Collection<ProcessRefDTO> processDtos) {
		if (CollectionUtils.isNotEmpty(processDtos)) {
			Set<Processes> processes = new HashSet<>(processDtos.size());
			for (ProcessRefDTO processDto : processDtos) {
				Processes process = processService.getProcessForCurrentOrganization(processDto.getId());
				processes.add(process);
			}
			return processes;
		}
		return Collections.emptySet();
	}

}
