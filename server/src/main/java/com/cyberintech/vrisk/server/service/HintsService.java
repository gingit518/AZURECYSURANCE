package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitEditDTO;
import com.cyberintech.vrisk.server.model.dto.hints.HintImportDTO;
import com.cyberintech.vrisk.server.model.dto.hints.HintLocalizedViewDTO;
import com.cyberintech.vrisk.server.model.dto.hints.HintsDTO;
import com.cyberintech.vrisk.server.model.dto.language_constants.LanguageConstantValueViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.HintType;
import com.cyberintech.vrisk.server.model.jpa.domains.LanguageConstantScopeType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.HintsRepository;
import com.cyberintech.vrisk.server.repository.jpa.LanguageConstantValueRepository;
import com.cyberintech.vrisk.server.repository.jpa.SupportedLanguagesRepository;
import com.cyberintech.vrisk.server.rest.exception.*;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hints management Service. Implements basic hints CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-04
 */
@Service
@Slf4j
public class HintsService {

	public static final String HINT_CODE = "Code";
	public static final String HINT_NAME = "Name";
	public static final String HINT_TYPE = "Type";
	public static final String HINT_TITLE_CODE = "Title Code";
	public static final String HINT_BODY_CODE = "Body Code";
	public static final String HINT_FOOTER_CODE = "Footer Code";
	public static final String HINT_BODY_TRANSLATION = "Body";
	public static final String HINT_HEADER_TRANSLATION = "Header";
	public static final String HINT_FOOTER_TRANSLATION = "Footer";
	public static final String HINT_LINK = "Link";
	public static final String HINT_PROPERTIES = "Properties";
	public static final String HINT_EXTERNAL_ID = "External Id";

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private Environment environment;

	@Autowired
	private HintsRepository hintsRepository;

	@Autowired
	private LanguageConstantValueRepository languageConstantValueRepository;

	@Autowired
	private LanguageConstantService languageConstantService;

	@Autowired
	private SupportedLanguagesRepository supportedLanguagesRepository;

	@Autowired
	private UserService userService;

