package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import com.cyberintech.vrisk.server.model.jpa.entity.QuantMetrics;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuantMetricsRepository extends CoreRepository<QuantMetrics, Long> {

	Optional<QuantMetrics> findById(Long id);

	@Query("SELECT distinct qm FROM QuantMetrics qm JOIN qm.quant qua JOIN FETCH qm.metricFormulaItems mfi " +
		"WHERE qua.id IN :quantIds AND qm.riskModelId = :riskModelId")
	List<QuantMetrics> getListByRiskModelIdAndQuantIds(@Param("riskModelId") Long riskModelId, @Param("quantIds") List<Long> quantIds);

	@Query("SELECT distinct qm FROM QuantMetrics qm JOIN qm.quant qua JOIN FETCH qm.metricFormulaItems mfi " +
		"WHERE qua.id = :quantId AND qm.riskModelId = :riskModelId")
	List<QuantMetrics> getListByRiskModelIdAndQuantId(@Param("riskModelId") Long riskModelId, @Param("quantId") Long quantId);

	@Query("SELECT count(qm) FROM QuantMetrics qm JOIN qm.quant qua WHERE qua.id = :quantId AND qm.riskModelId = :riskModelId")
	Long getCountForRiskModelIdAndQuantId(@Param("riskModelId") Long riskModelId, @Param("quantId") Long quantId);

	@Query("SELECT qm FROM QuantMetrics qm LEFT JOIN FETCH qm.createdBy " +
		"LEFT JOIN FETCH qm.updatedBy WHERE qm.riskModelId = ?1")
	List<QuantMetrics> getListByRiskModelId(Long riskModelId);

	@Query("SELECT qm FROM QuantMetrics qm LEFT JOIN FETCH qm.createdBy " +
		"LEFT JOIN FETCH qm.updatedBy WHERE qm.riskModelId = ?1 AND qm.quantMetricLevel = ?2")
	List<QuantMetrics> getListByRiskModelIdAndQuantMetricLevel(Long riskModelId, QuantMetricLevel quantMetricLevel);


	@Query("SELECT distinct qm FROM QuantMetrics qm LEFT JOIN FETCH qm.quant q LEFT JOIN FETCH qm.metricFormulaItems mfi " +
		"LEFT JOIN FETCH qm.createdBy LEFT JOIN FETCH qm.updatedBy " +
		"WHERE qm.riskModelId = :riskModelId AND qm.id NOT IN :excludeIds " +
		"AND (UPPER(qm.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(qm.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<QuantMetrics> getListByRiskModelAndName(
		@Param("riskModelId") Long riskModelId,
		@Param("name") String name,
		@Param("excludeIds") List<Long> excludeIds,
		Pageable pageable
	);

	@Query("SELECT count(qm) FROM QuantMetrics qm " +
		"WHERE qm.riskModelId = :riskModelId AND qm.id NOT IN :excludeIds " +
		"AND (UPPER(qm.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(qm.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByRiskModelAndName(
		@Param("riskModelId") Long riskModelId,
		@Param("name") String name,
		@Param("excludeIds") List<Long> excludeIds
	);

	Optional<QuantMetrics> findFirstByRiskModelIdAndName( Long riskModelId, String name);
}
