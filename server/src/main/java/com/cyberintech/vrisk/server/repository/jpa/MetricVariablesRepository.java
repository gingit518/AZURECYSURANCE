package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.MetricVariables;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetricVariablesRepository extends CoreRepository<MetricVariables, Long> {

	Optional<MetricVariables> findById(Long id);

}
