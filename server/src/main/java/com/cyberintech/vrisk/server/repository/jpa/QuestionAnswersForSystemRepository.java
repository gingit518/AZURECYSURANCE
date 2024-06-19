package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.QuestionAnswersForSystem;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionAnswersForSystemRepository extends CoreRepository<QuestionAnswersForSystem, Long> {

	Optional<QuestionAnswersForSystem> findById(Long id);

	void deleteAllBySystem(Systems system);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForSystem qa JOIN qa.system s JOIN FETCH qa.question q LEFT JOIN FETCH qa.answer a WHERE s.organizationId = :organizationId")
	List<QuestionAnswersForSystem> getAllByOrganizationId(@Param("organizationId") Long organizationId);

	@Modifying
	@Query("delete from QuestionAnswersForSystem qa where qa.answer.id=:answerId")
	void deleteByAnswerId(@Param("answerId") Long answerId);

	@Modifying
	@Query("delete from QuestionAnswersForSystem qa where qa.question.id=:questionId")
	void deleteByQuestionId(@Param("questionId") Long questionId);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForSystem qa JOIN qa.system v LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q WHERE q.riskModelId=:riskModelId AND q.vendorType IN :vendorTypes")
	List<QuestionAnswersForSystem> getListByRiskModelAndScoringTypes(@Param("riskModelId") Long riskModelId, @Param("vendorTypes") List<VendorType> vendorTypes);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForSystem qa JOIN qa.system s LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q WHERE q.riskModelId=:riskModelId AND q.vendorType IN :vendorTypes AND s.id IN :systemIds")
	List<QuestionAnswersForSystem> getListByRiskModelAndScoringTypesAndSystems(
		@Param("riskModelId") Long riskModelId,
		@Param("vendorTypes") List<VendorType> vendorTypes,
		@Param("systemIds") List<Long> systemIds
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForSystem qa JOIN qa.system v LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q JOIN q.answers ans JOIN q.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE q.riskModelId=:riskModelId AND md.id=:metricDomainId")
	List<QuestionAnswersForSystem> getListByRiskModelAndMetricDomainId(
		@Param("riskModelId") Long riskModelId,
		@Param("metricDomainId") Long metricDomainId
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForSystem qa JOIN qa.system v JOIN qa.question q LEFT JOIN FETCH qa.answer a " +
		"WHERE q.riskModelId = :riskModelId AND q.id IN :questions")
	List<QuestionAnswersForSystem> getListByRiskModelAndQuestions(
		@Param("riskModelId") Long riskModelId,
		@Param("questions") List<Long> questions
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForSystem qa JOIN qa.system v JOIN qa.question q LEFT JOIN FETCH qa.answer a " +
		"LEFT JOIN FETCH qa.createdBy LEFT JOIN FETCH qa.updatedBy " +
		"WHERE v.id = :systemId AND q.id IN :questions")
	List<QuestionAnswersForSystem> getListBySystemAndQuestions(
		@Param("systemId") Long systemId,
		@Param("questions") List<Long> questions
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForSystem qa JOIN qa.system s LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q JOIN q.answers ans JOIN q.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE q.riskModelId=:riskModelId AND md.id=:metricDomainId AND s.id IN :systemIdList")
	List<QuestionAnswersForSystem> getListByRiskModelAndMetricDomainIdAndSystems(
		@Param("riskModelId") Long riskModelId,
		@Param("metricDomainId") Long metricDomainId,
		@Param("systemIdList") List<Long> systemIdList
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForSystem qa JOIN qa.system s LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q JOIN q.answers ans JOIN q.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE q.riskModelId=:riskModelId AND s.id=:systemId AND md.id=:metricDomainId")
	List<QuestionAnswersForSystem> getListBySystemAndRiskModelAndMetricDomainId(
		@Param("riskModelId") Long riskModelId,
		@Param("systemId") Long systemId,
		@Param("metricDomainId") Long metricDomainId
	);

}
