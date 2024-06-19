package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPREvidenceDocumentsDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleItem;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPREvidenceDocuments;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.repository.jpa.DocumentsRepository;
import com.cyberintech.vrisk.server.repository.jpa.GDPRArticleItemRepository;
import com.cyberintech.vrisk.server.repository.jpa.GDPREvidenceDocumentsRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GDPR Evidence Documents management Service. Implements basic entity CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-11-11
 */
@Service
@Slf4j
public class GDPREvidenceDocumentsService {

	@Autowired
	private DocumentService documentService;

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private GDPREvidenceDocumentsRepository gdprEvidenceDocumentsRepository;

	@Autowired
	private GDPRArticleItemRepository gdprArticleItemRepository;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	/**
	 * Get GDPR Evidence Documents List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, GDPREvidenceDocumentsDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<GDPREvidenceDocuments> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, GDPREvidenceDocumentsDTO> filteredResponse = new FilteredResponse<NameFilter, GDPREvidenceDocumentsDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = gdprEvidenceDocumentsRepository.getListByName(namePattern, organizationId, filteredRequest.toPageRequest());
		count = gdprEvidenceDocumentsRepository.getCountByName(namePattern, organizationId);

		List<GDPREvidenceDocumentsDTO> itemsDTOList = DTOBase.fromEntitiesList(items, GDPREvidenceDocumentsDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get GDPR Evidence Documents details
	 *
	 * @return GDPR Evidence Documents Details
	 */
	public GDPREvidenceDocuments getItemForCurrentOrganization(Long itemId) {
		GDPREvidenceDocuments itemDetails;

		try {
			itemDetails = gdprEvidenceDocumentsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("GDPR Evidence Documents not found in the database [{0}]", itemId));
		}

		// Verify GDPR Evidence Documents and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for GDPR Evidence Documents [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get GDPR Evidence Documents DTO details
	 *
	 * @return GDPR Evidence Documents Details
	 */
	public GDPREvidenceDocumentsDTO getDetails(Long itemId) {

		GDPREvidenceDocuments itemDetails = getItemForCurrentOrganization(itemId);

		GDPREvidenceDocumentsDTO result = new GDPREvidenceDocumentsDTO(itemDetails);

		if (result.getDocuments() != null) {
			for (DocumentDTO document : result.getDocuments()) {
				String downloadUrl = documentService.buildDownloadUrl(document);
				document.setDownloadUrl(downloadUrl);
			}
		}

		return result;
	}


