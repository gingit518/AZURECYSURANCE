package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AnswerWeight;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerWeightRepository extends CoreRepository<AnswerWeight, Long> {

	Optional<AnswerWeight> findById(Long id);

	@Query("SELECT aw FROM AnswerWeight aw ORDER BY aw.value ASC")
	List<AnswerWeight> getList();

}
