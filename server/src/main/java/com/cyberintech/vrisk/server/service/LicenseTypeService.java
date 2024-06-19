package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.budget.LicenseTypeDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.LicenseTypes;
import com.cyberintech.vrisk.server.repository.jpa.LicenseTypeRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * License Type management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Service
public class LicenseTypeService {

	@Autowired
	private LicenseTypeRepository licenseTypeRepository;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get License Type List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, LicenseTypeDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<LicenseTypes> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, LicenseTypeDTO> filteredResponse = new FilteredResponse<NameFilter, LicenseTypeDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = licenseTypeRepository.getListByOrganizationAndName(namePattern, filteredRequest.toPageRequest());
		count = licenseTypeRepository.getCountByOrganizationAndName(namePattern);

		List<LicenseTypeDTO> itemsDTOList = DTOBase.fromEntitiesList(items, LicenseTypeDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get License Type details
	 *
	 * @return License Type Details
	 */
	public LicenseTypes getLicenseType(Long itemId) {
		LicenseTypes itemDetails;

		try {
			itemDetails = licenseTypeRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("License Type not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Get License Type DTO details
	 *
	 * @return License Type Details
	 */
	public LicenseTypeDTO getDetails(Long itemId) {

		LicenseTypes itemDetails = getLicenseType(itemId);

		LicenseTypeDTO result = new LicenseTypeDTO(itemDetails);

		return result;
	}

}
