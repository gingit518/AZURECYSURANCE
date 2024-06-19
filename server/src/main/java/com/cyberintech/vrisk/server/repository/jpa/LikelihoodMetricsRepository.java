package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.LikelihoodMetrics;
import com.cyberintech.vrisk.server.model.jpa.entity.QuantMetrics;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikelihoodMetricsRepository extends CoreRepository<LikelihoodMetrics, Long> {

	Optional<LikelihoodMetrics> findById(Long id);

	@Query("SELECT lm FROM LikelihoodMetrics lm LEFT JOIN FETCH lm.question LEFT JOIN FETCH lm.questionWeight " +
		"LEFT JOIN FETCH lm.riskType LEFT JOIN FETCH lm.vendor " +
		"LEFT JOIN FETCH lm.createdBy LEFT JOIN FETCH lm.updatedBy WHERE lm.riskModelId = :riskModelId")
	List<LikelihoodMetrics> getListByRiskModelId(@Param("riskModelId") Long riskModelId, Pageable pageable);

	@Query("SELECT count(lm) FROM LikelihoodMetrics lm WHERE lm.riskModelId = :riskModelId")
	Long getCountByRiskModelId(@Param("riskModelId") Long riskModelId);

}
