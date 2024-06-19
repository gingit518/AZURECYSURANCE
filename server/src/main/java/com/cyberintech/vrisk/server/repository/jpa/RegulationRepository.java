package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Regulations;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Description
 *
 * @author Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version 0.1.1
 * @since 2020-10-12
 */
@Repository
public interface RegulationRepository extends CoreRepository<Regulations, Long> {

	Optional<Regulations> findById(Long id);

	@Query("SELECT r FROM Regulations r WHERE UPPER(r.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Regulations> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT distinct r FROM QuantMetrics qm JOIN qm.regulations r WHERE qm.riskModelId = :riskModelId ORDER BY r.acronym ASC")
	LinkedHashSet<Regulations> getAllUsedRegulationsInRiskModel(@Param("riskModelId") Long riskModelId);

	@Query("SELECT r FROM QuantMetrics qm JOIN qm.regulations r WHERE r.id IN :ids ORDER BY r.acronym ASC")
	LinkedHashSet<Regulations> verifyUsedRegulations(@Param("ids") List<Long> ids);

	@Query("SELECT count(r) FROM Regulations r WHERE UPPER(r.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	Optional<Regulations> findFirstByAcronym(String acronym);


}
