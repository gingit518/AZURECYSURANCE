package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.ByParentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityControlFamilyDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SecurityControlFamilies;
import com.cyberintech.vrisk.server.repository.jpa.SecurityControlFamiliesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Security Control Families management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-01-23
 */
@Service
public class SecurityControlFamilyService {

	@Autowired
	private SecurityControlFamiliesRepository securityControlNamesRepository;

	/**
	 * Get Security Control Families List
	 *
	 * @return Security Control Families List
	 */
	public FilteredResponse<ByParentFilter, SecurityControlFamilyDTO> getListFiltered(FilteredRequest<ByParentFilter> filteredRequest) {
		List<SecurityControlFamilies> items = null;
		Long count = 0l;
		FilteredResponse<ByParentFilter, SecurityControlFamilyDTO> filteredResponse = new FilteredResponse<ByParentFilter, SecurityControlFamilyDTO>(filteredRequest);

		Long parentId = null;
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getParentId() != null) {
			parentId = filteredRequest.getFilter().getParentId();
		}

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = securityControlNamesRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = securityControlNamesRepository.getCountByName(namePattern);

		List<SecurityControlFamilyDTO> itemsDTOList = DTOBase.fromEntitiesList(items, SecurityControlFamilyDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

}
