package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.QualMetrics;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface QualitativeQuestionRepository extends CoreRepository<QualitativeQuestions, Long> {

	Optional<QualitativeQuestions> findById(Long id);

	Set<QualitativeQuestions> findAllByQualitativeMetric(QualMetrics qualitativeMetric);

	@Query("SELECT MAX(qq.ordinal) FROM QualitativeQuestions qq WHERE qq.riskModelId = :riskModelId")
	Long getMaxOrdinalByRiskModelId(@Param("riskModelId") Long riskModelId);

	@Query("SELECT qq FROM QualitativeQuestions qq JOIN FETCH qq.createdBy " +
		"JOIN FETCH qq.updatedBy WHERE qq.riskModelId = :riskModelId ORDER BY qq.ordinal")
	List<QualitativeQuestions> getListByRiskModelId(@Param("riskModelId") Long riskModelId);

	@Query("SELECT qq FROM QualitativeQuestions qq JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"LEFT JOIN FETCH qq.createdBy LEFT JOIN FETCH qq.updatedBy " +
		"WHERE qq.riskModelId = :riskModelId AND UPPER(qq.question) LIKE (CONCAT(UPPER(:question), '%')) AND md.id=:metricDomainId")
	List<QualitativeQuestions> getListByRiskModelIdAndQuestionAndMetricDomain(
		@Param("riskModelId") Long riskModelId,
		@Param("question") String question,
		@Param("metricDomainId") Long metricDomainId,
		Pageable pageable
	);

	@Query("SELECT count(qq) FROM QualitativeQuestions qq JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE qq.riskModelId = :riskModelId AND UPPER(qq.question) LIKE (CONCAT(UPPER(:question), '%')) AND md.id=:metricDomainId")
	Long getCountByRiskModelIdAndQuestionAndMetricDomain(
		@Param("riskModelId") Long riskModelId,
		@Param("question") String question,
		@Param("metricDomainId") Long metricDomainId
	);

	@Query("SELECT qq FROM QualitativeQuestions qq JOIN qq.vendors v JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE qq.riskModelId = :riskModelId AND md.id=:metricDomainId")
	List<QualitativeQuestions> getListByRiskModelIdAndMetricDomain(
		@Param("riskModelId") Long riskModelId,
		@Param("metricDomainId") Long metricDomainId
	);

	@Query("SELECT distinct qq FROM QualitativeQuestions qq LEFT JOIN qq.vendors v JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE qq.riskModelId = :riskModelId AND qq.vendorType IN :vendorTypes ")
	List<QualitativeQuestions> getListOfInternalByRiskModelIdAndTypes(
		@Param("riskModelId") Long riskModelId,
		@Param("vendorTypes") List<VendorType> vendorTypes
	);

	@Query("SELECT distinct qq FROM QualitativeQuestions qq LEFT JOIN qq.vendors v JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE qq.riskModelId = :riskModelId AND qq.vendorType IN :vendorTypes AND qq.vendorType NOT IN :internalVendorTypes")
	List<QualitativeQuestions> getListOfNotInternalByRiskModelIdAndTypes(
		@Param("riskModelId") Long riskModelId,
		@Param("vendorTypes") List<VendorType> vendorTypes,
		@Param("internalVendorTypes") List<VendorType> internalVendorTypes
	);

	@Query("SELECT distinct qq FROM QualitativeQuestions qq JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE qq.riskModelId = :riskModelId AND md.id=:metricDomainId AND qq.vendorType IN :vendorTypes")
	List<QualitativeQuestions> getListByRiskModelIdAndMetricDomainAndType(
		@Param("riskModelId") Long riskModelId,
		@Param("metricDomainId") Long metricDomainId,
		@Param("vendorTypes") List<VendorType> vendorTypes
	);

	@Query("SELECT distinct qq FROM QualitativeQuestions qq JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE qq.riskModelId = :riskModelId AND qq.vendorType IN :vendorTypes order by qq.ordinal")
	List<QualitativeQuestions> getListByRiskModelIdAndType(
		@Param("riskModelId") Long riskModelId,
		@Param("vendorTypes") List<VendorType> vendorTypes
	);

	@Query("SELECT distinct qq FROM QualitativeQuestions qq LEFT JOIN qq.vendors v JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE qq.riskModelId = :riskModelId AND qm.id IN :metricId order by qq.ordinal")
	List<QualitativeQuestions> getListByRiskModelIdAndMetric(
		@Param("riskModelId") Long riskModelId,
		@Param("metricId") List<Long> metricId
	);

	@Query("SELECT distinct qq FROM QualitativeQuestions qq LEFT JOIN qq.vendors v JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE qq.riskModelId = :riskModelId AND qm.id IN :metricId AND qq.vendorType IN :vendorTypes order by qq.ordinal")
	List<QualitativeQuestions> getListByRiskModelIdAndTypeAndMetric(
		@Param("riskModelId") Long riskModelId,
		@Param("metricId") List<Long> metricId,
		@Param("vendorTypes") List<VendorType> vendorTypes
	);

	@Query("SELECT distinct qq FROM QualitativeQuestions qq LEFT JOIN qq.vendors v JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"LEFT JOIN FETCH qq.createdBy LEFT JOIN FETCH qq.updatedBy " +
		"WHERE qq.riskModelId = :riskModelId AND (qq.allVendorsSelected = true OR v.id = :vendorId) AND md.id=:metricDomainId AND qq.vendorType IN :vendorTypes")
	List<QualitativeQuestions> getListByRiskModelIdAndVendorAndMetricDomain(
		@Param("riskModelId") Long riskModelId,
		@Param("vendorId") Long vendorId,
		@Param("metricDomainId") Long metricDomainId,
		@Param("vendorTypes") List<VendorType> vendorTypes,
		Pageable pageable
	);

	@Query("SELECT distinct qq FROM QualitativeQuestions qq LEFT JOIN qq.systems s JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"LEFT JOIN FETCH qq.createdBy LEFT JOIN FETCH qq.updatedBy " +
		"WHERE qq.riskModelId = :riskModelId AND (qq.allVendorsSelected = true OR s.id = :systemId) AND md.id=:metricDomainId AND qq.vendorType IN :vendorTypes")
	List<QualitativeQuestions> getListByRiskModelIdAndSystemAndMetricDomain(
		@Param("riskModelId") Long riskModelId,
		@Param("systemId") Long systemId,
		@Param("metricDomainId") Long metricDomainId,
		@Param("vendorTypes") List<VendorType> vendorTypes,
		Pageable pageable
	);

	@Query("SELECT distinct qq FROM QualitativeQuestions qq LEFT JOIN qq.systems s JOIN qq.qualitativeMetric qm " +
		"LEFT JOIN FETCH qq.createdBy LEFT JOIN FETCH qq.updatedBy " +
		"WHERE qq.riskModelId = :riskModelId AND (qq.allVendorsSelected = true OR s.id = :systemId) AND qq.vendorType IN :vendorTypes")
	List<QualitativeQuestions> getListByRiskModelIdAndSystem(
		@Param("riskModelId") Long riskModelId,
		@Param("systemId") Long systemId,
		@Param("vendorTypes") List<VendorType> vendorTypes,
		Pageable pageable
	);

	@Query("SELECT count(qq) FROM QualitativeQuestions qq JOIN qq.vendors v JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md " +
		"WHERE qq.riskModelId = :riskModelId AND v.id = :vendorId AND md.id=:metricDomainId")
	Long getCountByRiskModelIdAndVendorAndMetricDomain(
		@Param("riskModelId") Long riskModelId,
		@Param("vendorId") Long vendorId,
		@Param("metricDomainId") Long metricDomainId
	);

	List<QualitativeQuestions> findAllByRiskModelIdAndQuestionStartsWithIgnoreCaseAndIdIsNotIn(Long riskModelId, String question, Collection<Long> excludeIds, Pageable pageable);

	Long countAllByRiskModelIdAndQuestionStartsWithIgnoreCaseAndIdIsNotIn(Long riskModelId, String question, Collection<Long> excludeIds);

	List<QualitativeQuestions> findAllByRiskModelIdAndQuestionStartsWithAndVendorTypeAndIdIsNotIn(Long riskModelId, String question, VendorType vendorType, Collection<Long> excludeIds, Pageable pageable);

	Long countAllByRiskModelIdAndQuestionStartsWithAndVendorTypeAndIdIsNotIn(Long riskModelId, String question, VendorType vendorType, Collection<Long> excludeIds);

	Optional<QualitativeQuestions> findFirstByRiskModelIdAndQuestion(Long riskModelId, String question);

	Optional<QualitativeQuestions> findFirstByRiskModelIdAndCode(Long riskModelId, String code);

}
