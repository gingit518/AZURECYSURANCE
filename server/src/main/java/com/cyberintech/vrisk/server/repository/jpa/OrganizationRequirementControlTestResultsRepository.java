package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationRequirementControlTestResults;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRequirementControlTestResultsRepository extends CoreRepository<OrganizationRequirementControlTestResults, Long> {

	Optional<OrganizationRequirementControlTestResults> findById(Long id);

	Optional<OrganizationRequirementControlTestResults> findByIdAndOrganizationId(Long id, Long organizationId);

	@Query("SELECT orctr FROM OrganizationRequirementControlTestResults orctr LEFT JOIN FETCH orctr.securityRequirement sr " +
		"WHERE orctr.organizationId = :organizationId AND sr.id = :requirementId")
	Optional<OrganizationRequirementControlTestResults> findByOrganizationIdAndRequirementId(
		@Param("organizationId") Long organizationId,
		@Param("requirementId") Long requirementId
	);

}
