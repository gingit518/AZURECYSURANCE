package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.role.RoleListDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.repository.jpa.RoleRepository;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Roles management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Roles List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, RoleListDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<Roles> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, RoleListDTO> filteredResponse = new FilteredResponse<NameFilter, RoleListDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		boolean isSuperAdmin = userService.isSuperAdmin();
		Long minRoleId = isSuperAdmin ? 0l :8l;

		Organizations currentOrganization = organizationService.getCurrentOrganizationEntity();
		if (!isSuperAdmin && currentOrganization != null && currentOrganization.getPackagePlan() != null) {
			items = roleRepository.getRolesByNameForNonAdminAndPackagePlan(namePattern, currentOrganization.getPackagePlan().getId(), filteredRequest.toPageRequest());
			count = roleRepository.getCountRolesByNameForNonAdminAndPackagePlan(namePattern, currentOrganization.getPackagePlan().getId());
		} else {
			items = roleRepository.getRolesByNameForNonAdmin(namePattern, minRoleId, filteredRequest.toPageRequest());
			count = roleRepository.getCountRolesByNameForNonAdmin(namePattern, minRoleId);
		}

		List<RoleListDTO> itemsDTOList = DTOBase.fromEntitiesList(items, RoleListDTO.class);
		for (RoleListDTO roleDTO : itemsDTOList) {
			roleDTO.setIsLocked(RoleType.isRoleDefined(roleDTO.getName()));
		}

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Delete Risk Quant
	 *
	 * @return Deleted Quant
	 */
	@Transactional
	public Long delete(Long itemId) {
		Optional<Roles> itemDetails = roleRepository.findById(itemId);
		if (itemDetails.isPresent()) {
			Roles role = itemDetails.get();
			if (RoleType.isRoleDefined(role.getName())) {
				throw new BadRequestException("You are not allowed to delete system role");
			}

			// Clear role permissions
			role.getPermissions().clear();
			roleRepository.save(role);

			// Delete role itself
			roleRepository.delete(role);
		}

		return itemId;
	}

}
