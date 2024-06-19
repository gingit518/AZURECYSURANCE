package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssociateModels;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssociateModelRepository extends CoreRepository<AssociateModels, Long> {

	Optional<AssociateModels> findById(Long id);

	@Query("SELECT am FROM AssociateModels am LEFT JOIN FETCH am.owner ow " +
		"LEFT JOIN FETCH am.createdBy LEFT JOIN FETCH am.updatedBy " +
		"WHERE am.riskModelId = :riskModelId " +
		"AND (UPPER(am.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(am.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<AssociateModels> getListByRiskModelIdAndName(
		@Param("riskModelId") Long riskModelId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(am) FROM AssociateModels am " +
		"WHERE am.riskModelId = :riskModelId " +
		"AND (UPPER(am.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(am.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByRiskModelIdAndName(
		@Param("riskModelId") Long riskModelId,
		@Param("name") String name
	);

}
