package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleStatus;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface GDPRArticleStatusRepository extends CoreRepository<GDPRArticleStatus, Long> {

	Optional<GDPRArticleStatus> findById(Long id);

	Optional<GDPRArticleStatus> findByIdAndOrganizationId(Long id, Long organizationId);

	@Query("SELECT sas FROM GDPRArticleStatus sas JOIN sas.article a " +
		"WHERE sas.organizationId = :organizationId AND a.id = :articleId AND sas.paragraph IS NULL")
	Optional<GDPRArticleStatus> getOneByOrganizationAndArticle(
		@Param("articleId") Long articleId,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sas FROM GDPRArticleStatus sas JOIN sas.article a JOIN sas.paragraph p " +
		"WHERE sas.organizationId = :organizationId AND a.id = :articleId AND p.id = :paragraphId")
	Optional<GDPRArticleStatus> getOneByOrganizationAndArticleParagraph(
		@Param("articleId") Long articleId,
		@Param("paragraphId") Long paragraphId,
		@Param("organizationId") Long organizationId
	);

	@Modifying
	@Query(value = "DELETE FROM gdpr_article_status gas WHERE"
		+ " gas.organization_id = :organizationId"
		+ " AND gas.paragraph_id is null"
		+ " AND exists (SELECT 1 FROM gdpr_article_status"
		+ " WHERE organization_id = gas.organization_id AND article_id = gas.article_id AND paragraph_id is not null)",
		nativeQuery = true)
	@Transactional
	void deletePhantomStatuses(@Param("organizationId") Long organizationId);

}
