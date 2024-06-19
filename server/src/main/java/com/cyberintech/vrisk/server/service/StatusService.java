package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.status.StatusViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Status;
import com.cyberintech.vrisk.server.repository.jpa.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Status management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Service
public class StatusService {

	@Autowired
	private StatusRepository statusRepository;

	/**
	 * Get Status List
	 *
	 * @return Status List
	 */
	public FilteredResponse<NameFilter, StatusViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<Status> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, StatusViewDTO> filteredResponse = new FilteredResponse<NameFilter, StatusViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = statusRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = statusRepository.getCountByName(namePattern);

		List<StatusViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, StatusViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

}
