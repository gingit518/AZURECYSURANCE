package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.language.LanguageViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Language;
import com.cyberintech.vrisk.server.repository.jpa.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Language management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Service
public class LanguageService {

	@Autowired
	private LanguageRepository languageRepository;

	/**
	 * Get Language List
	 *
	 * @return Language List
	 */
	public FilteredResponse<NameFilter, LanguageViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<Language> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, LanguageViewDTO> filteredResponse = new FilteredResponse<NameFilter, LanguageViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = languageRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = languageRepository.getCountByName(namePattern);

		List<LanguageViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, LanguageViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

}
