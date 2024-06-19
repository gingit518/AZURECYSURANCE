package com.cyberintech.vrisk.server.service;

import static com.cyberintech.vrisk.server.service.csv.TechnologyCSVImporter.*;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.TechnologyModelDAO;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.TechnologyFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import com.cyberintech.vrisk.server.service.utils.ExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.transaction.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.*;

/**
 * Technology management Service. Implements basic entity CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-28
 */
@Service
@Slf4j
public class TechnologyService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private CountryService countryService;

	@Autowired
	private DataAssetClassificationService dataAssetClassificationService;

	@Autowired
	private DataDomainsService dataDomainsService;

	@Autowired
	private EnvironmentTypesService environmentTypesService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private TechnologyRepository technologyRepository;

	@Autowired
	private TechnologyModelDAO technologyModelDAO;

	@Autowired
	private TechnologyCategoryService technologyCategoryService;

	@Autowired
	private UserService userService;

	@Autowired
	private VendorService vendorService;

	/**
	 * Get Technology List
	 *
	 * @return Technology List
	 */
	public List<TechnologyViewDTO> getList() {
		List<Technologies> items = technologyRepository.findAll();

		List<TechnologyViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, TechnologyViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Technology List
	 *
	 * @return Users List
	 */
	public FilteredResponse<TechnologyFilter, TechnologyViewDTO> getListFiltered(FilteredRequest<TechnologyFilter> filteredRequest) {
//		List<Technologies> items = null;
//		Long count = 0l;
//		FilteredResponse<TechnologyFilter, TechnologyViewDTO> filteredResponse = new FilteredResponse<TechnologyFilter, TechnologyViewDTO>(filteredRequest);
//
//		String namePattern = "";
//		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
//			namePattern = filteredRequest.getFilter().getName();
//		}
//
//		Long organizationId = organizationService.getCurrentOrganizationId();
//
//		items = technologyRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
//		count = technologyRepository.getCountByOrganizationAndName(organizationId, namePattern);
//
//		List<TechnologyViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, TechnologyViewDTO.class);
//
//		filteredResponse.setItems(itemsDTOList);
//		filteredResponse.setTotal(count.intValue());

		PagedResult<TechnologyViewDTO> result = technologyModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<TechnologyFilter, TechnologyViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Technology details
	 *
	 * @return Technology Details
	 */
	public Technologies getTechnologyForCurrentOrganization(Long itemId) {
		Technologies itemDetails;

		try {
			itemDetails = technologyRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Technology not found in the database [{0}]", itemId));
		}

		// Verify Technology and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Technology [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Technology DTO details
	 *
	 * @return Technology Details
	 */
	public TechnologyEditDTO getDetails(Long itemId) {

		Technologies itemDetails = getTechnologyForCurrentOrganization(itemId);

		TechnologyEditDTO result = new TechnologyEditDTO(itemDetails);

		return result;
	}

	/**
	 * Get Existing or Create new Technology Class
	 *
	 * @return New Technology Class
	 */
	public Technologies getOrCreate(Long organizationId, TechnologyCategories technologyCategory, Long technologySubcategoryId, Long technologyClassId, String technologyCategoryName) {
		Optional<Technologies> technologyDetails = technologyRepository.getFirstByNameAndOrganizationAndParent(technologyCategoryName, organizationId, technologyClassId);
		if (technologyDetails.isEmpty()) {
			Technologies technology = new Technologies();
			technology.setName(technologyCategoryName);
			technology.setOrganizationId(organizationId);
			technology.setTechnologyCategory(technologyCategory);
			technology.setTechnologyClassTypeId(technologyClassId);
			technology.setTechnologySubcategoryId(technologySubcategoryId);
			technology.setCreatedAt(new Date());
			technology.setUpdatedAt(new Date());
			technology = technologyRepository.save(technology);

			technologyDetails = Optional.of(technology);
		}

		return technologyDetails.get();
	}


	/**
	 * Create new Technology Domain
	 *
	 * @return New Technology
	 */
	public TechnologyEditDTO create(TechnologyEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		Technologies newItem = newItemDTO.toEntity();
		Technologies newItem = new Technologies();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		Technologies saveResult = technologyRepository.save(newItem);

		TechnologyEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.TECHNOLOGY,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Technology
	 *
	 * @return Updated Qualitative Domains
	 */
	public TechnologyEditDTO update(TechnologyEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		Technologies existingItem = getTechnologyForCurrentOrganization(itemDTO.getId());
		TechnologyEditDTO existingItemDTO = new TechnologyEditDTO(existingItem);

		// Verify Technology and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Technology [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Technologies saveResult = technologyRepository.save(existingItem);

		TechnologyEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.TECHNOLOGY,
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
	private void applyEntityChanges(TechnologyEditDTO itemDTO, Technologies entity) {

		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setNotes(itemDTO.getNotes());
		entity.setVersion(itemDTO.getVersion());
		entity.setRiskReduction(itemDTO.getRiskReduction());
		entity.setRiskReductionPercent(itemDTO.getRiskReductionPercent());
		entity.setToolPrice(itemDTO.getToolPrice());

		// Set Technology Fields Flow
		if (itemDTO.getEolDate() != null) entity.setEolDate(itemDTO.getEolDate());
		if (itemDTO.getWarrantyExpiration() != null) entity.setWarrantyExpiration(itemDTO.getWarrantyExpiration());
		if (itemDTO.getIpAddress() != null) entity.setIpAddress(itemDTO.getIpAddress());
		if (itemDTO.getSerialNumber() != null) entity.setSerialNumber(itemDTO.getSerialNumber());
		if (itemDTO.getAssetDomainFunction() != null) entity.setAssetDomainFunction(itemDTO.getAssetDomainFunction());
		if (itemDTO.getOsName() != null) entity.setOsName(itemDTO.getOsName());
		if (itemDTO.getLocation() != null) entity.setLocation(itemDTO.getLocation());
		if (itemDTO.getHardwareSubstatus() != null) entity.setHardwareSubstatus(itemDTO.getHardwareSubstatus());
		if (itemDTO.getDiscoverySource() != null) entity.setDiscoverySource(itemDTO.getDiscoverySource());
		if (itemDTO.getDeviceId() != null) entity.setDeviceId(itemDTO.getDeviceId());

		if (itemDTO.getTechnologyCategory() != null && itemDTO.getTechnologyCategory().getId() != null) {
			TechnologyCategories technologyCategories = technologyCategoryService.getTechnologyCategoryForCurrentOrganization(itemDTO.getTechnologyCategory().getId());
			entity.setTechnologyCategory(technologyCategories);
		}

		entity.setTechnologySubcategoryId(itemDTO.getTechnologySubcategory() != null ? itemDTO.getTechnologySubcategory().getId() : null);
		entity.setTechnologyClassTypeId(itemDTO.getTechnologyClassType() != null ? itemDTO.getTechnologyClassType().getId() : null);


		if (itemDTO.getAssetClassification() != null && itemDTO.getAssetClassification().getId() != null) {
			DataAssetClassification assetClassification = dataAssetClassificationService.getDataAssetClassificationForCurrentOrganization(itemDTO.getAssetClassification().getId());
			entity.setAssetClassification(assetClassification);
		}

		if (itemDTO.getVendor() != null && itemDTO.getVendor().getId() != null) {
			Organizations vendor = vendorService.getVendor(itemDTO.getVendor().getId());
			entity.setVendor(vendor);
		}

		if (itemDTO.getCountry() != null && itemDTO.getCountry().getId() != null) {
			Country country = countryService.getCountry(itemDTO.getCountry().getId());
			entity.setCountry(country);
		}

		if (itemDTO.getEnvironmentType() != null && itemDTO.getEnvironmentType().getId() != null) {
			EnvironmentTypes environmentType = environmentTypesService.getEnvironmentTypesForCurrentOrganization(itemDTO.getEnvironmentType().getId());
			entity.setEnvironmentType(environmentType);
		}

		// Set Systems
		Optional.ofNullable(itemDTO.getSystems()).ifPresent(systemRefDTOList -> {
			entity.setSystems(new HashSet<>());
			systemRefDTOList.stream().forEach(systemRefDTO -> {
				entity.getSystems().add(systemsService.getSystemForCurrentOrganization(systemRefDTO.getId()));
			});
		});

		// Set Data Types
		Optional.ofNullable(itemDTO.getDataDomains()).ifPresent(dataDomainsList -> {
			entity.setDataDomains(new HashSet<>());
			dataDomainsList.stream().forEach(dataDomainDTO -> {
				entity.getDataDomains().add(dataDomainsService.getDataDomainsForCurrentOrganization(dataDomainDTO.getId()));
			});
		});

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Detaching vendor from the technologies
	 *
	 * @param vendor
	 * @return
	 */
	public boolean detachVendor(Organizations vendor) {
		boolean result = true;

		List<Technologies> technologies = technologyRepository.findAllByVendor(vendor);
		for (Technologies technology : technologies) {
			TechnologyEditDTO beforeUpdateDTO = new TechnologyEditDTO(technology);

			// Remove Vendor
			technology.setVendor(null);
			technologyRepository.save(technology);

			TechnologyEditDTO afterUpdateDTO = new TechnologyEditDTO(technology);

			// Save Audit Log UPDATE event
			auditLogService.update(
				VItemType.TECHNOLOGY,
				technology.getId(),
				beforeUpdateDTO,
				afterUpdateDTO,
				collectAuditLogItems(afterUpdateDTO, technology.getOrganizationId())
			);
		}

		return result;
	}

	/**
	 * Deletes Technology
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Technologies existingItem = getTechnologyForCurrentOrganization(itemId);
		TechnologyEditDTO existingItemDTO = new TechnologyEditDTO(existingItem);
		technologyRepository.delete(existingItem);
		technologyRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.TECHNOLOGY,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, existingItem.getOrganizationId())
		);

		return itemId;
	}

	public void exportTechnologies(ServletOutputStream outputStream) {
		try {
			CSVPrinter csvPrinter = createCsvPrinter(outputStream);

			Long organizationId = organizationService.getCurrentOrganizationId();
			List<Technologies> technologies = technologyRepository.getListByOrganizationAndName(organizationId, "",
				PageRequest.of(0, 10000000, Sort.by("name")));
			for (Technologies technology : technologies) {

				csvPrinter.printRecord(
					technology.getName(),
					technology.getVersion(),
					technology.getDescription(),
					ExportUtils.asString(technology.getTechnologyCategory()),
					ExportUtils.asString(technology.getAssetClassification()),
					ExportUtils.dataDomainsAsString(technology.getDataDomains()),
					ExportUtils.asString(technology.getEnvironmentType()),
					ExportUtils.asString(technology.getCountry()),
					ExportUtils.asString(technology.getVendor()),
					ExportUtils.systemsAsString(technology.getSystems()),
					verifyDoubleValue(technology.getRiskReduction()),
					verifyDoubleValue(technology.getRiskReductionPercent()),
					verifyDoubleValue(technology.getToolPrice()),
					technology.getNotes());
			}
			csvPrinter.flush();
		} catch (IOException e) {
			log.error("Failed to generate Technologies CSV Data file", e);
			throw new InternalServerErrorException("Failed to generate Technologies CSV Data file");
		}
	}

	private Double verifyDoubleValue(Double value) {
		if (value == null || value.isNaN() || value == 0) {
			return null;
		} else {
			return value;
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
			log.error("Failed to generate Technology CSV Template file", e);
			throw new InternalServerErrorException("Failed to generate Technology CSV Template file");
		}
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(TechnologyEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getTechnologyCategory() != null) logItems.add(AuditLogItemId.of(VItemType.TECHNOLOGY_CATEGORY, existingItemDTO.getTechnologyCategory().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Create CSV Printer to build Technologies
	 *
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	private CSVPrinter createCsvPrinter(OutputStream outputStream) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVUtils.createCSVFormatBuilder(
			TECHNOLOGY_NAME_HEADER,
			TECHNOLOGY_VERSION_HEADER,
			TECHNOLOGY_DESCRIPTION_HEADER,
			TECHNOLOGY_CATEGORY_HEADER,
			TECHNOLOGY_ASSET_CLASS_HEADER,
			TECHNOLOGY_DATA_DOMAINS_HEADER,
			TECHNOLOGY_ENVIRONMENT_TYPE_HEADER,
			TECHNOLOGY_COUNTRY_HEADER,
			TECHNOLOGY_VENDOR_HEADER,
			TECHNOLOGY_SYSTEMS_HEADER,
			TECHNOLOGY_RISK_REDUCTION_HEADER,
			TECHNOLOGY_RISK_REDUCTION_PERCENT_HEADER,
			TECHNOLOGY_LICENSE_COST_HEADER,
			TECHNOLOGY_NOTES_HEADER).build();
		return new CSVPrinter(writer, csvFormat);
	}

}
