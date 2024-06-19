package com.cyberintech.vrisk.server.service.admin;


import com.cyberintech.vrisk.server.model.dao.AdminBusinessUnitModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.BusinessUnitFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitViewExtDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import com.cyberintech.vrisk.server.repository.jpa.BusinessUnitRepository;
import com.cyberintech.vrisk.server.service.BusinessUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Admin extension for Business Unit management Service. Implements basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-06
 */
@Service
public class AdminBusinessUnitService extends BusinessUnitService {

	@Autowired
	private BusinessUnitRepository businessUnitRepository;

	@Autowired
	private AdminBusinessUnitModelDAO adminBusinessUnitModelDAO;

	/**
	 * Get Business Units List
	 *
	 * @return Business Units List
	 */
	@Override
	public List<BusinessUnitViewDTO> getList() {
		List<BusinessUnits> items;

		items = businessUnitRepository.findAll();

		List<BusinessUnitViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, BusinessUnitViewDTO.class);

		return itemsDTOList;
	}

	/**
	 * Get Business Units List Filtered
	 *
	 * @return Business Units List
	 */
	public FilteredResponse<BusinessUnitFilter, BusinessUnitViewExtDTO> getAdminListFiltered(FilteredRequest<BusinessUnitFilter> filteredRequest) {

		PagedResult<BusinessUnitViewExtDTO> result = adminBusinessUnitModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<BusinessUnitFilter, BusinessUnitViewExtDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}
}
