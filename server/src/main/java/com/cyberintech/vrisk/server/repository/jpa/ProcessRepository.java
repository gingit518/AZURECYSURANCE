package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Processes;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessRepository extends CoreRepository<Processes, Long> {

	Optional<Processes> findById(Long id);

	Optional<Processes> findFirstByNameAndOrganizationId(String name, Long organizationId);

	@Query("SELECT pr FROM Processes pr JOIN pr.systems s WHERE s.id = :systemId")
	List<Processes> getListBySystem(
		@Param("systemId") Long systemId
	);

	@Query("SELECT pr FROM Processes pr LEFT JOIN FETCH pr.systems sys WHERE pr.organizationId = :organizationId")
	List<Processes> getListByOrganization(@Param("organizationId") Long organizationId);

	@Query("SELECT pr FROM Processes pr LEFT JOIN FETCH pr.owner ow LEFT JOIN FETCH pr.businessUnit bu " +
		"LEFT JOIN FETCH pr.systems sys LEFT JOIN FETCH pr.createdBy LEFT JOIN FETCH pr.updatedBy " +
		"WHERE pr.organizationId = :organizationId AND UPPER(pr.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Processes> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(pr) FROM Processes pr " +
		"WHERE pr.organizationId = :organizationId AND UPPER(pr.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	void deleteAllBySystems(Systems system);

}
