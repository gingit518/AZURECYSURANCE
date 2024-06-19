package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRSystemArticleStatusLogDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRSystemArticleStatusLog;
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
 * System Article Status DAO Model
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @since    2019-10-07
 */
@Service
public class GDPRSystemArticleStatusLogModelDAO implements PageableModelDAO<GDPRSystemArticleStatusLogDTO, GDPRFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<GDPRSystemArticleStatusLogDTO> getItemsPageable(GDPRFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		// String hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.gdpr.GDPRSystemArticleStatusDTO(ss, s) FROM Systems s LEFT OUTER JOIN GDPRSystemStatus ss ON ss.system=s ";
		String hqlQuery = "SELECT sa FROM GDPRSystemArticleStatusLog sa JOIN sa.system s LEFT JOIN sa.article a ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(sa) FROM GDPRSystemArticleStatusLog sa JOIN sa.system s LEFT JOIN sa.article a ";

		// Build Query String
		String whereString = " WHERE sa.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(a.name) LIKE (CONCAT(UPPER(:name), '%')) ";
			whereString += " AND (UPPER(a.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(a.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND sa.id NOT IN :excludeIds";
		}
		if (filter.getSystemId() != null) {
			whereString += " AND s.id = :systemId";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "sa.id"),
			Map.entry("name", "s.name"),
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
		TypedQuery<GDPRSystemArticleStatusLog> typedQuery = entityManager.createQuery(searchQueryString, GDPRSystemArticleStatusLog.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<GDPRSystemArticleStatusLogDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), GDPRSystemArticleStatusLogDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<GDPRSystemArticleStatusLogDTO>(resultList, resultsCount);
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
		if (filter.getSystemId() != null) query.setParameter("systemId", filter.getSystemId());
	}

}
