package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.document.DownloadUrlDTO;
import com.cyberintech.vrisk.server.model.dto.document.ImageRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DocumentType;
import com.cyberintech.vrisk.server.model.jpa.domains.DownloadType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.DocumentAccessTokens;
import com.cyberintech.vrisk.server.model.jpa.entity.Documents;
import com.cyberintech.vrisk.server.repository.jpa.DocumentAccessTokensReporitory;
import com.cyberintech.vrisk.server.repository.jpa.DocumentsRepository;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsService;
import com.cyberintech.vrisk.server.service.storage.vo.UploadRequestVO;
import com.cyberintech.vrisk.server.service.storage.vo.UploadResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * Documents management Service. Implements basic document CRUD.
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2019-10-15
 */
@Service
@Slf4j
public class DocumentService {

	protected String REMOTE_URL_BASE;

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DocumentAccessTokensReporitory documentAccessTokensReporitory;

	@Lazy
	@Autowired
	private DocumentAccessTokensService documentAccessTokensService;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private StorageDocumentsService storageDocumentsService;

	@Autowired
	private UserService userService;

	/**
	 * Initialize documents
	 */
	public String getRemoteStorageUrl() {
		// Initialize document details
		if (StringUtils.isEmpty(REMOTE_URL_BASE)) {
			String storageURL = storageDocumentsService.getStorageURL();

			log.info(String.format("### Rebuild bucket URL: %s", storageURL));

			REMOTE_URL_BASE = storageURL;
		}

		return REMOTE_URL_BASE;
	}

