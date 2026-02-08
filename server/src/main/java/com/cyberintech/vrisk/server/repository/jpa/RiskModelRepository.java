package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskModelRepository extends CoreRepository<RiskModels, Long> {

	Optional<RiskModels> findById(Long id);

	Optional<RiskModels> findByIdAndOrganizationId(Long id, Long organizationId);

	List<RiskModels> findAllByOrganizationId(Long organizationId);

	List<RiskModels> findAllByOrganizationIdOrderByIdAsc(Long organizationId);

	@Query("SELECT max(rm.ordinal) FROM RiskModels rm WHERE rm.organizationId = ?1")
	Optional<Long> getMaxOrdinal(Long organizationId);

	@Transactional
	@Procedure(procedureName = "delete_risk_model")
	Long deleteRiskModel(@Param("riskModelId") Long riskModelId);
}
