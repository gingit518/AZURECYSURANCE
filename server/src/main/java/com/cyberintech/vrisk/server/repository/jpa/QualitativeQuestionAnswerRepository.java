package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestionAnswers;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QualitativeQuestionAnswerRepository extends CoreRepository<QualitativeQuestionAnswers, Long> {

	void deleteAllByQualitativeQuestion(QualitativeQuestions qualitativeQuestion);

	@Modifying
	@Query("delete from QualitativeQuestionAnswers a where a.qualitativeQuestion.id=:qualitativeQuestionId")
	void deleteByQuestionId(@Param("qualitativeQuestionId") Long qualitativeQuestionId);

	@Modifying
	@Query("delete from QualitativeQuestionAnswers a where a.id=:id")
	void deleteByItemId(@Param("id") Long id);

	@Query("select a from QualitativeQuestionAnswers a JOIN a.qualitativeQuestion q WHERE q.id=:questionId AND a.answer=:answer")
	List<QualitativeQuestionAnswers> getByQuestionAndAnswer(@Param("questionId") Long questionId, @Param("answer") String answer);

	@Query("select a from QualitativeQuestionAnswers a JOIN a.qualitativeQuestion q JOIN a.answerWeight w WHERE q.id=:questionId AND w.value=:value")
	Optional<QualitativeQuestionAnswers> getByQuestionAndAnswerWeight(@Param("questionId") Long questionId, @Param("value") Long value);

}
