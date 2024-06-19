package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleStatusLogDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleStatusLog;
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
 * Organization Article Status DAO Model
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @since    2019-10-20
 */
@Service
public class GDPRArticleStatusLogModelDAO implements PageableModelDAO<GDPRArticleStatusLogDTO, GDPRFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<GDPRArticleStatusLogDTO> getItemsPageable(GDPRFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT sa FROM GDPRArticleStatusLog sa LEFT JOIN sa.article a ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(sa) FROM GDPRArticleStatusLog sa LEFT JOIN sa.article a ";

		// Build Query String
		String whereString = " WHERE sa.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(a.name) LIKE (CONCAT(UPPER(:name), '%')) ";
			whereString += " AND (UPPER(a.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(a.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND sa.id NOT IN :excludeIds";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "sa.id"),
			Map.entry("name", "a.name"),
			Map.entry("articleNumber", "a.name"),
			Map.entry("articleName", "a.name"),
			Map.entry("createdAt", "sa.createdAt"),
			Map.entry("chapterNumber", "a.chapter.chapterNumber"),
			Map.entry("chapter", "a.chapter.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<GDPRArticleStatusLog> typedQuery = entityManager.createQuery(searchQueryString, GDPRArticleStatusLog.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<GDPRArticleStatusLogDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), GDPRArticleStatusLogDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<GDPRArticleStatusLogDTO>(resultList, resultsCount);
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
	}

}
