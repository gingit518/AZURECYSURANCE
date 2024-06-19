package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.RiskMetrics;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RiskMetricsRepository extends CoreRepository<RiskMetrics, Long> {

	Optional<RiskMetrics> findById(Long id);

	@Modifying
	@Query("update RiskMetrics rm set rm.isResidual = false where rm.riskModelId = :riskModelId and rm.id <> :id")
	void resetResidualForRiskModel(@Param("riskModelId") Long riskModelId, @Param("id") Long id);

	@Query("SELECT rm FROM RiskMetrics rm LEFT JOIN FETCH rm.createdBy LEFT JOIN FETCH rm.updatedBy WHERE rm.riskModelId = :riskModelId order by rm.name")
	List<RiskMetrics> getListByRiskModelId(@Param("riskModelId") Long riskModelId);

	@Query("SELECT rm FROM RiskMetrics rm LEFT JOIN FETCH rm.createdBy LEFT JOIN FETCH rm.updatedBy WHERE rm.riskModelId = :riskModelId AND rm.isResidual = :isResidual")
	List<RiskMetrics> getListByRiskModelIdAAndIsResidual(@Param("riskModelId") Long riskModelId, @Param("isResidual") Boolean isResidual);

	@Query("SELECT distinct rm FROM RiskMetrics rm LEFT JOIN FETCH rm.formula f LEFT JOIN FETCH rm.createdBy LEFT JOIN FETCH rm.updatedBy " +
		"WHERE rm.riskModelId = :riskModelId " +
		"AND (UPPER(rm.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(rm.description) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(f.name) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<RiskMetrics>  getListByRiskModelAndName(
		@Param("riskModelId") Long riskModelId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(rm) FROM RiskMetrics rm LEFT JOIN rm.formula f " +
		"WHERE rm.riskModelId = :riskModelId " +
		"AND (UPPER(rm.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(rm.description) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(f.name) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByRiskModelAndName(
		@Param("riskModelId") Long riskModelId,
		@Param("name") String name
	);

}
