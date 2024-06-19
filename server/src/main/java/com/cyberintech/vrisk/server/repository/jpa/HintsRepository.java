package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Hints;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface HintsRepository extends CoreRepository<Hints, Long> {

	Optional<Hints> findById(Long id);

	Optional<Hints> findFirstByCode(String code);

	Optional<Hints> findFirstByCodeAndIdNotIn(String code, List<Long> excludes);

	@Query("SELECT h FROM Hints h WHERE UPPER(h.code) IN :codes")
	List<Hints> getListByCodes(@Param("codes") Collection<String> codes);


	@Query("SELECT h FROM Hints h WHERE UPPER(h.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	List<Hints> getListByName(@Param("name") String name, Pageable pageable );

	@Query("SELECT count(h) FROM Hints h WHERE UPPER(h.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

}
