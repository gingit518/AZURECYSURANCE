package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleStatusDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualificationQuestionViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleToQuestion;
import com.cyberintech.vrisk.server.repository.jpa.GDPRArticleToQuestionRepository;
import com.cyberintech.vrisk.server.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Organization Article Status DAO Model
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @since    2019-10-20
 */
@Service
@Slf4j
public class GDPRArticleStatusModelDAO implements PageableModelDAO<GDPRArticleStatusDTO, GDPRFilter> {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private GDPRArticleToQuestionRepository gdprArticleToQuestionRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<GDPRArticleStatusDTO> getItemsPageable(GDPRFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT sa " +
			"FROM GDPRArticleStatus sa JOIN sa.article a LEFT JOIN sa.paragraph p " +
			" LEFT JOIN FETCH sa.tasks tc LEFT JOIN FETCH tc.taskManager tm LEFT JOIN FETCH tc.taskAssignee ta ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(a) FROM GDPRArticleStatus sa JOIN sa.article a ";

		// Build Query String
		// String whereString = " WHERE a.organizationId = :organizationId AND (a.isMandatory = true OR sa.article IS NOT NULL) ";
		String whereString = " WHERE a.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(a.name) LIKE (CONCAT(UPPER(:name), '%')) ";
			whereString += " AND (UPPER(a.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(a.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND sa.id NOT IN :excludeIds";
		}
		if (filter.getSystemId() != null) {
			// whereString += " AND s.id = :systemId";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "sa.id"),
			Map.entry("name", "a.name"),
			Map.entry("articleNumber", "a.articleNumber"),
			Map.entry("article", "a.articleNumber"),
			Map.entry("articleName", "a.name"),
			Map.entry("owner", "sa.owner.fullName"),
			Map.entry("compliance", "sa.compliance"),
			Map.entry("chapterNumber", "a.chapter.chapterNumber"),
			Map.entry("chapter", "a.chapter.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping) + ", p.name ASC";
		} else {
			searchQueryString += " ORDER BY a.articleNumber ASC, p.name ASC";
		}

		// Build Query data
		TypedQuery<GDPRArticleStatus> typedQuery = entityManager.createQuery(searchQueryString, GDPRArticleStatus.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		if (pageable != null) {
			typedQuery.setMaxResults(pageable.getPageSize());
			typedQuery.setFirstResult((int) pageable.getOffset());
		}
		List<GDPRArticleStatusDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), GDPRArticleStatusDTO.class);
		// List<GDPRArticleStatusDTO> resultList = typedQuery.getResultList();

		// TODO optimize this
		List<Long> articleIdsList = resultList.stream().filter(resultItem -> resultItem.getArticle() != null).map(resultItem -> resultItem.getArticle().getId()).collect(Collectors.toList());
		Set<GDPRArticleToQuestion> articleToQuestions = new HashSet<>();
		if (articleIdsList != null && articleIdsList.size() > 0) {
			articleToQuestions = gdprArticleToQuestionRepository.getAllByArticlesAndOrganization(articleIdsList, organizationId);
			Map<ArticleParagraphID, GDPRArticleToQuestion> articleToQuestionsMap = articleToQuestions.stream().collect(
				Collectors.toMap(
					resultItem -> new ArticleParagraphID(resultItem.getArticleId(), resultItem.getParagraphId()),
					resultItem -> resultItem,
					(gdprArticleToQuestion, gdprArticleToQuestion2) -> {
						log.warn(String.format("!!!! Duplicate key for mapping found: %s", gdprArticleToQuestion.toString()));

						return gdprArticleToQuestion;
					}
				)
			);
			for (GDPRArticleStatusDTO resultItem : resultList) {
				ArticleParagraphID mappingKey = new ArticleParagraphID(resultItem.getArticle().getId(), (resultItem.getParagraph() != null ? resultItem.getParagraph().getId() : null));
				if (articleToQuestionsMap.containsKey(mappingKey)) {
					GDPRArticleToQuestion gdprArticleToQuestion = articleToQuestionsMap.get(mappingKey);
					if (gdprArticleToQuestion.getQuestion() != null) resultItem.setQuestion(new QualificationQuestionViewDTO(gdprArticleToQuestion.getQuestion()));
				}
			}
		}

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<GDPRArticleStatusDTO>(resultList, resultsCount);
	}

	public PagedResult<GDPRArticleStatusDTO> getItemsPageable(GDPRFilter filter) {
		return getItemsPageable(filter, null, null);
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
