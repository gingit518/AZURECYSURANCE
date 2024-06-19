package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.QuestionWeights;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionWeightRepository extends CoreRepository<QuestionWeights, Long> {

	Optional<QuestionWeights> findById(Long id);

}
