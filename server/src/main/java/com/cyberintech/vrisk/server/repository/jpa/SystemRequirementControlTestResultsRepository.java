package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.SystemRequirementControlTestResults;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface SystemRequirementControlTestResultsRepository extends CoreRepository<SystemRequirementControlTestResults, Long> {

	Optional<SystemRequirementControlTestResults> findById(Long id);

	@Query("SELECT srctr FROM SystemRequirementControlTestResults srctr LEFT JOIN FETCH srctr.system s " +
		"LEFT JOIN FETCH srctr.securityRequirement sr WHERE s.id = :systemId AND sr.id = :requirementId")
	Optional<SystemRequirementControlTestResults> findBySystemIdAndRequirementId(
		@Param("systemId") Long systemId,
		@Param("requirementId") Long requirementId
	);

	@Query("SELECT srctr FROM SystemRequirementControlTestResults srctr LEFT JOIN FETCH srctr.system s " +
		"LEFT JOIN FETCH srctr.securityRequirement sr WHERE srctr.organizationId = :organizationId")
	Set<SystemRequirementControlTestResults> findAllByOrganizationId(@Param("organizationId") Long organizationId);

	@Query("SELECT srctr FROM SystemRequirementControlTestResults srctr LEFT JOIN FETCH srctr.system s " +
		"LEFT JOIN FETCH srctr.securityRequirement sr WHERE s.id = :systemId AND srctr.organizationId = :organizationId")
	Set<SystemRequirementControlTestResults> findAllBySystemIdAndOrganizationId(@Param("systemId") Long systemId, @Param("organizationId") Long organizationId);
}
