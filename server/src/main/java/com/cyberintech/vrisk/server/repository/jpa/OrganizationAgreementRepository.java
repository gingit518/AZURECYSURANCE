package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationsAgreements;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationAgreementRepository extends CoreRepository<OrganizationsAgreements, Long> {

	Optional<OrganizationsAgreements> findById(Long id);

	@Query("SELECT DISTINCT oa FROM OrganizationsAgreements oa LEFT JOIN FETCH oa.organizations org " +
		"LEFT JOIN FETCH oa.createdBy LEFT JOIN FETCH oa.updatedBy " +
		"WHERE org.id = :organizationId")
	List<OrganizationsAgreements> getListByOrganizationId(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT DISTINCT oa FROM OrganizationsAgreements oa LEFT JOIN FETCH oa.organizations org " +
		"LEFT JOIN FETCH oa.createdBy LEFT JOIN FETCH oa.updatedBy " +
		"WHERE org.id = :organizationId AND UPPER(oa.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<OrganizationsAgreements> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(oa) FROM OrganizationsAgreements oa LEFT JOIN oa.organizations org " +
		"WHERE org.id = :organizationId AND UPPER(oa.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT oa FROM OrganizationsAgreements oa LEFT JOIN FETCH oa.organizations org " +
		"LEFT JOIN FETCH oa.createdBy LEFT JOIN FETCH oa.updatedBy " +
		"WHERE UPPER(oa.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<OrganizationsAgreements> getListByName(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(oa) FROM OrganizationsAgreements oa " +
		"WHERE UPPER(oa.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByNameIsLike(
		@Param("name") String name
	);

	@Query("SELECT oa FROM OrganizationsAgreements oa " +
		"LEFT JOIN UsersAgreements ua ON ua.organizationAgreement.id = oa.id and ua.user.id = :userId " +
		"LEFT JOIN FETCH oa.organizations org " +
		"WHERE org.id = :organizationId and (ua.id is null or (ua.answer=false or ua.answer is null))")
	List<OrganizationsAgreements> getListOfNotAnsweredByOrganizationIdAndUserId(
		@Param("organizationId") Long organizationId,
		@Param("userId") Long userId
	);
}
