package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.RateTypes;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RateTypeRepository extends CoreRepository<RateTypes, Long> {

	Optional<RateTypes> findById(Long id);

	@Query("SELECT rtp FROM RateTypes rtp WHERE UPPER(rtp.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<RateTypes> getListByOrganizationAndName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(rtp) FROM RateTypes rtp WHERE UPPER(rtp.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(@Param("name") String name);

}
