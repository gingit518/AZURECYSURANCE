package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.LanguageConstantValueModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.LanguageConstantFilter;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.language_constants.LanguageConstantValueViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.LanguageConstantScopeType;
import com.cyberintech.vrisk.server.model.jpa.entity.LanguageConstantValues;
import com.cyberintech.vrisk.server.model.jpa.entity.LanguageConstants;
import com.cyberintech.vrisk.server.model.jpa.entity.SupportedLanguages;
import com.cyberintech.vrisk.server.repository.jpa.LanguageConstantRepository;
import com.cyberintech.vrisk.server.repository.jpa.LanguageConstantValueRepository;
import com.cyberintech.vrisk.server.repository.jpa.SupportedLanguagesRepository;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Language Constant Service.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-04-09
 */
@Service
@Slf4j
public class LanguageConstantService {

	public static final String CONSTANT_HEADER = "Constant";
	public static final String SCOPE_HEADER = "Scope";
	public static final String VALUE_HEADER = "Value";
	public static final String DEFAULT_VALUE_HEADER = "Default Value";

	public static final String TEST_LANGUAGE_CODE = "lcd";
	public static final String TEST_LANGUAGE_LOCALE = "lc_TEST";
	public static final String DEFAULT_LANGUAGE_NAME = "English";
	public static final String DEFAULT_LANGUAGE_CODE = "eng";
	public static final String DEFAULT_LANGUAGE_LOCALE = "en_US";

	public static Map<String, Map<String, String>> serverLanguageConstants = new HashMap<>();
	public static Map<String, Map<String, String>> uiLanguageConstants = new HashMap<>();
	public static Map<String, Map<String, String>> hintsLanguageConstants = new HashMap<>();
	public static Map<String, Map<String, String>> menuItemsLanguageConstants = new HashMap<>();

	public static Map<String, String> serverDefaultLanguageConstants = new HashMap<>();

	@Autowired
	private LanguageConstantRepository languageConstantRepository;

	@Autowired
	private LanguageConstantValueRepository languageConstantValueRepository;

	@Lazy
	@Autowired
	private LanguageConstantValueModelDAO languageConstantValueModelDAO;

	@Autowired
	private SupportedLanguagesRepository supportedLanguagesRepository;

	@Autowired
	private SupportedLanguagesService supportedLanguagesService;

	/**
	 * Load all UI and Server translations
	 */
	@Transactional
	public void loadLanguageConstants() {

		List<SupportedLanguages> languages = supportedLanguagesRepository.findAll();
		for (SupportedLanguages language: languages) {
			List<LanguageConstantValueViewDTO> languageConstantValuesServer = new ArrayList<>();
			List<LanguageConstantValueViewDTO> languageConstantValuesUI = new ArrayList<>();

			final String languageCode = language.getCode();

			// Load UI constants as well
			List<LanguageConstantValueViewDTO> languageConstantValuesDashboardScope = languageConstantValueModelDAO.getItemsPageable(LanguageConstantFilter.of(language.getCode(), LanguageConstantScopeType.DASHBOARD), PageRequest.of(0, Integer.MAX_VALUE), null).getItems();
			List<LanguageConstantValueViewDTO> languageConstantValuesUIScope = languageConstantValueModelDAO.getItemsPageable(LanguageConstantFilter.of(language.getCode(), LanguageConstantScopeType.UI), PageRequest.of(0, Integer.MAX_VALUE), null).getItems();
			List<LanguageConstantValueViewDTO> languageConstantValuesServerScope = languageConstantValueModelDAO.getItemsPageable(LanguageConstantFilter.of(language.getCode(), LanguageConstantScopeType.SERVER), PageRequest.of(0, Integer.MAX_VALUE), null).getItems();

			languageConstantValuesServer.addAll(languageConstantValuesDashboardScope);
			languageConstantValuesServer.addAll(languageConstantValuesUIScope);
			languageConstantValuesServer.addAll(languageConstantValuesServerScope);

			languageConstantValuesUI.addAll(languageConstantValuesDashboardScope);
			languageConstantValuesUI.addAll(languageConstantValuesUIScope);

			// Build Serverside constants
			Map<String, String> serverLanguageConstants = new HashMap<>();
			loadLanguageCodesMap(languageCode, languageConstantValuesServer, serverLanguageConstants);
			LanguageConstantService.serverLanguageConstants.put(language.getLocale(), serverLanguageConstants);

			// Build UI language constants
			Map<String, String> uiLanguageConstants = new HashMap<>();
			loadLanguageCodesMap(languageCode, languageConstantValuesUI, uiLanguageConstants);
			LanguageConstantService.uiLanguageConstants.put(language.getLocale(), uiLanguageConstants);
			LanguageConstantService.uiLanguageConstants.put(language.getCode(), uiLanguageConstants);
		}

		if (serverDefaultLanguageConstants.size() < 1 && serverLanguageConstants.size() > 0) {
			serverDefaultLanguageConstants = serverLanguageConstants.get(DEFAULT_LANGUAGE_LOCALE);
			if (serverDefaultLanguageConstants == null) serverDefaultLanguageConstants = serverLanguageConstants.values().stream().findFirst().orElse(new HashMap<>());
		}

		// Resync Language codes
		// resyncWithDefaultLocale(LanguageConstantService.uiLanguageConstants, DEFAULT_LANGUAGE_LOCALE);

		// Loading Hint Values to the UI
		reloadHintsLanguageConstants();
		reloadMenuLanguageConstants();
	}

