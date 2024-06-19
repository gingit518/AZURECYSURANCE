package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.QuestionBranchingLogic;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface QuestionBranchingLogicRepository extends CoreRepository<QuestionBranchingLogic, Long> {
	@Override
	Optional<QuestionBranchingLogic> findById(Long aLong);

	@Modifying
	@Query("delete from QuestionBranchingLogic a where a.question.id=:qualitativeQuestionId")
	void deleteByQuestionId(@Param("qualitativeQuestionId") Long qualitativeQuestionId);
}