	/**
	 * Create new GDPR Evidence Documents Domain
	 *
	 * @return New GDPR Evidence Documents
	 */
	public GDPREvidenceDocumentsDTO create(GDPREvidenceDocumentsDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		GDPREvidenceDocuments newItem = newItemDTO.toEntity();
		GDPREvidenceDocuments newItem = new GDPREvidenceDocuments();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		GDPREvidenceDocuments saveResult = gdprEvidenceDocumentsRepository.save(newItem);

		GDPREvidenceDocumentsDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.GDPR_EVIDENCE_DOCUMENTS,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update GDPR Evidence Documents
	 *
	 * @return Updated Qualitative Domains
	 */
	public GDPREvidenceDocumentsDTO update(GDPREvidenceDocumentsDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		GDPREvidenceDocuments existingItem = getItemForCurrentOrganization(itemDTO.getId());
		GDPREvidenceDocumentsDTO existingItemDTO = new GDPREvidenceDocumentsDTO(existingItem);

		// Verify GDPR Evidence Documents and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for GDPR Evidence Documents [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		GDPREvidenceDocuments saveResult = gdprEvidenceDocumentsRepository.save(existingItem);

		GDPREvidenceDocumentsDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.GDPR_EVIDENCE_DOCUMENTS,
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
	private void applyEntityChanges(GDPREvidenceDocumentsDTO itemDTO, GDPREvidenceDocuments entity) {

		entity.setName(itemDTO.getName());
		entity.setDocumentType(itemDTO.getDocumentType());
		entity.setTemplateLink(itemDTO.getTemplateLink());

		// Set GDPR Articles Mapping
		Optional.ofNullable(itemDTO.getArticles()).ifPresent(itemsList -> {
			entity.setArticles(new HashSet<>());
			itemsList.stream().forEach(item -> {
				entity.getArticles().add(gdprArticleItemRepository.findFirstByIdAndOrganizationId(item.getId(), entity.getOrganizationId()).get());
			});
		});

		// Set GDPR Documents Mapping
		Optional.ofNullable(itemDTO.getDocuments()).ifPresent(itemsList -> {
			entity.setDocuments(new HashSet<>());
			itemsList.stream().forEach(item -> {
				entity.getDocuments().add(documentsRepository.findByIdAndOrganizationId(item.getId(), entity.getOrganizationId()).get());
			});
		});
	}

	/**
	 * Deletes GDPR Evidence Documents
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		GDPREvidenceDocuments existingItem = getItemForCurrentOrganization(itemId);
		GDPREvidenceDocumentsDTO existingItemDTO = new GDPREvidenceDocumentsDTO(existingItem);
		gdprEvidenceDocumentsRepository.delete(existingItem);
		gdprEvidenceDocumentsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.GDPR_EVIDENCE_DOCUMENTS,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, existingItem.getOrganizationId())
		);

		return itemId;
	}

	/**
	 * Insert GDPR data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(MultipartFile file) {

		// Check Permissions to Import GDPR Data
		if (!userService.hasRole(RoleType.ADMIN) && !userService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import GDPR data!", ApplicationExceptionCodes.GDPR_IMPORT_FORBIDDEN);
		}

		ImportResultDTO result = new ImportResultDTO();
		try {
			InputStream fileContentStream = file.getInputStream();
			result = importFromCSVFile(fileContentStream);
		} catch (IOException e) {
			log.error("Failed to import GDPR Evidence Documents", e);
		}

		return result;
	}

	/**
	 * Insert GDPR data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(InputStream fileContentStream) {

		Organizations organization = organizationService.getCurrentOrganizationEntity();
		log.info(MessageFormat.format("## Importing GDPR Data for Organization: {0}", organization.getName()));

		ImportResultDTO result = new ImportResultDTO();
		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			for (CSVRecord csvRecord : csvRecordList) {
				if (!csvRecord.isMapped("Document")) {
					break;
				}
				String documentType = csvRecord.isMapped("Type") ? csvRecord.get("Type").trim() : "Vrisk";
				String name = Optional.ofNullable(csvRecord.get("Document")).orElse("").trim();
				String articlesString = csvRecord.isMapped("Articles") ? csvRecord.get("Articles").trim() : "";
				String templateLink = csvRecord.isMapped("Template Link") ? csvRecord.get("Template Link").trim() : null;

				String[] articles = StringUtils.split(articlesString,",");
				List<String> articlesList = Arrays.stream(articles).map(itemName -> itemName.trim()).filter(itemName -> StringUtils.isNotEmpty(itemName)).collect(Collectors.toList());

				Optional<GDPREvidenceDocuments> evidenceDocItemOptional = gdprEvidenceDocumentsRepository.findFirstByNameIgnoreCaseAndOrganizationId(name, organization.getId());
				GDPREvidenceDocuments evidenceDocuments;
				if (evidenceDocItemOptional.isPresent()) {
					evidenceDocuments = evidenceDocItemOptional.get();
				} else {
					evidenceDocuments = new GDPREvidenceDocuments();
					evidenceDocuments.setOrganizationId(organization.getId());
				}
				evidenceDocuments.setDocumentType(documentType);
				evidenceDocuments.setName(name);
				if (StringUtils.isNotEmpty(templateLink)) {
					evidenceDocuments.setTemplateLink(templateLink);
				}

				evidenceDocuments.setArticles(new HashSet<>());
				for (String articleName : articlesList) {
					Optional<GDPRArticleItem> articleOptional = gdprArticleItemRepository.findFirstByReferenceNumberIgnoreCaseAndOrganizationId(articleName, organization.getId());
					if (articleOptional.isPresent()) {
						evidenceDocuments.getArticles().add(articleOptional.get());
					}
				}

				boolean isNew = evidenceDocuments.getId() != null;

				GDPREvidenceDocuments savedItem = gdprEvidenceDocumentsRepository.save(evidenceDocuments);

				if (isNew) {
					result.getUpdated().add(new ItemViewDTO(evidenceDocuments.getId(), evidenceDocuments.getName()));
				} else {
					result.getCreated().add(new ItemViewDTO(evidenceDocuments.getId(), evidenceDocuments.getName()));
				}
			}
		} catch (IOException e) {
			log.error("Failed to import GDPR Evidence Documents", e);
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
	private AuditLogItemId[] collectAuditLogItems(GDPREvidenceDocumentsDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
