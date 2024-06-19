package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.CostTypes;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CostTypeRepository extends CoreRepository<CostTypes, Long> {

	Optional<CostTypes> findById(Long id);

	@Query("SELECT cts FROM CostTypes cts WHERE UPPER(cts.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<CostTypes> getListByOrganizationAndName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(cts) FROM CostTypes cts WHERE UPPER(cts.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(@Param("name") String name);

}
