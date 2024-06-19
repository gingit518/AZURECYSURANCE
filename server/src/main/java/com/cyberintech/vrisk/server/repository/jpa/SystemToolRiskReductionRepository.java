package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemToolRiskReductions;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import com.cyberintech.vrisk.server.repository.results.SystemToolRiskReductionResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemToolRiskReductionRepository extends CoreRepository<SystemToolRiskReductions, Long> {

	Optional<SystemToolRiskReductions> findById(Long id);

	@Query("SELECT new com.cyberintech.vrisk.server.repository.results.SystemToolRiskReductionResult(sys, strr) FROM Systems sys LEFT OUTER JOIN SystemToolRiskReductions strr ON strr.system=sys " +
		"WHERE sys.organizationId = :organizationId " +
		"AND (UPPER(sys.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(sys.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<SystemToolRiskReductionResult> getListByOrganization(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(sys) FROM Systems sys WHERE sys.organizationId = :organizationId " +
		"AND (UPPER(sys.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(sys.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganization(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	Optional<SystemToolRiskReductions> findByOrganizationIdAndSystem(Long organizationId, Systems system);

	List<SystemToolRiskReductions> findAllByOrganizationId(Long organizationId);
}
