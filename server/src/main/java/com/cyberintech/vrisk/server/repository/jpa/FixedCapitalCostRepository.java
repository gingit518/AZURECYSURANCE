package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.CybersecurityTools;
import com.cyberintech.vrisk.server.model.jpa.entity.FixedCapitalCosts;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FixedCapitalCostRepository extends CoreRepository<FixedCapitalCosts, Long> {

	Optional<FixedCapitalCosts> findById(Long id);

	@Query("SELECT sum(cc.totalCosts) FROM FixedCapitalCosts cc WHERE cc.organizationId = :organizationId")
	Double getTotalCosts(@Param("organizationId") Long organizationId);

	List<FixedCapitalCosts> findAllByOrganizationId(Long organizationId);

	void deleteAllByCybersecurityTool(CybersecurityTools cybersecurityTool);

}