	/**
	 * Reload all menu translations
	 */
	public void reloadMenuLanguageConstants() {
		reloadLanguageConstants(LanguageConstantScopeType.MENU, LanguageConstantService.menuItemsLanguageConstants);
	}

	/**
	 * Reload all hint translations
	 */
	public void reloadHintsLanguageConstants() {
		reloadLanguageConstants(LanguageConstantScopeType.HINTS, LanguageConstantService.hintsLanguageConstants);
	}

	/**
	 * Reload all hint translations
	 */
	@Transactional
	public void reloadLanguageConstants(LanguageConstantScopeType scope, Map<String, Map<String, String>> languageConstants) {
		List<SupportedLanguages> languages = supportedLanguagesRepository.findAll();
		for (SupportedLanguages language: languages) {
			final String languageCode = language.getCode();
			List<LanguageConstantValueViewDTO> languageConstantValues = languageConstantValueModelDAO.getItemsPageable(LanguageConstantFilter.of(language.getCode(), scope), PageRequest.of(0, Integer.MAX_VALUE), null).getItems();
			Map<String, String> languageConstantsMap = new HashMap<>();
			loadLanguageCodesMap(languageCode, languageConstantValues, languageConstantsMap);
			languageConstants.put(language.getLocale(), languageConstantsMap);
			languageConstants.put(language.getCode(), languageConstantsMap);
		}
	}

	private void loadLanguageCodesMap(String languageCode, List<LanguageConstantValueViewDTO> languageConstantValues, Map<String, String> languageConstantsMap) {
		for (LanguageConstantValueViewDTO languageConstantValue : languageConstantValues) {
			String languageValue = (TEST_LANGUAGE_CODE.equals(languageCode) && languageConstantValue.getLanguageConstant() != null && languageConstantValue.getLanguageConstant().getName() != null)
				? languageConstantValue.getLanguageConstant().getName()
				: ((StringUtils.isEmpty(languageConstantValue.getValue()) && languageConstantValue.getDefaultValue() != null) ? languageConstantValue.getDefaultValue() : languageConstantValue.getValue());
			languageConstantsMap.put(languageConstantValue.getLanguageConstant().getName(), languageValue);
		}
	}

	/**
	 * Get language constant from the languages Map
	 *
	 * @param wordCode
	 * @param languageCode
	 * @param languageConstants
	 * @param useFromDefaultIfNotExists
	 * @return
	 */
	public static String getLanguageConstantValue(String wordCode, String languageCode, Map<String, Map<String, String>> languageConstants, Boolean useFromDefaultIfNotExists, Boolean useWordcodeAsDefault) {
		String result = useWordcodeAsDefault ? wordCode : null;

		Map<String, String> languageTranslations = languageConstants.get(languageCode);
		if (languageTranslations != null && languageTranslations.containsKey(wordCode)) {
			result = languageTranslations.get(wordCode);
		} else if (Boolean.TRUE.equals(useFromDefaultIfNotExists) && languageConstants.get(DEFAULT_LANGUAGE_CODE).containsKey(wordCode)) {
			result = languageConstants.get(DEFAULT_LANGUAGE_CODE).get(wordCode);
		}

		return result;
	}

	/**
	 * Get language constant from the Hints languages Map
	 *
	 * @param wordCode
	 * @param languageCode
	 * @return
	 */
	public static String getHintLanguageConstantValue(String wordCode, String languageCode) {
		return getLanguageConstantValue(wordCode, languageCode, hintsLanguageConstants, false, true);
	}

