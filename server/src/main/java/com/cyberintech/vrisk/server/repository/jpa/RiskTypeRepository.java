package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.RiskTypes;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskTypeRepository extends CoreRepository<RiskTypes, Long> {

	Optional<RiskTypes> findById(Long id);

	@Query("SELECT r FROM RiskTypes r WHERE r.categoryDomain.id = ?1")
	List<RiskTypes> getListByCategoryDomainId(Long categoryDomainId);

	@Query("SELECT r FROM RiskTypes r JOIN FETCH r.categoryDomain cd WHERE cd.riskModelId = :riskModelId AND UPPER(r.name) LIKE (CONCAT('%', UPPER(:riskName), '%')) AND r.id NOT IN :excludeIds")
	List<RiskTypes> getListByRiskModelPaged(@Param("riskModelId") Long riskModelId, @Param("riskName") String riskName, @Param("excludeIds") Collection<Long> excludeIds, Pageable pageable);

	@Query("SELECT count(r) FROM RiskTypes r JOIN r.categoryDomain cd WHERE cd.riskModelId = :riskModelId AND UPPER(r.name) LIKE (CONCAT('%', UPPER(:riskName), '%')) AND r.id NOT IN :excludeIds")
	Long getListByRiskModelCount(@Param("riskModelId") Long riskModelId, @Param("riskName") String riskName, @Param("excludeIds") Collection<Long> excludeIds);

}
