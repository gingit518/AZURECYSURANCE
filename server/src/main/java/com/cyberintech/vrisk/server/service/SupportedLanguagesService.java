package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.organization.SupportedLanguageEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.SupportedLanguageViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SupportedLanguages;
import com.cyberintech.vrisk.server.repository.jpa.SupportedLanguagesRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Supported Languages management Service. Implements basic user CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-03-16
 */
@Service
public class SupportedLanguagesService {

	@Autowired
	private SupportedLanguagesRepository supportedLanguagesRepository;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Supported Language
	 *
	 * @return Supported Language
	 */
	public SupportedLanguages getSupportedLanguage(Long languageId) {

		SupportedLanguages result;
		Optional<SupportedLanguages> language = supportedLanguagesRepository.findById(languageId);

		if (language.isPresent()) {
			result = language.get();
		} else {
			throw new BadRequestException("Supported Language does not exists!", ApplicationExceptionCodes.SUPPORTED_LANGUAGE_NOT_EXIST);
		}

		return result;
	}

	/**
	 * Get Supported Language by Code
	 *
	 * @return Supported Language
	 */
	public SupportedLanguages getSupportedLanguage(String languageCode) {

		SupportedLanguages result;
		Optional<SupportedLanguages> language = supportedLanguagesRepository.findFirstByCode(languageCode);

		if (language.isPresent()) {
			result = language.get();
		} else {
			throw new BadRequestException("Supported Language does not exists!", ApplicationExceptionCodes.SUPPORTED_LANGUAGE_NOT_EXIST);
		}

		return result;
	}

	/**
	 * Get Supported Language Details
	 *
	 * @return Supported Language Details
	 */
	public SupportedLanguageEditDTO getDetails(Long languageId) {

		SupportedLanguages item = getSupportedLanguage(languageId);

		SupportedLanguageEditDTO result = new SupportedLanguageEditDTO(item);

		return result;
	}

	/**
	 * Get Supported Languages List
	 *
	 * @return Supported Languages List
	 */
	public List<SupportedLanguageViewDTO> getList() {
		List<SupportedLanguages> entitiesList = supportedLanguagesRepository.findAll();

		List<SupportedLanguageViewDTO> result = DTOBase.fromEntitiesList(entitiesList, SupportedLanguageViewDTO.class);

		return result;
	}

	/**
	 * Get Supported Languages List for current Organization
	 *
	 * @return Supported Languages List
	 */
	public List<SupportedLanguageViewDTO> getListForCurrentOrganization() {
		Long organizationId = organizationService.getCurrentOrganizationId();

		List<SupportedLanguages> entitiesList = supportedLanguagesRepository.getListByOrganizationsId(organizationId);
		if (CollectionUtils.isEmpty(entitiesList)) {
			Optional<SupportedLanguages> defaultLanguage = supportedLanguagesRepository.findFirstByCode(LanguageConstantService.DEFAULT_LANGUAGE_CODE);
			if (defaultLanguage.isPresent()) {
				entitiesList = Arrays.asList(defaultLanguage.get());
			}
		}

		List<SupportedLanguageViewDTO> result = DTOBase.fromEntitiesList(entitiesList, SupportedLanguageViewDTO.class);

		return result;
	}

	/**
	 * Get Supported Languages List filtered
	 *
	 * @return Supported Languages List
	 */
	public FilteredResponse<NameFilter,SupportedLanguageViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest, Boolean onlyPublic) {

		FilteredResponse<NameFilter,SupportedLanguageViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		List<SupportedLanguages> items;
		Long count;
		if (Boolean.TRUE.equals(onlyPublic)) {
			items = supportedLanguagesRepository.getListByNameAndIsPublic(namePattern, filteredRequest.toPageRequest());
			count = supportedLanguagesRepository.getCountByNameAndIsPublic(namePattern);
		} else {
			items = supportedLanguagesRepository.getListByName(namePattern, filteredRequest.toPageRequest());
			count = supportedLanguagesRepository.getCountByName(namePattern);
		}

		List<SupportedLanguageViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, SupportedLanguageViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get public Supported Languages List
	 *
	 * @return Supported Languages List
	 */
	public List<SupportedLanguageViewDTO> getPublicLanguagesList() {

		List<SupportedLanguages> items = supportedLanguagesRepository.findAllByIsPublicIsTrue();

		List<SupportedLanguageViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, SupportedLanguageViewDTO.class);

		return itemsDTOList;
	}

	/**
	 * Update Supported Language Item
	 *
	 * @return Updated Supported Language Item
	 */
	public SupportedLanguageEditDTO update(SupportedLanguageEditDTO itemDTO) {

		// Get Existing item from the database
		SupportedLanguages existingItem = supportedLanguagesRepository.findById(itemDTO.getId()).get();

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		SupportedLanguages saveResult = supportedLanguagesRepository.save(existingItem);

		return new SupportedLanguageEditDTO(saveResult);
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(SupportedLanguageEditDTO itemDTO, SupportedLanguages entity) {

		entity.setIsPublic(itemDTO.getIsPublic());

	}
}
