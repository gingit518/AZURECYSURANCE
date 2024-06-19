package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.QuestionAnswersForVendor;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionAnswersForVendorRepository extends CoreRepository<QuestionAnswersForVendor, Long> {

	Optional<QuestionAnswersForVendor> findById(Long id);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForVendor qa JOIN qa.vendor v JOIN FETCH qa.question q LEFT JOIN FETCH qa.answer a WHERE v.rootParent.id = :organizationId")
	List<QuestionAnswersForVendor> getAllByOrganizationId(@Param("organizationId") Long organizationId);

	@Modifying
	@Query("delete from QuestionAnswersForVendor qa where qa.answer.id=:answerId")
	void deleteByAnswerId(@Param("answerId") Long answerId);

	void deleteAllByVendor(Organizations vendor);

	@Modifying
	@Query("delete from QuestionAnswersForVendor qa where qa.question.id=:questionId")
	void deleteByQuestionId(@Param("questionId") Long questionId);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForVendor qa JOIN qa.vendor v JOIN qa.question q LEFT JOIN FETCH qa.answer a " +
		"LEFT JOIN FETCH qa.createdBy LEFT JOIN FETCH qa.updatedBy " +
		"WHERE v.id = :vendorId AND q.id IN :questions")
	List<QuestionAnswersForVendor> getListByVendorAndQuestions(
		@Param("vendorId") Long vendorId,
		@Param("questions") List<Long> questions
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForVendor qa JOIN qa.vendor v LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q JOIN q.answers ans JOIN q.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE q.riskModelId=:riskModelId AND md.id=:metricDomainId AND q.vendorType in :vendorTypes")
	List<QuestionAnswersForVendor> getListByRiskModelAndMetricDomainId(
		@Param("riskModelId") Long riskModelId,
		@Param("metricDomainId") Long metricDomainId,
		@Param("vendorTypes") List<VendorType> vendorTypes
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForVendor qa JOIN qa.vendor v LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q JOIN q.answers ans JOIN q.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE q.riskModelId=:riskModelId AND q.vendorType in :vendorTypes")
	List<QuestionAnswersForVendor> getListByRiskModelAndVendorTypes(
		@Param("riskModelId") Long riskModelId,
		@Param("vendorTypes") List<VendorType> vendorTypes
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForVendor qa JOIN FETCH qa.vendor v JOIN FETCH qa.question q LEFT JOIN FETCH qa.answer a " +
		"WHERE q.riskModelId = :riskModelId AND q.id IN :questions")
	List<QuestionAnswersForVendor> getListByRiskModelAndQuestions(
		@Param("riskModelId") Long riskModelId,
		@Param("questions") List<Long> questions
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForVendor qa JOIN qa.vendor v LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q JOIN q.answers ans JOIN q.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE q.riskModelId=:riskModelId AND md.id=:metricDomainId AND v.id IN :vendorIdList AND q.vendorType in :vendorTypes")
	List<QuestionAnswersForVendor> getListByRiskModelAndMetricDomainIdAndVendors(
		@Param("riskModelId") Long riskModelId,
		@Param("metricDomainId") Long metricDomainId,
		@Param("vendorIdList") List<Long> vendorIdList,
		@Param("vendorTypes") List<VendorType> vendorTypes
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForVendor qa JOIN qa.vendor v LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q JOIN q.answers ans JOIN q.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE q.riskModelId=:riskModelId AND v.id IN :vendorIdList AND q.vendorType in :scoringTypes")
	List<QuestionAnswersForVendor> getListByRiskModelAndScoringTypesAndVendors(
		@Param("riskModelId") Long riskModelId,
		@Param("scoringTypes") List<VendorType> scoringTypes,
		@Param("vendorIdList") List<Long> vendorIdList
	);

	@Query("SELECT DISTINCT qa FROM QuestionAnswersForVendor qa JOIN qa.vendor v LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q JOIN q.answers ans JOIN q.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE q.riskModelId=:riskModelId AND qa.vendor.id=:vendorId AND md.id=:metricDomainId AND q.vendorType in :vendorTypes")
	List<QuestionAnswersForVendor> getListByVendorAndRiskModelAndMetricDomainId(
		@Param("riskModelId") Long riskModelId,
		@Param("vendorId") Long vendorId,
		@Param("metricDomainId") Long metricDomainId,
		@Param("vendorTypes") List<VendorType> vendorTypes
	);


	@Query("SELECT DISTINCT qa FROM QuestionAnswersForVendor qa JOIN qa.vendor v LEFT JOIN FETCH qa.answer a " +
		"JOIN qa.question q JOIN q.answers ans JOIN q.qualitativeMetric qm JOIN qm.metricDomain md " +
		"JOIN AssociateVendors av ON av.vendor=v JOIN av.systems s " +
		"WHERE q.riskModelId=:riskModelId AND s.id=:systemId AND md.id=:metricDomainId")
	List<QuestionAnswersForVendor> getListBySystemAndRiskModelAndMetricDomainId(
		@Param("riskModelId") Long riskModelId,
		@Param("systemId") Long systemId,
		@Param("metricDomainId") Long metricDomainId
	);

}
