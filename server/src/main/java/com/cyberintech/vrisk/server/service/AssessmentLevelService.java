package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentLevelRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentLevels;
import com.cyberintech.vrisk.server.repository.jpa.AssessmentLevelsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Assessment Levels management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-10
 */
@Service
public class AssessmentLevelService {

	@Autowired
	private AssessmentLevelsRepository assessmentLevelsRepository;

	/**
	 * Get Assessment Levels List
	 *
	 * @return Assessment Levels List
	 */
	public FilteredResponse<NameFilter, AssessmentLevelRefDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<AssessmentLevels> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, AssessmentLevelRefDTO> filteredResponse = new FilteredResponse<NameFilter, AssessmentLevelRefDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = assessmentLevelsRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = assessmentLevelsRepository.getCountByName(namePattern);

		List<AssessmentLevelRefDTO> itemsDTOList = DTOBase.fromEntitiesList(items, AssessmentLevelRefDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

}