	/**
	 * Get language constant from the Hints languages Map
	 *
	 * @param wordCode
	 * @param languageCode
	 * @return
	 */
	public static String getHintLanguageConstantValue(String wordCode, String languageCode, Boolean useFromDefaultIfNotExists, Boolean useWordcodeAsDefault) {
		return getLanguageConstantValue(wordCode, languageCode, hintsLanguageConstants, useFromDefaultIfNotExists, useWordcodeAsDefault);
	}

	/**
	 * Get language constant from the Manu Items languages Map
	 *
	 * @param wordCode
	 * @param languageCode
	 * @return
	 */
	public static String getMenuLanguageConstantValue(String wordCode, String languageCode) {
		return getLanguageConstantValue(wordCode, languageCode, menuItemsLanguageConstants, false, true);
	}

	/**
	 * Get language constant from the Manu Items languages Map
	 *
	 * @param wordCode
	 * @param languageCode
	 * @return
	 */
	public static String getUILanguageConstantValue(String wordCode, String languageCode, Boolean useFromDefaultIfNotExists) {
		return getLanguageConstantValue(wordCode, languageCode, uiLanguageConstants, useFromDefaultIfNotExists, true);
	}

	/**
	 * Resync all the values with Default Locale translations
	 *
	 * @param allLanguageConstants
	 * @param defaultLocale
	 */
	public void resyncWithDefaultLocale(Map<String, Map<String, String>> allLanguageConstants, String defaultLocale) {
		if (!allLanguageConstants.containsKey(defaultLocale)) {
			log.error("Default locale doesn't exists in the language codes: " + defaultLocale);
		}

		Map<String, String> defaultLanguageConstantsMap = allLanguageConstants.get(defaultLocale);
		for (Map.Entry<String, Map<String, String>> languageConstantsEntry : allLanguageConstants.entrySet()) {
			if (defaultLocale.equals(languageConstantsEntry.getKey())) {
				continue;
			}

			Map<String, String> languageConstants = languageConstantsEntry.getValue();
			boolean isTestLanguage = TEST_LANGUAGE_LOCALE.equals(languageConstantsEntry.getKey()) || TEST_LANGUAGE_CODE.equals(languageConstantsEntry.getKey());
			for (Map.Entry<String, String> defaultLocaleItem: defaultLanguageConstantsMap.entrySet()) {
				if (isTestLanguage) {
					languageConstants.put(defaultLocaleItem.getKey(), defaultLocaleItem.getKey());
				} else if (!languageConstants.containsKey(defaultLocaleItem.getKey()) || StringUtils.isEmpty(languageConstants.get(defaultLocaleItem.getKey()))) {
					languageConstants.put(defaultLocaleItem.getKey(), defaultLocaleItem.getValue());
				}
			}
		}
	}

	public Map<String, String> getServerConstantsByLocale(String locale) {
		return serverLanguageConstants.get(locale);
	}

