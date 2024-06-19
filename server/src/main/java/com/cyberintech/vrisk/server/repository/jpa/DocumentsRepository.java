package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Documents;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentsRepository extends CoreRepository<Documents, Long> {

	Optional<Documents> findById(Long id);

	Optional<Documents> findByDocumentUid(String uid);

	Optional<Documents> findByIdAndOrganizationId(Long id, Long organizationId);

	@Query("SELECT d FROM Documents d WHERE d.organizationId = :organizationId AND UPPER(d.fileName) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Documents> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(d) FROM Documents d WHERE d.organizationId = :organizationId AND UPPER(d.fileName) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

}
