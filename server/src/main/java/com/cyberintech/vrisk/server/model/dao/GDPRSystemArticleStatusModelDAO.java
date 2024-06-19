package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRSystemArticleStatusDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualificationQuestionViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleToQuestion;
import com.cyberintech.vrisk.server.repository.jpa.GDPRArticleToQuestionRepository;
import com.cyberintech.vrisk.server.service.OrganizationService;
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
 * System Article Status DAO Model
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @since    2019-10-07
 */
@Service
public class GDPRSystemArticleStatusModelDAO implements PageableModelDAO<GDPRSystemArticleStatusDTO, GDPRFilter> {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private GDPRArticleToQuestionRepository gdprArticleToQuestionRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<GDPRSystemArticleStatusDTO> getItemsPageable(GDPRFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.gdpr.GDPRSystemArticleStatusDTO(sa) " +
			"FROM GDPRSystemArticleStatus sa JOIN sa.article a LEFT JOIN sa.paragraph p ";
//		String hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.gdpr.GDPRSystemArticleStatusDTO(sa, a, aq) " +
//			"FROM GDPRArticleToQuestion aq JOIN aq.question q JOIN aq.article a LEFT OUTER JOIN GDPRSystemArticleStatus sa ON sa.article=a AND sa.paragraph=aq.paragraph AND sa.system.id = :systemId ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(sa) FROM GDPRSystemArticleStatus sa JOIN sa.article a ";

		// Build Query String
		// String whereString = " WHERE a.organizationId = :organizationId AND (a.isMandatory = true OR sa.article IS NOT NULL) AND a.isSystemLevel = true  ";
//		String whereString = " WHERE a.organizationId = :organizationId AND q.vendorType = :questionType ";
		String whereString = " WHERE a.organizationId = :organizationId AND sa.system.id = :systemId  ";
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
			Map.entry("name", "s.name"),
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
			searchQueryString += " ORDER BY a.chapter.chapterNumber ASC, p.name ASC";
		}

		// Build Query data
		// TypedQuery<GDPRSystemArticleStatus> typedQuery = entityManager.createQuery(searchQueryString, GDPRSystemArticleStatus.class);
		TypedQuery<GDPRSystemArticleStatusDTO> typedQuery = entityManager.createQuery(searchQueryString, GDPRSystemArticleStatusDTO.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		if (pageable != null) {
			typedQuery.setMaxResults(pageable.getPageSize());
			typedQuery.setFirstResult((int) pageable.getOffset());
		}
		// List<GDPRSystemArticleStatusDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), GDPRSystemArticleStatusDTO.class);
		List<GDPRSystemArticleStatusDTO> resultList = typedQuery.getResultList();

		// TODO optimize this
		List<Long> articleIdsList = resultList.stream().filter(resultItem -> resultItem.getArticle() != null).map(resultItem -> resultItem.getArticle().getId()).collect(Collectors.toList());
		Set<GDPRArticleToQuestion> articleToQuestions = new HashSet<>();
		if (articleIdsList != null && articleIdsList.size() > 0) {
			articleToQuestions = gdprArticleToQuestionRepository.getAllByArticlesAndOrganization(articleIdsList, organizationId);
			Map<ArticleParagraphID, GDPRArticleToQuestion> articleToQuestionsMap = articleToQuestions.stream().collect(Collectors.toMap(resultItem -> new ArticleParagraphID(resultItem.getArticleId(), resultItem.getParagraphId()), resultItem -> resultItem));
			for (GDPRSystemArticleStatusDTO resultItem : resultList) {
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

		return new PagedResult<GDPRSystemArticleStatusDTO>(resultList, resultsCount);
	}

	public PagedResult<GDPRSystemArticleStatusDTO> getItemsPageable(GDPRFilter filter) {
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

		// query.setParameter("questionType", VendorType.GDPRSystem);
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (filter.getSystemId() != null) query.setParameter("systemId", filter.getSystemId());
	}

}
