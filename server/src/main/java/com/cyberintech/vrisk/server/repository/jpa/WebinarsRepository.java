package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.News;
import com.cyberintech.vrisk.server.model.jpa.entity.Webinars;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebinarsRepository extends CoreRepository<Webinars, Long> {
	Optional<Webinars> findById(Long id);

	@Query("SELECT w FROM Webinars w WHERE UPPER(w.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Webinars> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(w) FROM Webinars w WHERE UPPER(w.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	@Query("SELECT w FROM Webinars w WHERE UPPER(w.name) LIKE (CONCAT(UPPER(:name), '%')) AND (w.expiryDate IS NULL OR w.expiryDate >= :expiryDate)")
	List<Webinars> getListByNameAndExpiryDate(@Param("name") String name, @Param("expiryDate") Date expiryDate, Pageable pageable);

	@Query("SELECT count(w) FROM Webinars w WHERE UPPER(w.name) LIKE (CONCAT(UPPER(:name), '%')) AND (w.expiryDate IS NULL OR w.expiryDate >= :expiryDate)")
	Long getCountByNameAndExpiryDate(@Param("name") String name, @Param("expiryDate") Date expiryDate);

	Optional<Webinars> findFirstByUrl(String url);

	Optional<Webinars> findByUid(String uid);
}


