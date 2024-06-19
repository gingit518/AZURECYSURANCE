package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentLevels;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentLevelsRepository extends CoreRepository<AssessmentLevels, Long> {

	Optional<AssessmentLevels> findById(Long id);

	@Query("SELECT al FROM AssessmentLevels al " +
		"WHERE UPPER(al.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<AssessmentLevels> getListByName(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(al) FROM AssessmentLevels al " +
		"WHERE UPPER(al.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(
		@Param("name") String name
	);

	Optional<AssessmentLevels> findFirstByNameIgnoreCase(String name);

}
