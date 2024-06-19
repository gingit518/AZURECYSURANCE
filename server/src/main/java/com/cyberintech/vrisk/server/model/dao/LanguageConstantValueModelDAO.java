package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.LanguageConstantFilter;
import com.cyberintech.vrisk.server.model.dto.language_constants.LanguageConstantValueViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.SupportedLanguageViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.LanguageConstantScopeType;
import com.cyberintech.vrisk.server.model.jpa.entity.SupportedLanguages;
import com.cyberintech.vrisk.server.service.LanguageConstantService;
import com.cyberintech.vrisk.server.service.SupportedLanguagesService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Language Constant Value DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-04-21
 */
@Service
public class LanguageConstantValueModelDAO implements PageableModelDAO<LanguageConstantValueViewDTO, LanguageConstantFilter> {

	@Autowired
	private LanguageConstantService languageConstantService;

	@Autowired
	private SupportedLanguagesService supportedLanguagesService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<LanguageConstantValueViewDTO> getItemsPageable(LanguageConstantFilter filter, Pageable pageable, BaseSort sort) {

		// Detect filtered values
		String valueFilter = Optional.ofNullable(filter.getValue()).orElse("");
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		List<Long> excludeIds = null;
		if (filter != null && filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			excludeIds = filter.getExcludeIds();
		}
		LanguageConstantScopeType scope = filter.getScope();

		SupportedLanguages language = supportedLanguagesService.getSupportedLanguage(filter.getLanguageCode());
		SupportedLanguages defaultLanguage = supportedLanguagesService.getSupportedLanguage(languageConstantService.DEFAULT_LANGUAGE_CODE);

		// Define base hql data Query
		String hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.language_constants.LanguageConstantValueViewDTO(lc, lcv, dlcv.value) FROM LanguageConstants lc " +
			" LEFT JOIN FETCH LanguageConstantValues lcv ON lcv.languageConstant = lc AND lcv.language = :language " +
			" LEFT JOIN FETCH LanguageConstantValues dlcv ON dlcv.languageConstant = lc AND dlcv.language = :defaultLanguage ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(lc) FROM LanguageConstants lc " +
			" LEFT JOIN FETCH LanguageConstantValues lcv ON lcv.languageConstant = lc AND lcv.language = :language " +
			" LEFT JOIN FETCH LanguageConstantValues dlcv ON dlcv.languageConstant = lc AND dlcv.language = :defaultLanguage ";

		// Build Query String
		String whereString = " WHERE lc.id >= 0 ";
		if (StringUtils.isNotEmpty(valueFilter)) {
			whereString += " AND ( (UPPER(lcv.value) LIKE (CONCAT(UPPER(:value), '%'))) OR (UPPER(dlcv.value) LIKE (CONCAT(UPPER(:value), '%'))) OR (UPPER(lc.name) LIKE (CONCAT(UPPER(:value), '%'))) ) ";
		}
		if (excludeIds != null && excludeIds.size() > 0) {
			whereString += " AND lcv.id NOT IN :excludeIds";
		}
		if (StringUtils.isNotEmpty(nameFilter)) {
			whereString += " AND UPPER(lc.name) LIKE (CONCAT(UPPER(:name), '%'))";
		}
		if (scope != null) {
			whereString += " AND lc.scope = :scope";
		}

		// Build Sort based on mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "lc.id"),
			Map.entry("name", "lc.name"),
			Map.entry("value", "lcv.value"),
			Map.entry("defaultValue", "dlcv.value")
		);

		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		} else {
			searchQueryString += " ORDER BY lc.name ASC";
		}

		// Build Query data
		Query typedQuery = entityManager.createQuery(searchQueryString);
		applySearchFilterValues(filter, language, defaultLanguage, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<LanguageConstantValueViewDTO> resultList = typedQuery.getResultList();

		resultList.stream().forEach(languageConstantValueViewDTO -> {
			if (languageConstantValueViewDTO.getLanguage() == null) {
				languageConstantValueViewDTO.setLanguage(new SupportedLanguageViewDTO(language));
			}
		});

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, language, defaultLanguage, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<>(resultList, resultsCount);
	}

	/**
	 *
	 * @param filter
	 * @param language
	 * @param defaultLanguage
	 * @param query
	 */
	private void applySearchFilterValues(LanguageConstantFilter filter, SupportedLanguages language, SupportedLanguages defaultLanguage, Query query) {
		String valueFilter = Optional.ofNullable(filter.getValue()).orElse("");
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (StringUtils.isNotEmpty(valueFilter)) query.setParameter("value", valueFilter);
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (language != null) query.setParameter("language", language);
		if (defaultLanguage != null) query.setParameter("defaultLanguage", defaultLanguage);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (filter.getScope() != null) query.setParameter("scope", filter.getScope());
	}
}
