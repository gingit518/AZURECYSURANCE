package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.ByParentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentWeightViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentWeights;
import com.cyberintech.vrisk.server.repository.jpa.AssessmentWeightsRepository;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Assessment Weights management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-08
 */
@Service
public class AssessmentWeightService {

	@Autowired
	private AssessmentWeightsRepository assessmentWeightsRepository;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Assessment Weights List
	 *
	 * @return Assessment Weights List
	 */
	public FilteredResponse<ByParentFilter, AssessmentWeightViewDTO> getListFiltered(FilteredRequest<ByParentFilter> filteredRequest) {
		List<AssessmentWeights> items = null;
		Long count = 0l;
		FilteredResponse<ByParentFilter, AssessmentWeightViewDTO> filteredResponse = new FilteredResponse<ByParentFilter, AssessmentWeightViewDTO>(filteredRequest);

		if (filteredRequest.getFilter() == null || filteredRequest.getFilter().getParentId() == null) {
			throw new BadRequestException("Parent Filter is required for subcategory");
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = assessmentWeightsRepository.getListByOrganizationAndSubcategory(organizationId, filteredRequest.getFilter().getParentId(), filteredRequest.toPageRequest());
		count = assessmentWeightsRepository.getCountByOrganizationAAndSubcategory(organizationId, filteredRequest.getFilter().getParentId());

		List<AssessmentWeightViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, AssessmentWeightViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

}