	/**
	 * Get Language Constant Value details
	 *
	 * @return Language Constant Value Details
	 */
	public LanguageConstantValues getLanguageConstantValue(Long itemId) {
		LanguageConstantValues itemDetails;

		try {
			itemDetails = languageConstantValueRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Language Constant Value not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	public FilteredResponse<LanguageConstantFilter, LanguageConstantValueViewDTO> getLanguageConstantValuesListFiltered(String languageCode, FilteredRequest<LanguageConstantFilter> filteredRequest) {

		// Set Language Code
		filteredRequest.getFilter().setLanguageCode(languageCode);
//		filteredRequest.getFilter().setScope(LanguageConstantScopeType.UI);

		PagedResult<LanguageConstantValueViewDTO> result = languageConstantValueModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<LanguageConstantFilter, LanguageConstantValueViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get List of Language Constants with Values for Language
	 *
	 * @return Language Constants and Values Map
	 */
	public Map<String, String> getListForLanguage(String languageCode) {

		if (LanguageConstantService.uiLanguageConstants.containsKey(languageCode)) {
			return LanguageConstantService.uiLanguageConstants.get(languageCode);
		}

		return LanguageConstantService.uiLanguageConstants.get(DEFAULT_LANGUAGE_LOCALE);
	}

	/**
	 * Get List of Language Constants with Values for Language
	 *
	 * @return Language Constants and Values Map
	 */
	public Map<String, String> getListForLanguageOld(String languageCode) {

		SupportedLanguages language = supportedLanguagesService.getSupportedLanguage(languageCode);

		List<LanguageConstantValues> items = languageConstantValueRepository.getListByLanguageForAllConstants(language);

		// In case if current language is used to detect constants codes we must show constant code instead of value
		final Boolean isTestCodesLanguage = TEST_LANGUAGE_CODE.equalsIgnoreCase(languageCode);

		Map<String, String> resultMap = items.stream()
			.collect(Collectors.toMap(
					lcValue -> lcValue.getLanguageConstant().getName(),
					lcValue -> !isTestCodesLanguage ? lcValue.getValue() : lcValue.getLanguageConstant().getName(),
					(o1, o2) -> o1, LinkedHashMap::new
				)
			);

		return resultMap;
	}

	/**
	 * Insert data from JSON file
	 */
	@Deprecated
	@Transactional
	public ImportResultDTO importLanguageConstantsFromJSONFile(String languageCode, MultipartFile file) {

		Optional<SupportedLanguages> language = supportedLanguagesRepository.findFirstByCode(languageCode);

		InputStream fileContentStream = null;
		ImportResultDTO result = new ImportResultDTO();

		try {
			fileContentStream = file.getInputStream();

			try {
				fileContentStream.reset();
			} catch (IOException exception) { }

			// Parse JSON file
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> dataMap = objectMapper.readValue(fileContentStream, HashMap.class);

			if (language.isPresent()) {
				result = importLanguageConstantsFromStringMap(language.get(), LanguageConstantScopeType.UI, dataMap);
				// reload updated language constant values to server resources
				this.loadLanguageConstants();
			} else {
				throw new BadRequestException("Language does not exists!. Import Failed.");
			}

		} catch (IOException exception) {
			log.warn(exception.getMessage(), exception);
		}

		return result;
	}

	/**
	 * Insert data from CSV file
	 */
	@Transactional
	public ImportResultDTO importLanguageConstantsFromCSVFile(String languageCode, MultipartFile file) {

		Optional<SupportedLanguages> language = supportedLanguagesRepository.findFirstByCode(languageCode);

		InputStream fileContentStream = null;
		ImportResultDTO result = new ImportResultDTO();

		try {
			fileContentStream = file.getInputStream();

			try {
				fileContentStream.reset();
			} catch (IOException exception) { }

			// Parse CSV file
			Reader reader = new InputStreamReader(fileContentStream, Charset.defaultCharset());
			CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim();
			CSVParser csvParser =csvFormat.parse(reader);

			List<CSVRecord> csvRecordList = csvParser.getRecords();
			if (language.isPresent()) {
				result = importLanguageConstantsFromCSVItems(language.get(), csvRecordList);
				// Reload updated language constant values to server resources
				this.loadLanguageConstants();
			} else {
				throw new BadRequestException("Language does not exists!. Import Failed.");
			}

		} catch (IOException exception) {
			log.warn(exception.getMessage(), exception);
		}

		return result;
	}

	/**
	 * Insert data from JSON file
	 */
	@Transactional
	public ImportResultDTO importLanguageConstantsFromStringMap(SupportedLanguages language, LanguageConstantScopeType scopeType, Map<String, String> dataMap) {

		ImportResultDTO result = new ImportResultDTO();

		for (Map.Entry<String, String> dataRecord : dataMap.entrySet()) {
			Optional<LanguageConstants> languageConstantOptional = languageConstantRepository.findFirstByNameAndScope(dataRecord.getKey(), scopeType);
			LanguageConstants languageConstant;

			if (languageConstantOptional.isPresent()) {
				languageConstant = languageConstantOptional.get();
			} else {
				try {
					// create language constant
					languageConstant = new LanguageConstants();
					languageConstant.setName(dataRecord.getKey());
					languageConstant.setScope(scopeType);
					// save language constant
					languageConstant = languageConstantRepository.save(languageConstant);
					result.getMessages().add(MessageFormat.format("New Language Constant [{0}] [id: {1}] was created", languageConstant.getName(), languageConstant.getId()));
				} catch (DataIntegrityViolationException exception) {
					log.warn(String.format("!!!! Failed to create new language constant: %s, %s", dataRecord.getKey(), scopeType));
					continue;
				}
			}

			Optional<LanguageConstantValues> languageConstantValueOptional = languageConstantValueRepository.findFirstByLanguageIdAndLanguageConstantId(language.getId(), languageConstant.getId());
			LanguageConstantValues languageConstantValue;
			if (languageConstantValueOptional.isPresent()) {
				languageConstantValue = languageConstantValueOptional.get();
				languageConstantValue.setValue(dataRecord.getValue());

			} else {
				// create language constant value
				languageConstantValue = new LanguageConstantValues();
				languageConstantValue.setLanguage(language);
				languageConstantValue.setLanguageConstant(languageConstant);
			}
			// update existed or new language constant value
			languageConstantValue.setValue(dataRecord.getValue());
			// save new or updated language constant value
			languageConstantValue = languageConstantValueRepository.save(languageConstantValue);
			if (languageConstantOptional.isPresent()) {
				result.getUpdated().add(new ItemViewDTO(languageConstantValue.getId(), MessageFormat.format("[{0}] - [{1}]", languageConstant.getName(), languageConstantValue.getValue())));
			} else {
				result.getCreated().add(new ItemViewDTO(languageConstantValue.getId(), MessageFormat.format("[{0}] - [{1}]", languageConstant.getName(), languageConstantValue.getValue())));
			}
		}

		return result;
	}

	/**
	 * Insert data from CSV file
	 */
	public ImportResultDTO importLanguageConstantsFromCSVItems(SupportedLanguages language, List<CSVRecord> csvRecordList) {

		ImportResultDTO result = new ImportResultDTO();

		for (CSVRecord csvRecord : csvRecordList) {
			Optional<LanguageConstants> languageConstantOptional = languageConstantRepository.findFirstByName(csvRecord.get(CONSTANT_HEADER));
			LanguageConstants languageConstant;

			if (languageConstantOptional.isPresent()) {
				languageConstant = languageConstantOptional.get();
				languageConstant.setScope(LanguageConstantScopeType.of(csvRecord.get(SCOPE_HEADER)));
			} else {
				// create language constant
				languageConstant = new LanguageConstants();
				languageConstant.setName(csvRecord.get(CONSTANT_HEADER));
				languageConstant.setScope(LanguageConstantScopeType.of(csvRecord.get(SCOPE_HEADER)));
				// save language constant
				languageConstant = languageConstantRepository.save(languageConstant);
				result.getMessages().add(MessageFormat.format("New Language Constant [{0}] [id: {1}] was created", languageConstant.getName(), languageConstant.getId()));
			}

			Optional<LanguageConstantValues> languageConstantValueOptional = languageConstantValueRepository.findFirstByLanguageIdAndLanguageConstantId(language.getId(), languageConstant.getId());
			LanguageConstantValues languageConstantValue;
			if (languageConstantValueOptional.isPresent()) {
				languageConstantValue = languageConstantValueOptional.get();
				languageConstantValue.setValue(csvRecord.get(VALUE_HEADER));

			} else {
				// create language constant value
				languageConstantValue = new LanguageConstantValues();
				languageConstantValue.setLanguage(language);
				languageConstantValue.setLanguageConstant(languageConstant);
			}
			// update existed or new language constant value
			languageConstantValue.setValue(csvRecord.get(VALUE_HEADER));
			// save new or updated language constant value
			languageConstantValue = languageConstantValueRepository.save(languageConstantValue);
			if (languageConstantOptional.isPresent()) {
				result.getUpdated().add(new ItemViewDTO(languageConstantValue.getId(), MessageFormat.format("[{0}] - [{1}]", languageConstant.getName(), languageConstantValue.getValue())));
			} else {
				result.getCreated().add(new ItemViewDTO(languageConstantValue.getId(), MessageFormat.format("[{0}] - [{1}]", languageConstant.getName(), languageConstantValue.getValue())));
			}
		}

		return result;
	}


	/**
	 * Get JSON document with Language Constants Data for Language
	 *
	 * @param languageCode
	 * @return
	 */
	@Deprecated
	public ByteArrayInputStream getDownloadDataAsJSON(String languageCode) {
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectMapper mapper = new ObjectMapper();
			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

			SupportedLanguages language = supportedLanguagesRepository.findFirstByCode(languageCode).get();

			List<LanguageConstantValues> items = languageConstantValueRepository.getListByLanguageForAllConstants(language);
			Map<String, String> resultMap = items.stream()
				.collect(Collectors.toMap(lcValue -> lcValue.getLanguageConstant().getName(), lcValue -> lcValue.getValue(), (o1, o2) -> o1, LinkedHashMap::new));
			writer.writeValue(outputStream, resultMap);

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException exception) {
			log.warn(exception.getMessage(), exception);
			throw new InternalServerErrorException("Failed to generate CSV Template file for Language Constants");
		}

		return byteArrayInputStream;
	}

	/**
	 * Get CSV document with Language Constants Data for Language
	 *
	 * @param languageCode
	 * @return
	 */
	public ByteArrayInputStream getDownloadDataAsCSV(String languageCode) {
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createLanguageConstantCsvPrinter(outputStream);

			SupportedLanguages language = supportedLanguagesRepository.findFirstByCode(languageCode).get();
			List<LanguageConstantValues> items = languageConstantValueRepository.getListByLanguageForAllConstants(language);

			SupportedLanguages defaultLanguage = supportedLanguagesRepository.findFirstByCode(DEFAULT_LANGUAGE_CODE).get();
			List<LanguageConstantValues> defaultItems = languageConstantValueRepository.getListByLanguageForAllConstants(defaultLanguage);
			Map<Long, String> defaultItemsMap = defaultItems.stream().collect(Collectors.toMap(lcValue -> lcValue.getLanguageConstant().getId(), lcValue -> lcValue.getValue()));

			for(LanguageConstantValues value: items) {
				csvPrinter.printRecord(
					value.getLanguageConstant().getName(),
					value.getLanguageConstant().getScope().name(),
					value.getValue(),
					defaultItemsMap.get(value.getLanguageConstant().getId())
				);
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException exception) {
			log.warn(exception.getMessage(), exception);
			throw new InternalServerErrorException("Failed to generate CSV Template file for Language Constants");
		}

		return byteArrayInputStream;
	}

	/**
	 * Create CSV Printer to build Language Constant
	 *
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	private CSVPrinter createLanguageConstantCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(CONSTANT_HEADER, SCOPE_HEADER, VALUE_HEADER, DEFAULT_VALUE_HEADER);
		return new CSVPrinter(writer, csvFormat);
	}

	/**
	 * Update Language Constant Values List
	 *
	 * @return Updated Language Constant Values
	 */
	public List<LanguageConstantValueViewDTO> updateLanguageConstantValuesList(List<LanguageConstantValueViewDTO> itemsList, String languageCode) {

		SupportedLanguages language = supportedLanguagesService.getSupportedLanguage(languageCode);

		List<LanguageConstantValueViewDTO> result = new ArrayList<>();

		Boolean wasUpdated = false;
		for (LanguageConstantValueViewDTO itemDTO : itemsList) {

//			LanguageConstantValues entity = getLanguageConstantValue(itemDTO.getId());

			LanguageConstantValues entity;

			if (itemDTO.getId() != null) {
				entity = getLanguageConstantValue(itemDTO.getId());
			} else {
				entity = new LanguageConstantValues();
				entity.setLanguageConstant(itemDTO.getLanguageConstant());
				entity.setLanguage(language);
			}

			LanguageConstantValueViewDTO existingItemDTO = new LanguageConstantValueViewDTO(entity);

			// Save only if item changed
			if (!itemDTO.getValue().equals(existingItemDTO.getValue())) {
				entity.setValue(itemDTO.getValue());
				languageConstantValueRepository.save(entity);
				wasUpdated = true;
			}

			result.add(itemDTO);
		}

		// Reload all language constants if there were updates
		if (wasUpdated) {
			this.loadLanguageConstants();
		}

		return result;
	}

	/**
	 * Delete All Language Constant Values for Language
	 *
	 * @param languageCode 	code of language which language constant values should be deleted
	 * @return languageId
	 */
	@Transactional
	public Long clearVocabulary(String languageCode) {

		SupportedLanguages language = supportedLanguagesService.getSupportedLanguage(languageCode);

		List<LanguageConstantValues> itemsToDelete = languageConstantValueRepository.getListByLanguage(language);

		if(itemsToDelete.size() > 0) {
			languageConstantValueRepository.deleteAll(itemsToDelete);
			languageConstantValueRepository.flush();

			// Reload all language constants if there were updates
			this.loadLanguageConstants();
		}

		return language.getId();
	}

	/**
	 * Delete All Language Constant Values for Language
	 *
	 * @return
	 */
	@Transactional
	public void clearAllVocabularies() {

		languageConstantValueRepository.deleteAll();
		languageConstantValueRepository.flush();
		languageConstantRepository.deleteAll();
		languageConstantRepository.flush();

		// Reload all language constants if there were updates
		this.loadLanguageConstants();

	}
}
