package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.News;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends CoreRepository<News, Long> {
	Optional<News> findById(Long id);

	@Query("SELECT n FROM News n WHERE UPPER(n.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<News> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(n) FROM News n WHERE UPPER(n.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	@Query("SELECT n FROM News n WHERE UPPER(n.name) LIKE (CONCAT(UPPER(:name), '%')) AND (n.expiryDate IS NULL OR n.expiryDate >= :expiryDate)")
	List<News> getListByNameAndExpiryDate(@Param("name") String name, @Param("expiryDate") Date expiryDate, Pageable pageable);

	@Query("SELECT count(n) FROM News n WHERE UPPER(n.name) LIKE (CONCAT(UPPER(:name), '%')) AND (n.expiryDate IS NULL OR n.expiryDate >= :expiryDate)")
	Long getCountByNameAndExpiryDate(@Param("name") String name, @Param("expiryDate") Date expiryDate);

	Optional<News> findFirstByUrl(String url);

	Optional<News> findByUid(String uid);

}