	/**
	 * Get Documents List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, DocumentDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<Documents> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, DocumentDTO> filteredResponse = new FilteredResponse<NameFilter, DocumentDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = documentsRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = documentsRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<DocumentDTO> itemsDTOList = DTOBase.fromEntitiesList(items, DocumentDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Document details
	 *
	 * @return Document Details
	 */
	public Documents getItemForCurrentOrganization(String documentUid) {
		Documents itemDetails;

		try {
			itemDetails = documentsRepository.findByDocumentUid(documentUid).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Document not found in the database [{0}]", documentUid));
		}

		// Verify Document and Organization
		if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Document [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Document details
	 *
	 * @return Document Details
	 */
	public Documents getDocumentByToken(String token) {
		Documents itemDetails;

		try {
			// Fix for incorrect access token string
			String[] tokenParts = token.split("\\?");
			String tokenString = tokenParts[0];
			DocumentAccessTokens documentAcceesToken = documentAccessTokensReporitory.findFirstByCode(tokenString).get();
			itemDetails = documentsRepository.findById(documentAcceesToken.getDocumentId()).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Document not found in the database [{0}]", token));
		}

		return itemDetails;
	}

	/**
	 * Get Document details
	 *
	 * @return Document Details
	 */
	public Documents getItemForCurrentOrganization(Long itemId) {
		Documents itemDetails;

		try {
			itemDetails = documentsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Document not found in the database [{0}]", itemId));
		}

		// Verify Document and Organization
		if (!userService.isSuperAdmin() && itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Document [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Document details
	 *
	 * @return Document Details
	 */
	public Documents getItem(Long itemId) {
		Documents itemDetails;

		try {
			itemDetails = documentsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Document not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Get Document DTO details
	 *
	 * @return Document Details
	 */
	public DocumentDTO getDetails(Long itemId) {

		Documents itemDetails = getItemForCurrentOrganization(itemId);

		DocumentDTO result = new DocumentDTO(itemDetails, true);

		return result;
	}

	/**
	 * Create new Document Domain
	 *
	 * @return New Document
	 */
	public DocumentDTO upload(MultipartFile multipartFile, DocumentType documentType) throws IOException {

		DocumentDTO result = null;

		// Creating new Document
		String documentUid = UUID.randomUUID().toString();
		String remotePath = documentType.getRemotePath();

		Boolean isPublic = false;

		// Get original Extension for the image files
		if (DocumentType.IMAGE.equals(documentType) && multipartFile.getOriginalFilename().indexOf(".") != -1) {
			String[] fileParts = StringUtils.split(multipartFile.getOriginalFilename(), '.');
			documentUid += "." + fileParts[fileParts.length - 1];
		}

		Documents newItem = new Documents();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setDocumentUid(documentUid);
		newItem.setFileName(multipartFile.getOriginalFilename());
		newItem.setFileSize(Long.valueOf(multipartFile.getSize()).doubleValue());
		newItem.setFileType(multipartFile.getContentType());
		newItem.setRemotePath(remotePath);
		newItem.setItemType(documentType.getId());
		newItem.setCreatedAt(new Date());

		// Save File to S3
		File file = convertMultiPartToFile(multipartFile);
		try {
			FileInputStream input = new FileInputStream(file);
			UploadRequestVO uploadRequest = new UploadRequestVO(documentType.getRemotePath(), documentUid, file.length(), input, documentType, multipartFile.getContentType());
			UploadResponseVO uploadResponse = storageDocumentsService.upload(uploadRequest);
			isPublic = uploadResponse.getIsPublic();

			// Save item to the database
			Documents saveResult = documentsRepository.save(newItem);

			result = getDetails(saveResult.getId());

			// Save Audit Log CREATE event
			auditLogService.create(
				VItemType.DOCUMENT_FILE,
				saveResult.getId(),
				result,
				collectAuditLogItems(result, newItem.getOrganizationId())
			);

		} catch (Exception exception) {
			// Catch default Exception
			log.error(exception.getMessage(), exception);
			throw new InternalServerErrorException("Failed to process uploaded file. Please try again later.");
		} finally {
			file.delete(); // Deleting Temporary File from disk
		}

		if (isPublic && result != null) {
			buildStorageUrl(result);
		}

		return result;
	}

	/**
	 * Get file contents as Stream
	 *
	 * @param document
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream getFileContent(Documents document) throws IOException {

		DocumentType documentType = DocumentType.of(document.getItemType());
		String fileName = documentType.getRemotePath() + "/" + document.getDocumentUid();

		return storageDocumentsService.getRemoteFileContentOutputStream(fileName, documentType);
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(DocumentDTO itemDTO, Documents entity) {
	}

	/**
	 * Deletes Document
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Documents existingItem = getItemForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Document [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getFileName()));
		}
		DocumentDTO existingItemDTO = new DocumentDTO(existingItem);
		documentsRepository.delete(existingItem);
		documentsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.DOCUMENT_FILE,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, existingItem.getOrganizationId())
		);

		return itemId;
	}

	/**
	 * Build Direct S3 URL for the document
	 *
	 * @param document
	 * @return
	 */
	public String buildStorageUrl(DocumentDTO document) {
		if (document == null || document.getDocumentUid() == null) {
			return null;
		}

		// If public Access is not Permitted - rewrite to Proxy Download URL
		if (!applicationProperties.isPublicAccessPermitted()) {
			return documentAccessTokensService.buildDownloadUrl(document.getId());
		}

		String result = getRemoteStorageUrl() + document.getRemotePath() + "/" + document.getDocumentUid();
		document.setUrl(result);

		return result;
	}

	/**
	 * Build Direct S3 URL for the document
	 *
	 * @param document
	 * @return
	 */
	public String buildStorageUrl(ImageRefDTO document) {

		if (document == null || document.getDocumentUid() == null) {
			return null;
		}

		// If public Access is not Permitted - rewrite to Proxy Download URL
		if (!applicationProperties.isPublicAccessPermitted() && document.getId() != null) {
			return documentAccessTokensService.buildDownloadUrl(document.getId());
		}

		String result = getRemoteStorageUrl() + document.getRemotePath() + "/" + document.getDocumentUid();
		document.setUrl(result);

		return result;
	}

	/**
	 * Obtain Download URL info
	 *
	 * @param downloadType
	 * @return
	 */
	public DownloadUrlDTO buildDownloadUrl(DownloadType downloadType, Long riskModelId) {
		DownloadUrlDTO result = new DownloadUrlDTO();

		DocumentAccessTokens documentAccessToken = documentAccessTokensService.create(null, 259200l);
		String linkUrl = applicationProperties.getApiUrl() + downloadType.getRemotePath() + "?dat=" + documentAccessToken.getCode();

		if (
			DownloadType.QUALITATIVE_QUESTIONS.equals(downloadType) || DownloadType.QUALITATIVE_QUESTIONS_FOR_TYPE.equals(downloadType)
			|| DownloadType.SCORING_QUESTION_ANSWERS.equals(downloadType) || DownloadType.SCORING_QUESTIONS_XLSX_REPORT.equals(downloadType)
		) {
			linkUrl += String.format("&riskModelId=%s", riskModelId);
		}
		if (DownloadType.QUANT_METRIC.equals(downloadType)) {
			linkUrl += String.format("&riskModelId=%s", riskModelId);
		}

		result.setRemotePath(downloadType.getRemotePath());
		result.setToken(documentAccessToken.getCode());
		result.setUrl(linkUrl);

		return result;
	}

	/**
	 * Build Download URL for the document
	 *
	 * @param document
	 * @return
	 */
	public String buildDownloadUrl(DocumentDTO document) {
		String apiUrl = applicationProperties.getApiUrl();
		/*
		String authorization = request.getHeader("Authorization");
		String tokenId = authorization;
		if (authorization != null && authorization.toLowerCase().contains("bearer")) {
			tokenId = authorization.substring("bearer".length() + 1);
		}
		String downloadUrl = apiUrl + "/api/documents/download/" + document.getDocumentUid() + "?token=" + tokenId.trim();
		*/
		String downloadUrl = documentAccessTokensService.buildDownloadUrl(document.getId());

		return downloadUrl;
	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = File.createTempFile(UUID.randomUUID().toString(), ".vrisk-tmp");

		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();

		return convFile;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(DocumentDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
