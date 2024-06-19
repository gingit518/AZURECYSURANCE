package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.budget.CostTypeDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CostTypes;
import com.cyberintech.vrisk.server.repository.jpa.CostTypeRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Cost Type management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Service
public class CostTypeService {

	@Autowired
	private CostTypeRepository costTypeRepository;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Cost Type List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, CostTypeDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<CostTypes> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, CostTypeDTO> filteredResponse = new FilteredResponse<NameFilter, CostTypeDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = costTypeRepository.getListByOrganizationAndName(namePattern, filteredRequest.toPageRequest());
		count = costTypeRepository.getCountByOrganizationAndName(namePattern);

		List<CostTypeDTO> itemsDTOList = DTOBase.fromEntitiesList(items, CostTypeDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Cost Type details
	 *
	 * @return Cost Type Details
	 */
	public CostTypes getCostType(Long itemId) {
		CostTypes itemDetails;

		try {
			itemDetails = costTypeRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Cost Type not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Get Cost Type DTO details
	 *
	 * @return Cost Type Details
	 */
	public CostTypeDTO getDetails(Long itemId) {

		CostTypes itemDetails = getCostType(itemId);

		CostTypeDTO result = new CostTypeDTO(itemDetails);

		return result;
	}

}
