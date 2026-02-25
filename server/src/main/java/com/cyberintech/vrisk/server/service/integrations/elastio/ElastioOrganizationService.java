package com.cyberintech.vrisk.server.service.integrations.elastio;

import com.cyberintech.vrisk.server.model.dao.OrganizationModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.ElastioOrganizationFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.organization.ElastioOrganizationViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.PackagePlansDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.PackagePlans;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.PackagePlansRepository;
import com.cyberintech.vrisk.server.service.AuditLogService;
import com.cyberintech.vrisk.server.service.admin.AdminOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Organization management Service. Implements basic Organization logic.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-20
 */
@Service
@Slf4j
public class ElastioOrganizationService extends AdminOrganizationService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private OrganizationModelDAO organizationModelDAO;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private PackagePlansRepository packagePlansRepository;

	/**
	 * Get List of Organizations by Type and Filter
	 *
	 * @return Users List
	 */
	public FilteredResponse<ElastioOrganizationFilter, ElastioOrganizationViewDTO> getElastioListFiltered(FilteredRequest<ElastioOrganizationFilter> filteredRequest) {

		FilteredResponse<ElastioOrganizationFilter, ElastioOrganizationViewDTO> filteredResponse = new FilteredResponse<ElastioOrganizationFilter, ElastioOrganizationViewDTO>(filteredRequest);

		if (filteredRequest.getFilter() == null) {
			filteredRequest.setFilter(new ElastioOrganizationFilter());
		}

		OrganizationFilter filter = new OrganizationFilter();
		filter.setName(filteredRequest.getFilter().getName());
		filter.setPackagePlanIds(List.of(PackagePlans.PACKAGE_PLAN_ELASTIO));

		PagedResult<Organizations> pagedResult = organizationModelDAO.getItemsPageable(filter, filteredRequest.toPageRequest(), filteredRequest.getSort());

		List<ElastioOrganizationViewDTO> itemsDTOList = DTOBase.fromEntitiesList(pagedResult.getItems(), ElastioOrganizationViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(pagedResult.getCount().intValue());

		return filteredResponse;
	}

	/**
	 * Get organization Details to edit
	 *
	 * @param id
	 * @return
	 */
	public ElastioOrganizationViewDTO getElastioDetails(Long id) {
		Organizations organization = getOrganization(id);

		ElastioOrganizationViewDTO result = new ElastioOrganizationViewDTO(organization);

		return result;
	}

	/**
	 * Create new Organization
	 *
	 * @return New Organization
	 */
	public ElastioOrganizationViewDTO createElastio(ElastioOrganizationViewDTO newItemDTO) {

		PackagePlans elastioPackagePlan = packagePlansRepository.findById(PackagePlans.PACKAGE_PLAN_ELASTIO).get();

		// Throw Exception if ID is set in create mode
		Optional<Organizations> existingItemOpt = Optional.empty();
		if (newItemDTO.getId() != null) {
			existingItemOpt = organizationRepository.findByIdAndPackagePlan(newItemDTO.getId(), elastioPackagePlan);
		}
		if (existingItemOpt.isEmpty()) {
			existingItemOpt = organizationRepository.findFirstByNameAndOrganizationTypeAndPackagePlan(newItemDTO.getName(), OrganizationType.Organization, elastioPackagePlan);
		}

		PackagePlansDTO packagePlan = new PackagePlansDTO();
		packagePlan.setId(PackagePlans.PACKAGE_PLAN_ELASTIO);

		Organizations newItem = existingItemOpt.orElse(null);
		if (newItem == null) {
			newItem = new Organizations();
		}
		newItem.setOrganizationType(OrganizationType.Organization);
		newItem.setName(newItemDTO.getName());
		newItem.setDescription(newItemDTO.getDescription());
		newItem.setUid(newItemDTO.getUid());
		newItem.setPlatformType(newItemDTO.getPlatformType());
		newItem.setAssetType(newItemDTO.getAssetType());
		newItem.setAmountOfDataInTerabytes(newItemDTO.getAmountOfDataInTerabytes());
		newItem.setReplicationFactor(newItemDTO.getReplicationFactor());
		if (newItem.getUid() == null) newItem.setUid(UUID.randomUUID().toString());
		if (newItem.getCreatedAt() == null) newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());
		newItem.setPackagePlan(elastioPackagePlan);

		// TODO Apply Contact Email/Name

		// newItemDTO.setPackagePlan(packagePlan);
		// applyEntityChanges(newItemDTO, newItem);

		Organizations saveResult = organizationRepository.save(newItem);

		// Verify Elastio Package Plan setup
		verifyElastioPackagePlanSetup(newItem);

		ElastioOrganizationViewDTO result = new ElastioOrganizationViewDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.ORGANIZATION,
			saveResult.getId(),
			result,
			collectAuditLogItems(null, newItem.getRootParent() != null ? newItem.getRootParent().getId() : newItem.getId())
		);

		return result;
	}

}
