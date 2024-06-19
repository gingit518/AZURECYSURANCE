package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricDomains;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MetricDomainRepository extends CoreRepository<MetricDomains, Long> {

	List<MetricDomains> findAll();

	@Query("SELECT distinct(md) FROM QualitativeQuestions q inner join q.qualitativeMetric qm inner join qm.metricDomain md WHERE q.vendorType=:questionType AND q.riskModelId=:riskModelId order by md.name")
	List<MetricDomains> getAllByTypeAndNotEmpty(@Param("questionType") VendorType vendorType, @Param("riskModelId") Long riskModelId);

	@Query("SELECT distinct(md) FROM QualitativeQuestions q inner join q.qualitativeMetric qm inner join qm.metricDomain md WHERE q.vendorType=:questionType AND q.riskModelId=:riskModelId AND md.categoryCode=:categoryCode")
	List<MetricDomains> getAllByTypeAndNotEmptyAndCategoryCode(@Param("questionType") VendorType vendorType, @Param("riskModelId") Long riskModelId, @Param("categoryCode") String categoryCode);

	@Query("SELECT distinct(md.categoryCode) FROM QualitativeQuestions q inner join q.qualitativeMetric qm inner join qm.metricDomain md WHERE q.vendorType=:questionType AND q.riskModelId=:riskModelId")
	Set<String> getAllCodesByTypeAndNotEmpty(@Param("questionType") VendorType vendorType, @Param("riskModelId") Long riskModelId);

	MetricDomains findFirstById(Long domainId);

	Optional<MetricDomains> findFirstByCodeIgnoreCase(String code);

	Optional<MetricDomains> findFirstByCodeIgnoreCaseAndOrganizationId(String code, Long organizationId);

	Optional<MetricDomains> findFirstByCodeIgnoreCaseAndOrganizationIdIsNull(String code);

	Optional<MetricDomains> findFirstByNameIgnoreCase(String name);

}
