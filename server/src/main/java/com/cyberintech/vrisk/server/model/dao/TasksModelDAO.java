package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.TasksFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskBudgetViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Tasks;
import com.cyberintech.vrisk.server.service.OrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tasks DAO Model
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.0
 * @since    2021-05-13
 */
@Service
public class TasksModelDAO implements PageableModelDAO<TaskBudgetViewDTO, TasksFilter> {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private EntityManager entityManager;

	@Override
	public PagedResult<TaskBudgetViewDTO> getItemsPageable(TasksFilter filter, Pageable pageable, BaseSort sort) {

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		Long organizationId = organizationService.getCurrentOrganizationId();

		// Define base hql data Query
		String hqlQuery = "SELECT t FROM Tasks t LEFT JOIN FETCH t.taskAssignee tas LEFT JOIN FETCH t.taskManager tam ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(t) FROM Tasks t LEFT JOIN t.taskAssignee tas LEFT JOIN t.taskManager tam ";

		// Build Query String
		String whereString = " WHERE t.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			whereString += " AND UPPER(t.name) LIKE (CONCAT(UPPER(:name), '%')) ";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND tec.id NOT IN :excludeIds";
		}
		if (filter.getTaskManagerOrAssigneeId() != null) {
			whereString += " AND (tam.id = :taskManagerId OR tas.id = :taskAssigneeId) ";
		} else if (filter.getTaskManagerId() != null) {
			whereString += " AND tam.id = :taskManagerId ";
		} else if (filter.getTaskAssigneeId() != null) {
			whereString += " AND tas.id = :taskAssigneeId ";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "t.id"),
			Map.entry("name", "t.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		} else {
			 searchQueryString += "ORDER BY t.id ASC";
		}

		// Build Query data
		TypedQuery<Tasks> typedQuery = entityManager.createQuery(searchQueryString, Tasks.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<TaskBudgetViewDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), TaskBudgetViewDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<TaskBudgetViewDTO>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(TasksFilter filter, Long organizationId, Query query) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		if(StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (filter.getTaskManagerOrAssigneeId() != null) {
			query.setParameter("taskManagerId", filter.getTaskManagerOrAssigneeId());
			query.setParameter("taskAssigneeId", filter.getTaskManagerOrAssigneeId());
		} else if (filter.getTaskManagerId() != null) {
			query.setParameter("taskManagerId", filter.getTaskManagerId());
		} else if (filter.getTaskAssigneeId() != null) {
			query.setParameter("taskAssigneeId", filter.getTaskAssigneeId());
		}
		if(filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
	}
}
