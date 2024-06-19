package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRSystemArticleStatus;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GDPRSystemArticleStatusRepository extends CoreRepository<GDPRSystemArticleStatus, Long> {

	Optional<GDPRSystemArticleStatus> findById(Long id);

	Optional<GDPRSystemArticleStatus> findByIdAndOrganizationId(Long id, Long organizationId);

	@Query("SELECT sas FROM GDPRSystemArticleStatus sas JOIN sas.system s JOIN sas.article a " +
		"WHERE sas.organizationId = :organizationId AND s.id = :systemId AND a.id = :articleId AND sas.paragraph IS NULL")
	Optional<GDPRSystemArticleStatus> getOneByOrganizationAndSystemAndArticle(
		@Param("systemId") Long systemId,
		@Param("articleId") Long articleId,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sas FROM GDPRSystemArticleStatus sas JOIN sas.system s JOIN sas.article a JOIN sas.paragraph p " +
		"WHERE sas.organizationId = :organizationId AND s.id = :systemId AND a.id = :articleId AND p.id = :paragraphId")
	Optional<GDPRSystemArticleStatus> getOneByOrganizationAndSystemAndArticleParagraph(
		@Param("systemId") Long systemId,
		@Param("articleId") Long articleId,
		@Param("paragraphId") Long paragraphId,
		@Param("organizationId") Long organizationId
	);

}