	/**
	 * Get Hints List
	 *
	 * @return Hints List
	 */
	public List<HintsDTO> getList() {
		List<Hints> items = hintsRepository.findAll();

		List<HintsDTO> itemDTOs = DTOBase.fromEntitiesList(items, HintsDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Hints List by its codes
	 *
	 * @return Hints List
	 */
	public List<HintLocalizedViewDTO> getList(List<String> codes, String language) {

		List<Hints> items = hintsRepository.getListByCodes(codes);

		if ("true".equalsIgnoreCase(environment.getProperty("vrisk.setup-mode.enabled"))) {
			log.info("Hints auto-create mode enabled");
			Set<String> existingItemsCodes = items.stream().map(Hints::getCode).collect(Collectors.toSet());
			boolean isNewItemsCreated = false;
			for (String code : codes) {
				try {
					if (!existingItemsCodes.contains(code)) {
						// Auto Create new code
						log.info(String.format("## Hint not exists [%s] auto creating", code));
						String hintName = StringUtils.capitalize(code.replaceAll("\\_", " ").toLowerCase());
						HintsDTO itemDTO = new HintsDTO();
						itemDTO.setCode(code);
						itemDTO.setName(hintName);
						itemDTO.setHintType(HintType.Simple);
						itemDTO.getTranslations().put(LanguageConstantService.DEFAULT_LANGUAGE_CODE, Map.of("body", hintName));
						itemDTO = create(itemDTO);

						isNewItemsCreated = true;
					}
				} catch (ConflictException exception) {
					// Just silently ignore
				}
			}

			if (isNewItemsCreated) {
				items = hintsRepository.getListByCodes(codes);
				languageConstantService.reloadHintsLanguageConstants();
			}
		}

		List<HintLocalizedViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, HintLocalizedViewDTO.class);
		for (HintLocalizedViewDTO itemDTO : itemDTOs) {
			if (LanguageConstantService.TEST_LANGUAGE_CODE.equalsIgnoreCase(language)) {
				itemDTO.setTitle(itemDTO.getTitle());
				itemDTO.setBody(itemDTO.getName());
				itemDTO.setFooter(itemDTO.getFooter());
			} else {
				itemDTO.setBody(LanguageConstantService.getHintLanguageConstantValue(itemDTO.getBody(), language));
				if (HintType.Rich.equals(itemDTO.getHintType())) {
					itemDTO.setTitle(LanguageConstantService.getHintLanguageConstantValue(itemDTO.getTitle(), language));
					itemDTO.setFooter(LanguageConstantService.getHintLanguageConstantValue(itemDTO.getFooter(), language));
				} else {
					itemDTO.setTitle(null);
					itemDTO.setFooter(null);
				}
			}
		}

		return itemDTOs;
	}

	/**
	 * Get Hints List
	 *
	 * @return Hints List
	 */
	public FilteredResponse<NameFilter, HintsDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest, Boolean includeTranslations) {
		List<Hints> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, HintsDTO> filteredResponse = new FilteredResponse<NameFilter, HintsDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = hintsRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = hintsRepository.getCountByName(namePattern);

		List<HintsDTO> itemsDTOList = DTOBase.fromEntitiesList(items, HintsDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		if (Boolean.TRUE.equals(includeTranslations)) {
			List<SupportedLanguages> languages = supportedLanguagesRepository.findAll();
			for (SupportedLanguages supportedLanguage : languages) {
				if (LanguageConstantService.TEST_LANGUAGE_CODE.equalsIgnoreCase(supportedLanguage.getCode())) continue;

				String language = supportedLanguage.getCode();
				// Prepare Default Translations List
				for (HintsDTO itemDTO : itemsDTOList) {
					Map<String, String> translations = new HashMap<>();
					itemDTO.getTranslations().put(language, translations);
					translations.put("body", LanguageConstantService.getHintLanguageConstantValue(itemDTO.getBody(), language));
					if (HintType.Rich.equals(itemDTO.getHintType())) {
						translations.put("title", LanguageConstantService.getHintLanguageConstantValue(itemDTO.getTitle(), language, false, false));
						translations.put("footer", LanguageConstantService.getHintLanguageConstantValue(itemDTO.getFooter(), language, false, false));
					}
				}
			}
		}

		return filteredResponse;
	}

	/**
	 * Get Hint details
	 *
	 * @return Hint Details
	 */
	public Hints getItem(Long itemId) {
		Hints itemDetails;

		try {
			itemDetails = hintsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Hint not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Get Hint DTO details
	 *
	 * @return Hint Details
	 */
	public HintsDTO getDetails(Long itemId) {

		Hints itemDetails = getItem(itemId);

		HintsDTO result = new HintsDTO(itemDetails);

		List<LanguageConstantValues> languageConstantValues = new ArrayList<>();
		List<String> ignoredLanguages = Arrays.asList(LanguageConstantService.TEST_LANGUAGE_CODE);
		if (HintType.Simple.equals(itemDetails.getHintType())) {
			languageConstantValues = languageConstantValueRepository.getListByConstantCodes(Arrays.asList(itemDetails.getBody()), ignoredLanguages);
		} else {
			languageConstantValues = languageConstantValueRepository.getListByConstantCodes(Arrays.asList(itemDetails.getTitle(), itemDetails.getBody(), itemDetails.getFooter()), ignoredLanguages);
		}

		// Prepare Translations List
		List<LanguageConstantValueViewDTO> translations = languageConstantValues.stream().map(LanguageConstantValueViewDTO::new).collect(Collectors.toList());
		// result.setLanguageConstants(translations);

		applyTranslations(result, translations);

		return result;
	}

	/**
	 * Apply Translations to hint DTO
	 *
	 * @param hintDTO
	 * @param translations
	 */
	private void applyTranslations(HintsDTO hintDTO, List<LanguageConstantValueViewDTO> translations) {
		// fill language constant
		for (LanguageConstantValueViewDTO languageConstant : translations) {
			if (!hintDTO.getTranslations().containsKey(languageConstant.getLanguage().getCode())) hintDTO.getTranslations().put(languageConstant.getLanguage().getCode(), new HashMap<>());

			Map<String, String> codesMap = hintDTO.getTranslations().get(languageConstant.getLanguage().getCode());
			String localCode = "";
			if (hintDTO.getTitle().equals(languageConstant.getLanguageConstant().getName())) localCode = "title";
			if (hintDTO.getFooter().equals(languageConstant.getLanguageConstant().getName())) localCode = "footer";
			if (hintDTO.getBody().equals(languageConstant.getLanguageConstant().getName())) localCode = "body";

			codesMap.put(localCode, StringUtils.isNotEmpty(languageConstant.getValue()) ? languageConstant.getValue() : languageConstant.getDefaultValue());
		}
	}

	/**
	 * Import hints from the JSON Array
	 *
	 * @return New Technology Categorie
	 */
	public List<HintsDTO> importFromLinks(List<HintImportDTO> hintsDetailsList) {
		List<HintsDTO> result = new ArrayList<>();

		for (HintImportDTO hintImportDTO : hintsDetailsList) {
			String code = buildCode(hintImportDTO.getName());

			if (StringUtils.isEmpty(code)) {
				log.warn(String.format("Empty code for the item: %s", hintImportDTO.getLink()));
				continue;
			}

			// Verify Hint with such name not exists
			Optional<Hints> existingHintOptional = hintsRepository.findFirstByCodeAndIdNotIn(code, Arrays.asList(0l));
			if (!existingHintOptional.isPresent()) {

				String label = LanguageConstantService.getUILanguageConstantValue(hintImportDTO.getName(), LanguageConstantService.DEFAULT_LANGUAGE_CODE, false);
				if (StringUtils.isEmpty(label)) label = hintImportDTO.getName();

				Map<String, String> translations = new HashMap<>();

				HintsDTO itemDTO = new HintsDTO();
				itemDTO.setCode(code);
				itemDTO.setName(label);
				itemDTO.setHintType("LINK".equalsIgnoreCase(hintImportDTO.getType()) ? HintType.Rich : HintType.Simple);
				itemDTO.getTranslations().put(LanguageConstantService.DEFAULT_LANGUAGE_CODE, translations);

				translations.put("body", label);
				if (HintType.Rich.equals(itemDTO.getHintType())) {
					translations.put("title", label);
				}

				HintsDTO newHint = create(itemDTO);

				result.add(newHint);
			}
		}

		// languageConstantService.reloadHintsLanguageConstants();

		return result;
	}

	/**
	 * Create new Hint Domain
	 *
	 * @return New Technology Categorie
	 */
	public HintsDTO create(HintsDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		String name = newItemDTO.getName();

//		Hints newItem = newItemDTO.toEntity();
		Hints newItem = new Hints();
		applyEntityChanges(newItemDTO, newItem);
		Hints saveResult = hintsRepository.save(newItem);

		HintsDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.HINT,
			saveResult.getId(),
			result,
			null
		);

		return result;
	}

	/**
	 * Update Technology Categorie
	 *
	 * @return Updated Qualitative Domains
	 */
	public HintsDTO update(HintsDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		Hints existingItem = getItem(itemDTO.getId());
		HintsDTO existingItemDTO = new HintsDTO(existingItem);

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Hints saveResult = hintsRepository.save(existingItem);

		HintsDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.HINT,
			saveResult.getId(),
			existingItemDTO,
			result,
			null
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(HintsDTO itemDTO, Hints entity) {

		String code = buildCode(itemDTO.getCode());

		if (StringUtils.isEmpty(code)) {
			throw new BadRequestException(MessageFormat.format("Hint code cannot be empty {0}", code), ApplicationExceptionCodes.HINT_CODE_EMPTY);
		}

		// Verify Hint with such name not exists
		if (hintsRepository.findFirstByCodeAndIdNotIn(code, Arrays.asList(entity.getId() != null ? entity.getId() : 0l)).isPresent()) {
			throw new ConflictException(MessageFormat.format("Hint with this name already exist {0}", code), ApplicationExceptionCodes.HINT_ALREADY_EXISTS);
		}

		// Apply entity changes
		entity.setName(itemDTO.getName());
		entity.setCode(code);
		entity.setHintType(itemDTO.getHintType());
		entity.setTitle(buildTitleCode(code));
		entity.setBody(buildBodyCode(code));
		entity.setFooter(buildFooterCode(code));
		if (itemDTO.getExternalId() != null) entity.setExternalId(itemDTO.getExternalId());
		entity.setLink(itemDTO.getLink());
		entity.setProperties(itemDTO.getProperties());

		// Save translations
		/*
		Map<Long, Map<String, String>> languageConstantsMap = new HashMap<>();
		for (LanguageConstantValueViewDTO translation : itemDTO.getLanguageConstants()) {
			if (!languageConstantsMap.containsKey(translation.getLanguage().getId())) {
				languageConstantsMap.put(translation.getLanguage().getId(), new HashMap<>());
			}
			Map<String, String> constantsMap = languageConstantsMap.get(translation.getLanguage().getId());
			constantsMap.put(translation.getLanguageConstant().getName(), translation.getValue());
		}
		for (Map.Entry<Long, Map<String, String>> langInfoMapEntry : languageConstantsMap.entrySet()) {
			Long languageId = langInfoMapEntry.getKey();
			Optional<SupportedLanguages> language = supportedLanguagesRepository.findById(languageId);
			languageConstantService.importLanguageConstantsFromStringMap(language.get(), LanguageConstantScopeType.HINTS, langInfoMapEntry.getValue());
		}
		*/
		boolean isLanguageUpdated = false;
		if (itemDTO.getTranslations() != null && itemDTO.getTranslations().size() > 0) {
			for (Map.Entry<String, Map<String, String>> langInfoMapEntry : itemDTO.getTranslations().entrySet()) {
				String languageCode = langInfoMapEntry.getKey();
				Optional<SupportedLanguages> language = supportedLanguagesRepository.findFirstByCode(languageCode);
				if (language.isPresent()) {
					HashMap<String, String> codesMap = new HashMap<>();
					if (langInfoMapEntry.getValue().containsKey("title")) codesMap.put(entity.getTitle(), langInfoMapEntry.getValue().get("title"));
					if (langInfoMapEntry.getValue().containsKey("body")) codesMap.put(entity.getBody(), langInfoMapEntry.getValue().get("body"));
					if (langInfoMapEntry.getValue().containsKey("footer")) codesMap.put(entity.getFooter(), langInfoMapEntry.getValue().get("footer"));

					try {
						languageConstantService.importLanguageConstantsFromStringMap(language.get(), LanguageConstantScopeType.HINTS, codesMap);
						isLanguageUpdated = true;
					} catch (Exception e) {
						;
					}
				}
			}
		}

		// Reloading Hints language constants
		if (isLanguageUpdated) {
			// languageConstantService.reloadHintsLanguageConstants();
		}
	}

	/**
	 * Build code variable name
	 *
	 * @param code
	 * @return
	 */
	public static String buildCode(String code) {
		return StringUtils.substring(code.replaceAll("[\\$\\.]", "_").replaceAll("[^a-zA-Z0-9\\.\\_]", "").toUpperCase(), 0, 108);
	}

	/**
	 * Build code for body variable
	 * @param code
	 * @return
	 */
	public static String buildBodyCode(String code) {
		return "HINT." + code + ".BODY";
	}

	/**
	 * Build code for title variable
	 * @param code
	 * @return
	 */
	public static String buildTitleCode(String code) {
		return "HINT." + code + ".TITLE";
	}

	/**
	 * Build code for body variable
	 * @param code
	 * @return
	 */
	public static String buildFooterCode(String code) {
		return "HINT." + code + ".FOOTER";
	}

	/**
	 * Deletes Technology Categorie
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Hints existingItem = getItem(itemId);
		HintsDTO existingItemDTO = new HintsDTO(existingItem);
		hintsRepository.delete(existingItem);
		hintsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.HINT,
			existingItemDTO.getId(),
			existingItemDTO,
			null
		);

		return itemId;
	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getDownloadData() {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;

		try {

			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			jsonMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			List<SupportedLanguages> languages = supportedLanguagesRepository.findAll().stream()
				.filter(supportedLanguages -> !LanguageConstantService.TEST_LANGUAGE_CODE.equalsIgnoreCase(supportedLanguages.getCode()))
				.collect(Collectors.toList());

			Writer writer = new OutputStreamWriter(outputStream);

			// Build Headers List
			ArrayList<String> headers = new ArrayList<>();
			headers.addAll(Arrays.asList(HINT_CODE, HINT_NAME, HINT_TYPE/*, HINT_TITLE_CODE, HINT_BODY_CODE, HINT_FOOTER_CODE*/));
			for (SupportedLanguages language : languages) {
				headers.add(HINT_HEADER_TRANSLATION + " (" + language.getCode() + ")");
				headers.add(HINT_BODY_TRANSLATION + " (" + language.getCode() + ")");
				headers.add(HINT_FOOTER_TRANSLATION + " (" + language.getCode() + ")");
			}
			headers.addAll(Arrays.asList(HINT_LINK, HINT_PROPERTIES, HINT_EXTERNAL_ID));
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0]));

			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

			FilteredRequest<NameFilter> filteredRequest = new FilteredRequest<>();
			filteredRequest.setSize(Integer.MAX_VALUE);
			filteredRequest.setSort(BaseSort.of("code", BaseSort.SortOrder.ASC));
			FilteredResponse<NameFilter, HintsDTO> filteredItems = getListFiltered(filteredRequest, true);
			List<HintsDTO> items = filteredItems.getItems();
			for (HintsDTO itemDTO : items) {

				ArrayList<String> dataRecord = new ArrayList<>();
				dataRecord.addAll(Arrays.asList(
					itemDTO.getCode()
					, itemDTO.getName()
					, (itemDTO.getHintType() != null ? itemDTO.getHintType().name() : HintType.Simple.name())
					// , itemDTO.getTitle()
					// , itemDTO.getBody()
					// , itemDTO.getFooter()
				));
				for (SupportedLanguages language : languages) {
					if (HintType.Rich.equals(itemDTO.getHintType())) {
						dataRecord.add(itemDTO.getTranslations().containsKey(language.getCode()) ? itemDTO.getTranslations().get(language.getCode()).get("title") : "");
						dataRecord.add(itemDTO.getTranslations().containsKey(language.getCode()) ? itemDTO.getTranslations().get(language.getCode()).get("body") : "");
						dataRecord.add(itemDTO.getTranslations().containsKey(language.getCode()) ? itemDTO.getTranslations().get(language.getCode()).get("footer") : "");
					} else {
						dataRecord.add("");
						dataRecord.add(itemDTO.getTranslations().containsKey(language.getCode()) ? itemDTO.getTranslations().get(language.getCode()).get("body") : "");
						dataRecord.add("");
					}
				}
				dataRecord.add(itemDTO.getLink());
				if (itemDTO.getProperties() != null && itemDTO.getProperties().size() > 0) {
					dataRecord.add(jsonMapper.writeValueAsString(itemDTO.getProperties()));
				} else {
					dataRecord.add("");
				}
				dataRecord.add(itemDTO.getExternalId());

				csvPrinter.printRecord(dataRecord);
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to generate CSV Data file");
		}

		return byteArrayInputStream;
	}

	/**
	 * Insert Hints data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(MultipartFile file) {

		ImportResultDTO result = new ImportResultDTO();

		try {

			List<SupportedLanguages> languages = supportedLanguagesRepository.findAll().stream()
				.filter(supportedLanguages -> !LanguageConstantService.TEST_LANGUAGE_CODE.equalsIgnoreCase(supportedLanguages.getCode()))
				.collect(Collectors.toList());

			CSVParser csvParser = CSVUtils.createCSVParser(file.getInputStream());
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			for (CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				String code = Optional.ofNullable(csvRecord.get(HINT_CODE)).orElse("").trim();
				String name = (csvRecord.isMapped(HINT_NAME)) ? Optional.ofNullable(csvRecord.get(HINT_NAME)).orElse("").trim() : "";

				String hintTypeString = (csvRecord.isMapped(HINT_TYPE)) ? csvRecord.get(HINT_TYPE) : "";
				HintType type = null;
				if (HintType.Rich.name().equalsIgnoreCase(hintTypeString)) {
					type = HintType.Rich;
				} else if (HintType.Simple.name().equalsIgnoreCase(hintTypeString)) {
					type = HintType.Simple;
				}
				String link = (csvRecord.isMapped(HINT_LINK)) ? Optional.ofNullable(csvRecord.get(HINT_LINK)).orElse("").trim() : null;
				String externalId = (csvRecord.isMapped(HINT_EXTERNAL_ID)) ? Optional.ofNullable(csvRecord.get(HINT_EXTERNAL_ID)).orElse("").trim() : null;

				String propertiesString = (csvRecord.isMapped(HINT_PROPERTIES)) ? Optional.ofNullable(csvRecord.get(HINT_PROPERTIES)).orElse("").trim() : null;
				Map<String, Object> properties = null;
				if (StringUtils.isNotEmpty(propertiesString)) {
					ObjectMapper jsonMapper = new ObjectMapper();
					jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
					jsonMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
					properties = jsonMapper.readValue(propertiesString, Map.class);
				}

				Map<String, Map<String, String>> translations = new HashMap<>();
				for (SupportedLanguages language : languages) {
					Map<String, String> translation = new HashMap<>();
					translations.put(language.getCode(), translation);
					String bodyCode = HINT_BODY_TRANSLATION + " (" + language.getCode() + ")";
					String translationString = null;
					if (HintType.Rich.equals(type)) {
						String titleCode = HINT_HEADER_TRANSLATION + " (" + language.getCode() + ")";
						String footerCode = HINT_FOOTER_TRANSLATION + " (" + language.getCode() + ")";
						if (csvRecord.isMapped(titleCode)) {
							translationString = Optional.ofNullable(csvRecord.get(titleCode)).orElse("").trim();
							if (StringUtils.isNotEmpty(translationString)) translation.put("title", translationString);
						}
						if (csvRecord.isMapped(footerCode)) {
							translationString = Optional.ofNullable(csvRecord.get(footerCode)).orElse("").trim();
							if (StringUtils.isNotEmpty(translationString)) translation.put("footer", translationString);
						}
					}

					if (csvRecord.isMapped(bodyCode)) {
						translationString = Optional.ofNullable(csvRecord.get(bodyCode)).orElse("").trim();
						if (StringUtils.isNotEmpty(translationString)) translation.put("body", translationString);
					}
				}

				Optional<Hints> hintDetails = hintsRepository.findFirstByCode(code);
				HintsDTO itemDTO;
				if (hintDetails.isPresent()) {
					// Updating
					itemDTO = new HintsDTO(hintDetails.get());
				} else {
					itemDTO = new HintsDTO();
					itemDTO.setCode(code);
				}

				if (StringUtils.isNotEmpty(name)) {
					itemDTO.setName(name);
				} else if (StringUtils.isEmpty(itemDTO.getName())) {
					itemDTO.setName(code);
				}
				itemDTO.setLink(link);
				if (properties != null) itemDTO.setProperties(properties);
				itemDTO.setExternalId(externalId);
				itemDTO.setTranslations(translations);

				if (itemDTO.getId() != null) {
					update(itemDTO);

					// Add Updated item
					result.getUpdated().add(new ItemViewDTO(itemDTO.getId(), itemDTO.getName()));
				} else {
					HintsDTO newItem = create(itemDTO);

					// Add Create Item
					result.getUpdated().add(new ItemViewDTO(newItem.getId(), newItem.getName()));
				}
			}

			// Reloading Hints Translations
			languageConstantService.reloadHintsLanguageConstants();
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return result;
	}

}
