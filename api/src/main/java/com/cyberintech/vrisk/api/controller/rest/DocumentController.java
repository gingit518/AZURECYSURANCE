package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DocumentType;
import com.cyberintech.vrisk.server.model.jpa.entity.Documents;
import com.cyberintech.vrisk.server.service.DocumentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

/**
 * Documents Management Controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-16
 */
@RestController
@RequestMapping(
	value = DocumentController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Documents Management Controller"
)
@Tag(name = "Documents Management")
public class DocumentController {

	static final String CONTROLLER_URI = "/api/documents";

	@Autowired
	private DocumentService documentService;

	/**
	 * Upload document to the storage
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/gdpr/upload", name = "Upload document to the storage")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@Deprecated
	public DocumentDTO uploadGDPRDocument(@RequestParam("file") MultipartFile file) throws IOException {
		DocumentDTO result = documentService.upload(file, DocumentType.GDPR);

		return result;
	}

	/**
	 * Upload document to the storage
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/control_test_evidence/upload", name = "Upload document to the storage")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@Deprecated
	public DocumentDTO uploadControlTestEvidenceDocument(@RequestParam("file") MultipartFile file) throws IOException {
		DocumentDTO result = documentService.upload(file, DocumentType.CONTROL_TEST_EVIDENCE);

		return result;
	}

	/**
	 * Upload document to the storage
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/question_answer/upload", name = "Upload document to the storage")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@Deprecated
	public DocumentDTO uploadAnswerDocument(@RequestParam("file") MultipartFile file) throws IOException {
		DocumentDTO result = documentService.upload(file, DocumentType.QUESTION_ANSWER);

		return result;
	}

	/**
	 * Upload Policy document to the storage
	 *
	 * @return Document Save Details
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/policy/upload", name = "Upload Policy document to the storage")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@Deprecated
	public DocumentDTO uploadPolicyDocument(@RequestParam("file") MultipartFile file) throws IOException {
		DocumentDTO result = documentService.upload(file, DocumentType.POLICY);

		return result;
	}

	/**
	 * Upload Vendor Contract document to the storage
	 *
	 * @return Document Save Details
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/vendor-contract/upload", name = "Upload Policy document to the storage")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@Deprecated
	public DocumentDTO uploadVendorContractDocument(@RequestParam("file") MultipartFile file) throws IOException {
		DocumentDTO result = documentService.upload(file, DocumentType.VENDOR_CONTRACT);

		return result;
	}

	/**
	 * Upload general document to the storage
	 *
	 * @return Document Save Details
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/upload", name = "Upload general document to the storage")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public DocumentDTO uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
		DocumentDTO result = documentService.upload(file, DocumentType.DEFAULT);

		return result;
	}

	/**
	 * Upload document of specified type to the storage
	 *
	 * @return Document Save Details
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/upload/{documentType}", name = "Upload general document to the storage")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public DocumentDTO uploadDocument(
		@RequestParam("file") MultipartFile file,
		@PathVariable("documentType") @NotNull @Size(min = 1) DocumentType documentType
	) throws IOException {
		DocumentDTO result = documentService.upload(file, documentType);

		return result;
	}

	/**
	 * Download document
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/download/{itemId}", name = "Download Document")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadDocument(
		HttpServletResponse response
		, @PathVariable("itemId") @NotNull @Size(min = 1) String itemId
	) throws IOException {

		Documents documentDetails = documentService.getItemForCurrentOrganization(itemId);
		ByteArrayOutputStream byteArrayOutputStream = documentService.getFileContent(documentDetails);

		// Build HTTP Response
		byte[] fileBytes = byteArrayOutputStream.toByteArray();
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", documentDetails.getFileName()));
		response.setHeader("Content-Type", documentDetails.getFileType());
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download document
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/download", name = "Download Document")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadDocumentByToken(
		HttpServletResponse response
		, @Parameter(example = "101") @RequestParam("dat") @NotNull @Size(min = 1) String token
	) throws IOException {

		Documents documentDetails = documentService.getDocumentByToken(token);
		ByteArrayOutputStream byteArrayOutputStream = documentService.getFileContent(documentDetails);

		// Build HTTP Response
		byte[] fileBytes = byteArrayOutputStream.toByteArray();
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", documentDetails.getFileName()));
		response.setHeader("Content-Type", documentDetails.getFileType());
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

}
