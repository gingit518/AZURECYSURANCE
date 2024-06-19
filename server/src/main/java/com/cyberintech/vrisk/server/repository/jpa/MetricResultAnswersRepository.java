package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.MetricResultAnswers;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import com.cyberintech.vrisk.server.repository.results.QualitativeQuestionAnswerResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetricResultAnswersRepository extends CoreRepository<MetricResultAnswers, Long> {

	Optional<MetricResultAnswers> findById(Long id);

	@Modifying
	@Query("delete from MetricResultAnswers qa where qa.answer.id=:answerId")
	void deleteByAnswerId(@Param("answerId") Long answerId);

	@Modifying
	@Query("delete from MetricResultAnswers ma where ma.question.id=:questionId")
	void deleteByQuestionId(@Param("questionId") Long questionId);

	@Query("SELECT mr FROM MetricResultAnswers mr WHERE mr.riskModelId = :riskModelId AND mr.id=:id")
	MetricResultAnswers getItemByIdAndRiskModelId(@Param("id") Long id, @Param("riskModelId") Long riskModelId);

	@Query("SELECT mr FROM MetricResultAnswers mr JOIN mr.question q WHERE q.riskModelId = :riskModelId AND q.id=:id")
	MetricResultAnswers getItemByQuestionIdAndRiskModelId(@Param("id") Long id, @Param("riskModelId") Long riskModelId);

	@Query("SELECT mr FROM MetricResultAnswers mr LEFT JOIN FETCH mr.question q LEFT JOIN FETCH mr.answer " +
		"LEFT JOIN FETCH mr.createdBy LEFT JOIN FETCH mr.updatedBy " +
		"WHERE mr.riskModelId = :riskModelId AND UPPER(q.question) LIKE (CONCAT(UPPER(:question), '%'))")
	List<MetricResultAnswers> getListByRiskModelIdAndQuestion(@Param("riskModelId") Long riskModelId, @Param("question") String question, Pageable pageable);

	@Query("SELECT count(mr) FROM MetricResultAnswers mr JOIN mr.question q " +
		"WHERE mr.riskModelId = :riskModelId AND UPPER(q.question) LIKE (CONCAT(UPPER(:question), '%'))")
	Long getCountByRiskModelIdAndQuestion(@Param("riskModelId") Long riskModelId, @Param("question") String question);

}
