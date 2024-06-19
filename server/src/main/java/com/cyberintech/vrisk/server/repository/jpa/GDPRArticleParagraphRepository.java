package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleItem;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleParagraph;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GDPRArticleParagraphRepository extends CoreRepository<GDPRArticleParagraph, Long> {

	Optional<GDPRArticleParagraph> findById(Long id);

	Optional<GDPRArticleParagraph> findFirstByIdAndOrganizationId(Long id, Long organizationId);

	Optional<GDPRArticleParagraph> findFirstByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);

	Optional<GDPRArticleParagraph> findFirstByNameIgnoreCaseAndAndArticle(String name, GDPRArticleItem article);

}
