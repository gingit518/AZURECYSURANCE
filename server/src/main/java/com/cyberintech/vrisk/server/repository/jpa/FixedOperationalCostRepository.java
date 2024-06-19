package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.FixedOperationalCosts;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FixedOperationalCostRepository extends CoreRepository<FixedOperationalCosts, Long> {

	Optional<FixedOperationalCosts> findById(Long id);

	@Query("SELECT sum(oc.totalCosts) FROM FixedOperationalCosts oc WHERE oc.organizationId = :organizationId")
	Double getTotalCosts(@Param("organizationId") Long organizationId);

	List<FixedOperationalCosts> findAllByOrganizationId(Long organizationId);
}
