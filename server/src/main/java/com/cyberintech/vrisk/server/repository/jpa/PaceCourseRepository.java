package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.PaceCourse;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaceCourseRepository extends CoreRepository<PaceCourse, Long> {
	Optional<PaceCourse> findById(Long id);

	@Query("SELECT pc FROM PaceCourse pc WHERE UPPER(pc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<PaceCourse> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(pc) FROM PaceCourse pc WHERE UPPER(pc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	@Query("SELECT pc FROM PaceCourse pc WHERE UPPER(pc.name) LIKE (CONCAT(UPPER(:name), '%')) AND (pc.expiryDate IS NULL OR pc.expiryDate >= :expiryDate)")
	List<PaceCourse> getListByNameAndExpiryDate(@Param("name") String name, @Param("expiryDate") Date expiryDate, Pageable pageable);

	@Query("SELECT count(pc) FROM PaceCourse pc WHERE UPPER(pc.name) LIKE (CONCAT(UPPER(:name), '%')) AND (pc.expiryDate IS NULL OR pc.expiryDate >= :expiryDate)")
	Long getCountByNameAndExpiryDate(@Param("name") String name, @Param("expiryDate") Date expiryDate);

	Optional<PaceCourse> findFirstByUrl(String url);

	Optional<PaceCourse> findByUid(String uid);
}


