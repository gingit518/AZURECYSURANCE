package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.ByParentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityControlNameDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SecurityControlNames;
import com.cyberintech.vrisk.server.repository.jpa.SecurityControlNamesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Security Control Names management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-01-23
 */
@Service
public class SecurityControlNameService {

	@Autowired
	private SecurityControlNamesRepository securityControlNamesRepository;

	/**
	 * Get Security Control Names List
	 *
	 * @return Security Control Names List
	 */
	public FilteredResponse<ByParentFilter, SecurityControlNameDTO> getListFiltered(FilteredRequest<ByParentFilter> filteredRequest) {
		List<SecurityControlNames> items = null;
		Long count = 0l;
		FilteredResponse<ByParentFilter, SecurityControlNameDTO> filteredResponse = new FilteredResponse<ByParentFilter, SecurityControlNameDTO>(filteredRequest);

		Long parentId = null;
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getParentId() != null) {
			parentId = filteredRequest.getFilter().getParentId();
			// throw new BadRequestException("Parent Filter is required for category");
		}

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		if (parentId != null) {
			items = securityControlNamesRepository.getListByNameAndFamilyId(namePattern, filteredRequest.getFilter().getParentId(), filteredRequest.toPageRequest());
			count = securityControlNamesRepository.getCountByNameAndFamilyId(namePattern, filteredRequest.getFilter().getParentId());
		} else {
			items = securityControlNamesRepository.getListByName(namePattern, filteredRequest.toPageRequest());
			count = securityControlNamesRepository.getCountByName(namePattern);

		}

		List<SecurityControlNameDTO> itemsDTOList = DTOBase.fromEntitiesList(items, SecurityControlNameDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

}
