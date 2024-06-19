package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.QualMetrics;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QualMetricsRepository extends CoreRepository<QualMetrics, Long> {

	Optional<QualMetrics> findById(Long id);

	@Query("SELECT qm FROM QualMetrics qm JOIN FETCH qm.createdBy " +
		"JOIN FETCH qm.updatedBy WHERE qm.riskModelId = ?1")
	List<QualMetrics> getListByRiskModelId(Long riskModelId);

	@Query("SELECT qm FROM QualMetrics qm JOIN qm.metricDomain md WHERE qm.riskModelId = :riskModelId AND md.name=:qualMetricDomain ")
	List<QualMetrics> getOneByRiskModelAndMetricDomain(@Param("riskModelId") Long riskModelId, @Param("qualMetricDomain") String qualMetricDomain);

	@Query("SELECT qm FROM QualMetrics qm JOIN qm.metricDomain md WHERE qm.riskModelId = :riskModelId AND md.code=:qualMetricDomain ")
	List<QualMetrics> getOneByRiskModelAndMetricDomainCode(@Param("riskModelId") Long riskModelId, @Param("qualMetricDomain") String qualMetricDomain);

}
