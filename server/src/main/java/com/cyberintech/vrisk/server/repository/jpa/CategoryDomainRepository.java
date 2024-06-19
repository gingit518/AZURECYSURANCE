package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.CategoryDomains;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryDomainRepository extends CoreRepository<CategoryDomains, Long> {

	Optional<CategoryDomains> findById(Long id);

	@Query("SELECT rcd FROM CategoryDomains rcd JOIN FETCH rcd.riskModelDomain rmd " +
		"JOIN FETCH rmd.riskDomain WHERE rcd.riskModelId = ?1")
	List<CategoryDomains> getListByRiskModelId(Long riskModelId);

}
