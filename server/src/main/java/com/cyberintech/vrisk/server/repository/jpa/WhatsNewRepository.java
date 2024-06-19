package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Webinars;
import com.cyberintech.vrisk.server.model.jpa.entity.WhatsNew;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface WhatsNewRepository extends CoreRepository<WhatsNew, Long> {
	Optional<WhatsNew> findById(Long id);

	@Query("SELECT wn FROM WhatsNew wn WHERE UPPER(wn.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<WhatsNew> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(wn) FROM WhatsNew wn WHERE UPPER(wn.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	@Query("SELECT w FROM WhatsNew w WHERE UPPER(w.name) LIKE (CONCAT(UPPER(:name), '%')) AND (w.expiryDate IS NULL OR w.expiryDate >= :expiryDate)")
	List<WhatsNew> getListByNameAndExpiryDate(@Param("name") String name, @Param("expiryDate") Date expiryDate, Pageable pageable);

	@Query("SELECT count(w) FROM WhatsNew w WHERE UPPER(w.name) LIKE (CONCAT(UPPER(:name), '%')) AND (w.expiryDate IS NULL OR w.expiryDate >= :expiryDate)")
	Long getCountByNameAndExpiryDate(@Param("name") String name, @Param("expiryDate") Date expiryDate);

	Optional<WhatsNew> findFirstByUrl(String url);

	Optional<WhatsNew> findFirstByName(String name);

	Optional<WhatsNew> findByUid(String uid);

}


