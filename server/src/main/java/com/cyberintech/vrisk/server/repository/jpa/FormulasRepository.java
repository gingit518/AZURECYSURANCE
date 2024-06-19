package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Formulas;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormulasRepository extends CoreRepository<Formulas, Long> {

	Optional<Formulas> findById(Long id);

	@Query("SELECT f FROM Formulas f LEFT JOIN FETCH f.formulaItems " +
		"WHERE f.organizationId = :organizationId AND UPPER(f.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Formulas> getListByOrganizationAndName(
		@Param(value = "organizationId") Long organizationId,
		@Param(value = "name") String name,
		Pageable pageable
	);

	@Query("SELECT count(f) FROM Formulas f " +
		"WHERE f.organizationId = :organizationId AND UPPER(f.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param(value = "organizationId") Long organizationId,
		@Param(value = "name") String name
	);
}
