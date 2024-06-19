package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.SecurityRequirements;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SecurityRequirementRepository extends CoreRepository<SecurityRequirements, Long> {

	Optional<SecurityRequirements> findById(Long id);

	List<SecurityRequirements> findAllByOrganizationId(Long organizationId, Pageable pageable);

	Set<SecurityRequirements> findAllByOrganizationId(Long organizationId);

	Set<SecurityRequirements> findAllByOrganizationIdOrderByCodeAsc(Long organizationId);

	Long countAllByOrganizationId(Long organizationId);

	@Query("SELECT sr FROM SecurityRequirements sr LEFT JOIN FETCH sr.securityControlFamily scf " +
		"LEFT JOIN FETCH sr.securityControlName scn LEFT JOIN FETCH sr.assessmentLevel al " +
		"WHERE sr.organizationId = :organizationId")
	List<SecurityRequirements> getListByOrganizationId(Long organizationId);

	@Query("SELECT sr FROM SecurityRequirements sr LEFT JOIN FETCH sr.securityControlFamily scf " +
		"LEFT JOIN FETCH sr.securityControlName scn LEFT JOIN FETCH sr.assessmentLevel al " +
		"WHERE sr.organizationId = :organizationId AND UPPER(sr.code) LIKE (CONCAT(UPPER(:code), '%'))")
	List<SecurityRequirements> getListByOrganizationAndCode(
		@Param("organizationId") Long organizationId,
		@Param("code") String code,
		Pageable pageable
	);

	@Query("SELECT count(sr) FROM SecurityRequirements sr " +
		"WHERE sr.organizationId = :organizationId AND UPPER(sr.code) LIKE (CONCAT(UPPER(:code), '%'))")
	Long getCountByOrganizationAndCode(
		@Param("organizationId") Long organizationId,
		@Param("code") String code
	);

	Optional<SecurityRequirements> findFirstByCodeIgnoreCaseAndOrganizationId(String code, Long organizationId);
}
