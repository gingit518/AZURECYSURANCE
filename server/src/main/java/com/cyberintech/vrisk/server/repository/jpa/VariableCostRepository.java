package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.VariableCosts;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableCostRepository extends CoreRepository<VariableCosts, Long> {

	Optional<VariableCosts> findById(Long id);

	@Query("SELECT sum(vc.totalCosts) FROM VariableCosts vc WHERE vc.organizationId = :organizationId")
	Double getTotalCosts(@Param("organizationId") Long organizationId);

	List<VariableCosts> findAllByOrganizationId(Long organizationId);

}
