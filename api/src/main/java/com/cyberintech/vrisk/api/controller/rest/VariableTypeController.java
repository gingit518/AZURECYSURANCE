package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableTypeRelation;
import com.cyberintech.vrisk.server.model.jpa.entity.VariableTypes;
import com.cyberintech.vrisk.server.repository.jpa.VariableTypesRepository;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Variable Type controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = VariableTypeController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Variable Type Controller"
)
@Tag(name = "Variable Type")
public class VariableTypeController {

	static final String CONTROLLER_URI = "/api/variable-types";

	@Autowired
	private VariableTypesRepository variableTypesRepository;

	/**
	 * Get Variable Types List
	 *
	 * @return Variable Types List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/", name = "All Variable Types")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<ItemViewDTO<VariableTypes>> getList(
	) {

		List<VariableTypes> items = variableTypesRepository.findAll();
		List<ItemViewDTO<VariableTypes>> result = items.stream().map(ItemViewDTO<VariableTypes>::new).collect(Collectors.toList());

		return result;
	}

	/**
	 * Get Variable Types List for current Filters
	 *
	 * @return Variable Types List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Variable Types List filtered by relation")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, ItemViewDTO<VariableTypes>> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true)  @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, ItemViewDTO<VariableTypes>> result = new FilteredResponse<>(filteredRequest);

		VariableTypeRelation relation;
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			relation = VariableTypeRelation.of(filteredRequest.getFilter().getName());
			List<Long> excludeIds = Arrays.asList(0L);

			if (filteredRequest.getFilter().getExcludeIds() != null && filteredRequest.getFilter().getExcludeIds().size() > 0) {
				excludeIds = filteredRequest.getFilter().getExcludeIds();
			}

			List<VariableTypes> items = variableTypesRepository.getListByRelationName(relation, excludeIds, filteredRequest.toPageRequest());
			List<ItemViewDTO<VariableTypes>> itemDTOList = items.stream().map(ItemViewDTO<VariableTypes>::new).collect(Collectors.toList());
			Long count = variableTypesRepository.getCountByRelationName(relation, excludeIds);

			result.setItems(itemDTOList);
			result.setTotal(count.intValue());
		} else {
			throw new BadRequestException("Relation name is required in filter.");
		}

		return result;
	}

}
