package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Status;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends CoreRepository<Status, Long> {

	Optional<Status> findById(Long id);

	Optional<Status> findFirstByName(String statusName);

	@Query("SELECT s FROM Status s WHERE UPPER(s.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Status> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(s) FROM Status s WHERE UPPER(s.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

}
