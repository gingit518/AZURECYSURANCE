package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Quants;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuantsRepository extends CoreRepository<Quants, Long> {

	Optional<Quants> findById(Long id);

	@Query("SELECT qm FROM Quants qm LEFT JOIN FETCH qm.createdBy LEFT JOIN FETCH qm.updatedBy")
	List<Quants> getList();

	@Query("SELECT q FROM Quants q LEFT JOIN FETCH q.createdBy LEFT JOIN FETCH q.updatedBy " +
		"WHERE (UPPER(q.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(q.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<Quants> getListByName(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(q) FROM Quants q " +
		"WHERE (UPPER(q.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(q.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByName(
		@Param("name") String name
	);

	Optional<Quants> findFirstByName(String name);
}
