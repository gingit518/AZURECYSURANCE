package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.VariableTypeRelation;
import com.cyberintech.vrisk.server.model.jpa.entity.VariableTypes;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableTypesRepository extends CoreRepository<VariableTypes, Long> {

	Optional<VariableTypes> findById(Long id);

	@Query("SELECT v FROM VariableTypes v WHERE v.relation = :relationName AND v.id NOT IN :excludeIds")
	List<VariableTypes> getListByRelationName(
		@Param("relationName") VariableTypeRelation relationName,
		@Param("excludeIds") List<Long> excludeIds,
		Pageable pageable
	);

	@Query("SELECT count(v) FROM VariableTypes v WHERE v.relation = :relationName AND v.id NOT IN :excludeIds")
	Long getCountByRelationName(@Param("relationName") VariableTypeRelation relationName, @Param("excludeIds") List<Long> excludeIds);

	@Query("SELECT v FROM  VariableTypes v WHERE UPPER(v.name) LIKE CONCAT(UPPER(:name), '%') AND v.relation = :relationName")
	Optional<VariableTypes> findByNameAndRelation(@Param("name") String name, @Param("relationName") VariableTypeRelation relationName);
}
