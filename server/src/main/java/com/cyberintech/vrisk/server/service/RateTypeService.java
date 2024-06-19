package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.budget.RateTypeDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.RateTypes;
import com.cyberintech.vrisk.server.repository.jpa.RateTypeRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Rate Type management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Service
public class RateTypeService {

	@Autowired
	private RateTypeRepository rateTypeRepository;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Rate Type List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, RateTypeDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<RateTypes> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, RateTypeDTO> filteredResponse = new FilteredResponse<NameFilter, RateTypeDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = rateTypeRepository.getListByOrganizationAndName(namePattern, filteredRequest.toPageRequest());
		count = rateTypeRepository.getCountByOrganizationAndName(namePattern);

		List<RateTypeDTO> itemsDTOList = DTOBase.fromEntitiesList(items, RateTypeDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Rate Type details
	 *
	 * @return Rate Type Details
	 */
	public RateTypes getRateType(Long itemId) {
		RateTypes itemDetails;

		try {
			itemDetails = rateTypeRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Rate Type not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Get Rate Type DTO details
	 *
	 * @return Rate Type Details
	 */
	public RateTypeDTO getDetails(Long itemId) {

		RateTypes itemDetails = getRateType(itemId);

		RateTypeDTO result = new RateTypeDTO(itemDetails);

		return result;
	}

}
