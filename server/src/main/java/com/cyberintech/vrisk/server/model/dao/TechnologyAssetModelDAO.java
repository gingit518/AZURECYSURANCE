package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.SystemFilter;
import com.cyberintech.vrisk.server.model.data.TechnologyAssetFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.TechnologyAssetViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyAssets;
import com.cyberintech.vrisk.server.service.OrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TechnologyAsset DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2024-01-09
 */
@Service
public class TechnologyAssetModelDAO implements PageableModelDAO<TechnologyAssetViewDTO, TechnologyAssetFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<TechnologyAssetViewDTO> getItemsPageable(TechnologyAssetFilter filter, Pageable pageable, BaseSort sort) {
		// Detect filtered values
		Long organizationId = organizationService.getCurrentOrganizationId();

		// Define base hql data Query
		String hqlQuery = "SELECT sys FROM TechnologyAssets sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.businessUnit bu LEFT JOIN FETCH sys.infosecFocalPerson ip " +
			"LEFT JOIN FETCH sys.createdBy LEFT JOIN FETCH sys.updatedBy ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(sys) FROM TechnologyAssets sys ";

		// Build Query String
		String whereString = " WHERE sys.organizationId = :organizationId";
		if (StringUtils.isNotEmpty(filter.getName())) {
			// whereString += " AND UPPER(sys.name) LIKE (CONCAT(UPPER(:name), '%'))";
			whereString += " AND (UPPER(sys.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(sys.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (filter.getSystemStatus() != null) {
			whereString += " AND sys.systemStatus = :systemStatus";
		}
		if (StringUtils.isNotEmpty(filter.getIpAddress())) {
			whereString += " AND (UPPER(sys.ipAddress) LIKE CONCAT('%', UPPER(:ipAddress), '%'))";
		}
		if (filter.getTechnologyCategory() != null) {
			whereString += " AND sys.technologyCategory.id = :technologyCategoryId";
		}
		if (filter.getTechnology() != null) {
			whereString += " AND sys.technology.id = :technologyId";
		}
		if (filter.getManufacturer() != null) {
			whereString += " AND sys.manufacturer.id = :manufacturerId";
		}
		if (StringUtils.isNotEmpty(filter.getDiscoverySource())) {
			whereString += " AND (UPPER(sys.discoverySource) LIKE CONCAT('%', UPPER(:discoverySource), '%'))";
		}
		if (filter.getEndOfLife() != null) {
			whereString += " AND sys.eolDate < :endOfLife";
		}
		if (StringUtils.isNotEmpty(filter.getLocation())) {
			whereString += " AND (UPPER(sys.location) LIKE CONCAT('%', UPPER(:location), '%'))";
		}
		if (filter.getExcludeIds() != null) {
			whereString += " AND sys.id NOT IN :excludeIds";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "sys.id"),
			Map.entry("name", "sys.name"),
			Map.entry("description", "sys.description"),
			Map.entry("owner", "ow.fullName"),
			Map.entry("systemStatus", "sys.systemStatus"),
			Map.entry("numberOfRecProcessed", "sys.numberOfRecProcessed"),
			Map.entry("businessUnit", "bu.name"),
			Map.entry("dataAssetClassification", "dac.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<TechnologyAssets> typedQuery = entityManager.createQuery(searchQueryString, TechnologyAssets.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<TechnologyAssetViewDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), TechnologyAssetViewDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<TechnologyAssetViewDTO>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(TechnologyAssetFilter filter, Long organizationId, Query query) {
		query.setParameter("organizationId", organizationId);
		if (StringUtils.isNotEmpty(filter.getName())) query.setParameter("name", filter.getName());
		if (StringUtils.isNotEmpty(filter.getIpAddress())) query.setParameter("ipAddress", filter.getIpAddress());
		if (filter.getExcludeIds() != null) query.setParameter("excludeIds", filter.getExcludeIds());
		if (filter.getSystemStatus() != null) query.setParameter("systemStatus", filter.getSystemStatus());
		if (filter.getTechnologyCategory() != null) query.setParameter("technologyCategoryId", filter.getTechnologyCategory().getId());
		if (filter.getTechnology() != null) query.setParameter("technologyId", filter.getTechnology().getId());
		if (filter.getManufacturer() != null) query.setParameter("manufacturerId", filter.getManufacturer().getId());
		if (StringUtils.isNotEmpty(filter.getDiscoverySource())) query.setParameter("discoverySource", filter.getDiscoverySource());
		if (filter.getEndOfLife() != null) query.setParameter("endOfLife", filter.getEndOfLife());
		if (StringUtils.isNotEmpty(filter.getLocation())) query.setParameter("location", filter.getLocation());
	}
}
