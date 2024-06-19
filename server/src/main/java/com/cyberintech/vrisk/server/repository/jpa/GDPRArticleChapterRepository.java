package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleChapter;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GDPRArticleChapterRepository extends CoreRepository<GDPRArticleChapter, Long> {

	Optional<GDPRArticleChapter> findById(Long id);

	Optional<GDPRArticleChapter> findFirstByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);

	@Query("SELECT c FROM GDPRArticleChapter c WHERE c.organizationId=:organizationId AND UPPER(c.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<GDPRArticleChapter> getListByName(@Param("name") String name, @Param("organizationId") Long organizationId, Pageable pageable);

	@Query("SELECT count(c) FROM GDPRArticleChapter c WHERE c.organizationId=:organizationId AND UPPER(c.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name, @Param("organizationId") Long organizationId);

	@Query("SELECT c FROM GDPRArticleChapter c WHERE c.organizationId=:organizationId AND c.chapterNumber=:chapterNumber")
	List<GDPRArticleChapter> getListByChapterNumber(@Param("chapterNumber") Long chapterNumber, @Param("organizationId") Long organizationId, Pageable pageable);

	@Query("SELECT count(c) FROM GDPRArticleChapter c WHERE c.organizationId=:organizationId AND c.chapterNumber=:chapterNumber")
	Long getCountByChapterNumber(@Param("chapterNumber") Long chapterNumber, @Param("organizationId") Long organizationId);

}
