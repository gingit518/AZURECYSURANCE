package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.PackagePlans;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackagePlansRepository extends CoreRepository<PackagePlans, Long> {

	Optional<PackagePlans> findById(Long id);

	@Query("SELECT pp FROM PackagePlans pp WHERE UPPER(pp.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<PackagePlans> getAllByName(@Param("name") String name);

	@Query("SELECT pp FROM PackagePlans pp WHERE UPPER(pp.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(pp.description) LIKE CONCAT('%', UPPER(:name), '%')")
	List<PackagePlans> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT COUNT(pp) FROM PackagePlans pp WHERE UPPER(pp.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(pp.description) LIKE CONCAT('%', UPPER(:name), '%')")
	Long getCountByName(@Param("name") String name);

}
