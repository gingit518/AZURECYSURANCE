package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Policies;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends CoreRepository<Policies, Long> {

	Optional<Policies> findById(Long id);

	Optional<Policies> findByNameAndOrganizationId(String name, Long organizationId);

	@Query("SELECT p FROM Policies p LEFT JOIN FETCH p.approvedBy ab LEFT JOIN FETCH p.createdBy cb " +
		"WHERE p.organizationId = :organizationId AND UPPER(p.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	List<Policies> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(p) FROM Policies p " +
		"WHERE p.organizationId = :organizationId AND UPPER(p.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);
}
