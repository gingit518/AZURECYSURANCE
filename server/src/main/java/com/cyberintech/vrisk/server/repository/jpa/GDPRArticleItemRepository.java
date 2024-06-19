package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleItem;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GDPRArticleItemRepository extends CoreRepository<GDPRArticleItem, Long> {

	Optional<GDPRArticleItem> findById(Long id);

	Optional<GDPRArticleItem> findFirstByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);

	Optional<GDPRArticleItem> findFirstByReferenceNumberIgnoreCaseAndOrganizationId(String name, Long organizationId);

	Optional<GDPRArticleItem> findFirstByArticleNumberAndOrganizationId(Long articleNumber, Long organizationId);

	Optional<GDPRArticleItem> findFirstByIdAndOrganizationId(Long id, Long organizationId);

	Set<GDPRArticleItem> findAllByOrganizationIdOrderByArticleNumberAsc(Long organizationId);

	@Query("SELECT it FROM GDPRArticleItem it JOIN it.chapter c WHERE it.organizationId=:organizationId AND c.id = :chapterId AND UPPER(it.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<GDPRArticleItem> getListByChapterAndName(@Param("chapterId") Long chapterId, @Param("name") String name, @Param("organizationId") Long organizationId, Pageable pageable);

	@Query("SELECT count(it) FROM GDPRArticleItem it JOIN it.chapter c WHERE it.organizationId=:organizationId AND c.id = :chapterId AND UPPER(it.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByChapterAndName(@Param("chapterId") Long chapterId, @Param("name") String name, @Param("organizationId") Long organizationId);

	@Query("SELECT it FROM GDPRArticleItem it JOIN it.section s WHERE it.organizationId=:organizationId AND s.id = :sectionId AND UPPER(it.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<GDPRArticleItem> getListBySectionAndName(@Param("sectionId") Long sectionId, @Param("name") String name, @Param("organizationId") Long organizationId, Pageable pageable);

	@Query("SELECT count(it) FROM GDPRArticleItem it JOIN it.section s WHERE it.organizationId=:organizationId AND s.id = :sectionId AND UPPER(it.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountBySectionAndName(@Param("sectionId") Long sectionId, @Param("name") String name, @Param("organizationId") Long organizationId);

}
