package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.LocationFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.state.StateViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.State;
import com.cyberintech.vrisk.server.repository.jpa.StateRepository;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * State management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Service
public class StateService {

	@Autowired
	private StateRepository stateRepository;

	/**
	 * Get State List
	 *
	 * @return State List
	 */
	public FilteredResponse<LocationFilter, StateViewDTO> getListFiltered(FilteredRequest<LocationFilter> filteredRequest) {
		List<State> items = null;
		Long count = 0l;
		FilteredResponse<LocationFilter, StateViewDTO> filteredResponse = new FilteredResponse<LocationFilter, StateViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long countryId = null;
		if (filteredRequest.getFilter() != null && CollectionUtils.isNotEmpty(filteredRequest.getFilter().getCountryIds())) {
			List<Long> countryIds = filteredRequest.getFilter().getCountryIds();
			items = stateRepository.getListByNameAndCountries(countryIds, namePattern, filteredRequest.toPageRequest());
			count = stateRepository.getCountByNameAndCountries(countryIds, namePattern);
		} else if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getCountryId() != null) {
			countryId = filteredRequest.getFilter().getCountryId();
			items = stateRepository.getListByName(countryId, namePattern, filteredRequest.toPageRequest());
			count = stateRepository.getCountByName(countryId, namePattern);
		} else {
			throw new BadRequestException("Country Id is not specified");
		}

		List<StateViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, StateViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

}
