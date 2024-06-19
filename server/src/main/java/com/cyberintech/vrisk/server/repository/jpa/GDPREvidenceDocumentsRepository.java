package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleItem;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPREvidenceDocuments;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GDPREvidenceDocumentsRepository extends CoreRepository<GDPREvidenceDocuments, Long> {

	Optional<GDPREvidenceDocuments> findFirstByIdAndOrganizationId(Long id, Long organizationId);

	Optional<GDPREvidenceDocuments> findFirstByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);

	@Query("SELECT ed FROM GDPREvidenceDocuments ed LEFT JOIN FETCH ed.articles a WHERE ed.organizationId=:organizationId " +
		"AND (UPPER(ed.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(ed.documentType) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<GDPREvidenceDocuments> getListByName(@Param("name") String name, @Param("organizationId") Long organizationId, Pageable pageable);

	@Query("SELECT COUNT(ed) FROM GDPREvidenceDocuments ed WHERE ed.organizationId=:organizationId " +
		"AND (UPPER(ed.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(ed.documentType) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name, @Param("organizationId") Long organizationId);

}
