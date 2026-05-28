package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.RiskModelConstants;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskModelConstantRepository extends CoreRepository<RiskModelConstants, Long> {

	Optional<RiskModelConstants> findById(Long id);

	@Query("SELECT rmc FROM RiskModelConstants rmc WHERE rmc.riskModelId = :riskModelId " +
		"AND (UPPER(rmc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(rmc.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<RiskModelConstants> getListByRiskModelAndName(
		@Param("riskModelId") Long riskModelId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT COUNT(rmc) FROM RiskModelConstants rmc WHERE rmc.riskModelId = :riskModelId " +
		"AND (UPPER(rmc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(rmc.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByRiskModelAndName(
		@Param("riskModelId") Long riskModelId,
		@Param("name") String name
	);

	@Query("SELECT rmc FROM RiskModelConstants rmc WHERE UPPER(rmc.name) LIKE UPPER(:name) AND rmc.riskModelId = :riskModelId")
	Optional<RiskModelConstants> findByNameAndRiskModelId(@Param("name") String name, @Param("riskModelId") Long riskModelId);

}
