package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.RiskModelDomains;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskModelDomainRepository extends CoreRepository<RiskModelDomains, Long> {

	Optional<RiskModelDomains> findById(Long id);

	@Query("SELECT rmd FROM RiskModelDomains rmd LEFT JOIN FETCH rmd.riskDomain JOIN FETCH rmd.createdBy JOIN FETCH rmd.riskManagementOwner WHERE rmd.riskModelId = ?1 ORDER BY rmd.name")
	List<RiskModelDomains> getListByRiskModelId(Long riskModelId);

}
