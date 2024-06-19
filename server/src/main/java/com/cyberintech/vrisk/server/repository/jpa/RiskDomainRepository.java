package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.RiskDomains;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskDomainRepository extends CoreRepository<RiskDomains, Long> {

	Optional<RiskDomains> findById(Long id);

}
