package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.MetricFormulaItems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetricFormulaItemsRepository extends CoreRepository<MetricFormulaItems, Long> {

	Optional<MetricFormulaItems> findById(Long id);
	@Override
	void deleteById(Long id);

	@Query("SELECT mfi FROM  MetricFormulaItems mfi WHERE UPPER(mfi.name) LIKE CONCAT(UPPER(:name), '%') AND mfi.quantMetricId = :quantMetricId")
	Optional<MetricFormulaItems> findByNameAndQuantMetricId(@Param("name") String name, @Param("quantMetricId") Long quantMetricId);

}
