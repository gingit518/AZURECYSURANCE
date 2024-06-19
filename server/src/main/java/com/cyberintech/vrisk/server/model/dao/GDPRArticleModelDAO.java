package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleItemDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleItem;
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
 * GDPR Article DAO Model
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @since    2019-10-17
 */
@Service
public class GDPRArticleModelDAO implements PageableModelDAO<GDPRArticleItemDTO, GDPRFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<GDPRArticleItemDTO> getItemsPageable(GDPRFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT it FROM GDPRArticleItem it LEFT JOIN it.section s LEFT JOIN it.chapter c ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(it) FROM GDPRArticleItem it LEFT JOIN it.section s LEFT JOIN it.chapter c ";

		// WHERE it.organizationId=:organizationId AND s.id = :sectionId AND UPPER(it.name) LIKE (CONCAT(UPPER(:name), '%'))
		// Build Query String
		String whereString = " WHERE it.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			//whereString += " AND UPPER(it.name) LIKE (CONCAT(UPPER(:name), '%')) ";
			whereString += " AND (UPPER(it.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(it.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND it.id NOT IN :excludeIds";
		}
		if (filter.getChapterId() != null) {
			whereString += " AND c.id = :chapterId";
		}
		if (filter.getSectionId() != null) {
			whereString += " AND s.id = :sectionId";
		}
		if (Boolean.TRUE.equals(filter.getIsSystemLevel())) {
			whereString += " AND it.isSystemLevel = true";
		}
		if (Boolean.TRUE.equals(filter.getIsOrganizationLevel())) {
			whereString += " AND it.isOrganizationLevel = true";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "a.id"),
			Map.entry("name", "a.name"),
			Map.entry("articleName", "a.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<GDPRArticleItem> typedQuery = entityManager.createQuery(searchQueryString, GDPRArticleItem.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<GDPRArticleItemDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), GDPRArticleItemDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<GDPRArticleItemDTO>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(GDPRFilter filter, Long organizationId, Query query) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (filter.getChapterId() != null) query.setParameter("chapterId", filter.getChapterId());
		if (filter.getSectionId() != null) query.setParameter("sectionId", filter.getSectionId());
	}

}
