package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleToQuestion;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GDPRArticleToQuestionRepository extends CoreRepository<GDPRArticleToQuestion, Long> {

	Optional<GDPRArticleToQuestion> findById(Long id);

	Optional<GDPRArticleToQuestion> findByIdAndOrganizationId(Long id, Long organizationId);

	GDPRArticleToQuestion findByQuestionIdAndArticleIdAndParagraphId(Long questionId, Long articleId, Long paragraphId);

	@Query("SELECT aq FROM GDPRArticleToQuestion aq JOIN aq.article a WHERE aq.organizationId = :organizationId")
	Set<GDPRArticleToQuestion> getAllByOrganization(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT aq FROM GDPRArticleToQuestion aq JOIN aq.question q WHERE aq.organizationId = :organizationId AND q.id IN :questions")
	Set<GDPRArticleToQuestion> getAllByOrganizationAndQuestions(
		@Param("organizationId") Long organizationId,
		@Param("questions") List<Long> questions
	);

	@Query("SELECT aq FROM GDPRArticleToQuestion aq JOIN aq.article a WHERE aq.organizationId = :organizationId AND a.id IN :articles")
	Set<GDPRArticleToQuestion> getAllByArticlesAndOrganization(
		@Param("articles") List<Long> articles,
		@Param("organizationId") Long organizationId
	);

}
