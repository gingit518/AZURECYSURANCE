package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentFindings;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemControlTestResults;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemControlTestResultsRepository extends CoreRepository<SystemControlTestResults, Long> {

	Optional<SystemControlTestResults> findById(Long id);

	Optional<SystemControlTestResults> findBySystemId(Long systemId);

	@Query("SELECT distinct sctr FROM SystemControlTestResults sctr LEFT JOIN FETCH sctr.system s LEFT JOIN FETCH sctr.systemRequirementControlTestResults srctr " +
		"WHERE sctr.organizationId = :organizationId AND s.id IN :systemIds")
	List<SystemControlTestResults> getListByOrganizationAndSystemIds(
		@Param("organizationId") Long organizationId
		, @Param("systemIds") List<Long> systemIds
	);

	@Query("SELECT distinct sctr FROM SystemControlTestResults sctr LEFT JOIN FETCH sctr.system s LEFT JOIN FETCH sctr.systemRequirementControlTestResults srctr " +
		"WHERE sctr.organizationId = :organizationId")
	List<SystemControlTestResults> getListByOrganization(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sctr FROM SystemControlTestResults sctr LEFT JOIN FETCH sctr.system s " +
		"WHERE sctr.organizationId = :organizationId AND UPPER(s.name) LIKE (CONCAT(UPPER(:systemName), '%'))")
	List<SystemControlTestResults> getListByOrganizationAndSystemName(
		@Param("organizationId") Long organizationId,
		@Param("systemName") String systemName
	);

	@Query("SELECT count(sctr) FROM SystemControlTestResults sctr " +
		"WHERE sctr.organizationId = :organizationId AND UPPER(sctr.system.name) LIKE (CONCAT(UPPER(:systemName), '%'))")
	Long getCountByOrganizationAndSystemName(
		@Param("organizationId") Long organizationId,
		@Param("systemName") String systemName
	);
}
