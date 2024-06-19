package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.MetricRisks;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetricRisksRepository extends CoreRepository<MetricRisks, Long> {

	Optional<MetricRisks> findById(Long id);

	@Query("SELECT mr FROM MetricRisks mr LEFT JOIN FETCH mr.question LEFT JOIN FETCH mr.questionWeight " +
		"LEFT JOIN FETCH mr.riskType LEFT JOIN FETCH mr.vendor LEFT JOIN FETCH mr.metricDomain " +
		"LEFT JOIN FETCH mr.createdBy LEFT JOIN FETCH mr.updatedBy WHERE mr.riskModelId = :riskModelId AND mr.metricDomainId IN :metricDomainIds")
	List<MetricRisks> getListByRiskModelId(@Param("riskModelId") Long riskModelId, @Param("metricDomainIds") Collection<Long> metricDomainIds, Pageable pageable);

	@Query("SELECT count(mr) FROM MetricRisks mr WHERE mr.riskModelId = :riskModelId AND mr.metricDomainId IN :metricDomainIds")
	Long getCountByRiskModelId(@Param("riskModelId") Long riskModelId, @Param("metricDomainIds") Collection<Long> metricDomainIds);

}
