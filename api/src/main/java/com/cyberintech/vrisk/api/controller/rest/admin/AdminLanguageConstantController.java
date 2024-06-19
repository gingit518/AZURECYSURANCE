package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.LanguageConstantFilter;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.language_constants.LanguageConstantValueViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SupportedLanguages;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.LanguageConstantService;
import com.cyberintech.vrisk.server.service.SupportedLanguagesService;
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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;

/**
 * Language Constant controller
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-04-10
 */
@RestController
@RequestMapping(
	value = AdminLanguageConstantController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Language Constant Controller"
)
@Tag(name = "Admin Language Constant")
public class AdminLanguageConstantController {

	static final String CONTROLLER_URI = "/api/admin/language-constants";

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private LanguageConstantService languageConstantService;

	@Autowired
	private SupportedLanguagesService supportedLanguagesService;

	/**
	 * Get Language Constant Values List for current Language
	 *
	 * @param languageCode
	 * @param filteredRequest
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/values/filter", name = "Language Constant Values List for current Filters and Language")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<LanguageConstantFilter, LanguageConstantValueViewDTO> getListFiltered(
		@Parameter(example = "eng") @RequestHeader("language-code") @NotNull String languageCode,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<LanguageConstantFilter> filteredRequest
	) {
		FilteredResponse<LanguageConstantFilter, LanguageConstantValueViewDTO> result = languageConstantService.getLanguageConstantValuesListFiltered(languageCode, filteredRequest);

		return result;
	}

	/**
	 * Update Language Constant Values List
	 *
	 * @param itemsDTOList
	 * @return
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/values", name = "Update Language Constant Values List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<LanguageConstantValueViewDTO> updateList(
		@Parameter(description = "Language Constant Values update Details List", required = true) @RequestBody List<LanguageConstantValueViewDTO> itemsDTOList,
		@Parameter(example = "eng") @RequestHeader("language-code") String languageCode
	) {
		List<LanguageConstantValueViewDTO> result = languageConstantService.updateLanguageConstantValuesList(itemsDTOList, languageCode);

		return result;
	}

	/**
	 * Upload file with Language Constants data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/import", name = "Import Language Constants data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ImportResultDTO uploadLanguageConstantsDataAsCSV(
		@Parameter(example = "eng") @RequestHeader("language-code") @NotNull String languageCode,
		@RequestParam("file") MultipartFile file
	) {
		ImportResultDTO result = languageConstantService.importLanguageConstantsFromCSVFile(languageCode, file);

		return result;
	}

	/**
	 * Download JSON document with Language Constants Data for current Language
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/json/export/{languageCode}", name = "Get Language Constants Data in JSON")
	@Produces("application/json")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadLanguageConstantsDataAsJSON(
		HttpServletResponse response,
		@PathVariable("languageCode") @NotNull String languageCode
	) throws IOException {
		SupportedLanguages language = supportedLanguagesService.getSupportedLanguage(languageCode);

		// Build Download Data JSON
		ByteArrayInputStream byteArrayInputStream = languageConstantService.getDownloadDataAsJSON(languageCode);

		// Build HTTP Response
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("LanguageConstants", language.getName(), language.getCode(), "json");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Language Constants Data for current Language
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/export/{languageCode}", name = "Get Language Constants Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadLanguageConstantsDataAsCSV(
		HttpServletResponse response,
		@PathVariable("languageCode") @NotNull @Size(min=1) String languageCode
	) throws IOException {

		SupportedLanguages language = supportedLanguagesService.getSupportedLanguage(languageCode);

		// Build Download Data CSV
		ByteArrayInputStream byteArrayInputStream = languageConstantService.getDownloadDataAsCSV(languageCode);

		// Build HTTP Response
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("LanguageConstants", language.getName(), language.getCode(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Delete All Language Constant Values for current Language
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/values", name = "Delete existing Language Constants Values for current Language")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(
		@Parameter(example = "eng") @RequestHeader("language-code") @NotNull String languageCode
	) {
		Long result = languageConstantService.clearVocabulary(languageCode);

		return result;
	}

	/**
	 * Delete All Language Constants with Values for current Language
	 *
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/values/all", name = "Delete existing Language Constants and Values")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public boolean deleteAll() {

		languageConstantService.clearAllVocabularies();

		return true;
	}
}
