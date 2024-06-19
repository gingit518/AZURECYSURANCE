package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.CacheMetricsData;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CacheMetricsDataRepository extends CoreRepository<CacheMetricsData, Long> {
	@Override
	Optional<CacheMetricsData> findById(Long aLong);

	@Modifying
	@Query("delete from CacheMetricsData a where a.organizationId=:organizationId")
	void deleteByOrganizationId(@Param("organizationId") Long organizationId);

	@Query("select a from CacheMetricsData a where a.organizationId=:organizationId")
	List<CacheMetricsData> getByOrganizationId(@Param("organizationId") Long organizationId);

	@Modifying
	@Query("delete from CacheMetricsData a where a.riskModelId=:riskModelId")
	void deleteByRiskModelId(@Param("riskModelId") Long riskModelId);

	@Query("SELECT d FROM CacheMetricsData d WHERE UPPER(d.systemName) LIKE (CONCAT(UPPER(:name), '%'))")
	List<CacheMetricsData> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(d) FROM CacheMetricsData d WHERE UPPER(d.systemName) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

}
