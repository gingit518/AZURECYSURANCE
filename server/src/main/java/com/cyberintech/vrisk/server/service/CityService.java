package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.LocationFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.city.CityEditDTO;
import com.cyberintech.vrisk.server.model.dto.city.CityViewDTO;
import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.CityRepository;
import com.cyberintech.vrisk.server.repository.jpa.CountryRepository;
import com.cyberintech.vrisk.server.repository.jpa.StateRepository;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * City management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Service
public class CityService {


	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private StateRepository stateRepository;
	@Qualifier("organizationService")
	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get City List
	 *
	 * @return City List
	 */
	public FilteredResponse<LocationFilter, CityViewDTO> getListFiltered(FilteredRequest<LocationFilter> filteredRequest) {
		List<City> items = null;
		Long count = 0l;
		FilteredResponse<LocationFilter, CityViewDTO> filteredResponse = new FilteredResponse<LocationFilter, CityViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long countryId = null;
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getCountryId() != null) {
			countryId = filteredRequest.getFilter().getCountryId();
		} else {
			throw new BadRequestException("Country Id is not specified");
		}

		Long stateId = null;
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getStateId() != null) {
			stateId = filteredRequest.getFilter().getStateId();
		}

		if (countryId == null && stateId == null) {
			throw new BadRequestException("Country or State must be specified to search a City");
		}

		if (stateId != null) {
			items = cityRepository.getListByStateAndName(stateId, namePattern, filteredRequest.toPageRequest());
			count = cityRepository.getCountByStateAndName(stateId, namePattern);
		} else {
			items = cityRepository.getListByCountryAndName(countryId, namePattern, filteredRequest.toPageRequest());
			count = cityRepository.getCountByCountryAndName(countryId, namePattern);
		}

		List<CityViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, CityViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Create new Data Domain Domain
	 *
	 * @return New Data Domain
	 */
	public CityEditDTO safelyCreate(CityEditDTO newItemDTO) {

		Country country = newItemDTO.getCountry() != null && newItemDTO.getCountry().getId() != null ? countryRepository.findById(newItemDTO.getCountry().getId()).orElse(null) : null;
		State state = newItemDTO.getState() != null && newItemDTO.getState().getId() != null ? stateRepository.findById(newItemDTO.getState().getId()).orElse(null) : null;
		Optional<City> existingCity = cityRepository.findFirstByNameAndCountryAndState(newItemDTO.getName(), country, state);

		if (existingCity.isPresent()) {
			return new CityEditDTO(existingCity.get());
		}

		City newItem = new City();
		newItem.setName(newItemDTO.getName());
		newItem.setCountry(country);
		newItem.setState(state);
		City saveResult = cityRepository.save(newItem);

		CityEditDTO result = new CityEditDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.CITY,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId()));

		return result;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(CityEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
