package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleChapterSection;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GDPRArticleChapterSectionRepository extends CoreRepository<GDPRArticleChapterSection, Long> {

	Optional<GDPRArticleChapterSection> findById(Long id);

	Optional<GDPRArticleChapterSection> findFirstByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);

	@Query("SELECT s FROM GDPRArticleChapterSection s JOIN s.chapter c WHERE s.organizationId=:organizationId AND c.id = :chapterId AND UPPER(s.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<GDPRArticleChapterSection> getListByChapterAndName(@Param("chapterId") Long chapterId, @Param("name") String name, @Param("organizationId") Long organizationId, Pageable pageable);

	@Query("SELECT count(s) FROM GDPRArticleChapterSection s JOIN s.chapter c WHERE s.organizationId=:organizationId AND c.id = :chapterId AND UPPER(s.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByChapterAndName(@Param("chapterId") Long chapterId, @Param("name") String name, @Param("organizationId") Long organizationId);

}
