package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.SystemFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
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
 * System DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-02-24
 */
@Service
public class SystemModelDAO implements PageableModelDAO<SystemViewDTO, SystemFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<SystemViewDTO> getItemsPageable(SystemFilter filter, Pageable pageable, BaseSort sort) {
		// Detect filtered values
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		boolean isEtl = Optional.ofNullable(filter.getIsEtl()).orElse(false);
		Optional<SystemStatus> systemStatus = Optional.ofNullable(filter.getSystemStatus());
		DataAssetClassificationRefDTO assetClass = Optional.ofNullable(filter.getAssetClass()).orElse(null);
		DataTypeClassificationRefDTO dataType = Optional.ofNullable(filter.getDataType()).orElse(null);
		BusinessUnitRefDTO businessUnit = Optional.ofNullable(filter.getBusinessUnit()).orElse(null);
		UserRefDTO systemOwner = Optional.ofNullable(filter.getSystemOwner()).orElse(null);
		List<Long> excludeIds = null;
		if (filter != null && filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			excludeIds = filter.getExcludeIds();
		}
		Long organizationId = organizationService.getCurrentOrganizationId();

		// Define base hql data Query
		String hqlQuery = "SELECT sys FROM Systems sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.businessUnit bu LEFT JOIN FETCH sys.infosecFocalPerson ip " +
			"LEFT JOIN FETCH sys.createdBy LEFT JOIN FETCH sys.updatedBy ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(sys) FROM Systems sys ";

		// Build Query String
		String whereString = " WHERE sys.organizationId = :organizationId";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(sys.name) LIKE (CONCAT(UPPER(:name), '%'))";
			whereString += " AND (UPPER(sys.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(sys.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (excludeIds != null) {
			whereString += " AND sys.id NOT IN :excludeIds";
		}
		if (isEtl) {
			whereString += " AND sys.isEtl != true";
		}
		if (systemStatus.isPresent()) {
			whereString += " AND sys.systemStatus=:systemStatus";
		}
		if (assetClass != null && assetClass.getId() != null) {
			hqlQuery += " JOIN sys.dataAssetClassification dac";
			hqlQueryCount += " JOIN sys.dataAssetClassification dac";
			whereString += " AND dac.id=:assetClassId";
		} else {
			hqlQuery += " LEFT JOIN FETCH sys.dataAssetClassification dac";
		}
		if (dataType != null && dataType.getId() != null) {
			hqlQuery += " JOIN sys.dataTypeClassifications dtc";
			hqlQueryCount += " JOIN sys.dataTypeClassifications dtc";
			whereString += " AND dtc.id=:dataTypeId";
		} else {
			hqlQuery += " LEFT JOIN FETCH sys.dataTypeClassifications dtc";
		}
		if (businessUnit != null && businessUnit.getId() != null) {
			hqlQuery += " INNER JOIN BusinessUnits item " +
				" ON sys.businessUnit.id = item.id " +
				" INNER JOIN BusinessUnitLevels bul " +
				" ON item.id = bul.childId " +
				" JOIN BusinessUnits bm " +
				" ON bm.id = bul.parentId ";
			hqlQueryCount += " INNER JOIN BusinessUnits item " +
				" ON sys.businessUnit.id = item.id " +
				" INNER JOIN BusinessUnitLevels bul " +
				" ON item.id = bul.childId " +
				" JOIN BusinessUnits bm " +
				" ON bm.id = bul.parentId ";
			whereString += " AND bm.id=:businessUnitId";
		} else {
			hqlQuery += " LEFT JOIN FETCH sys.businessUnit bu";
		}
		if (systemOwner != null && systemOwner.getId() != null) {
			hqlQueryCount += " JOIN sys.owner ow ";
			whereString += " AND ow.id=:systemOwnerId";
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
		TypedQuery<Systems> typedQuery = entityManager.createQuery(searchQueryString, Systems.class);
		applySearchFilterValues(nameFilter, systemStatus, assetClass, dataType, businessUnit, systemOwner, excludeIds, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<SystemViewDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), SystemViewDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(nameFilter, systemStatus, assetClass, dataType, businessUnit, systemOwner, excludeIds, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<SystemViewDTO>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param nameFilter
	 * @param systemStatus
	 * @param assetClass
	 * @param businessUnit
	 * @param systemOwner
	 * @param excludeIds
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(String nameFilter, Optional<SystemStatus> systemStatus, DataAssetClassificationRefDTO assetClass, DataTypeClassificationRefDTO dataType, BusinessUnitRefDTO businessUnit, UserRefDTO systemOwner, List<Long> excludeIds, Long organizationId, Query query) {
		query.setParameter("organizationId", organizationId);
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (excludeIds != null) query.setParameter("excludeIds", excludeIds);
		if (systemStatus.isPresent()) query.setParameter("systemStatus", systemStatus.get());
		if (assetClass != null && assetClass.getId() != null) query.setParameter("assetClassId", assetClass.getId());
		if (dataType != null && dataType.getId() != null) query.setParameter("dataTypeId", dataType.getId());
		if (businessUnit != null && businessUnit.getId() != null) query.setParameter("businessUnitId", businessUnit.getId());
		if (systemOwner != null && systemOwner.getId() != null) query.setParameter("systemOwnerId", systemOwner.getId());
	}
}
