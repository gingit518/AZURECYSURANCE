package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.LicenseTypes;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseTypeRepository extends CoreRepository<LicenseTypes, Long> {

	Optional<LicenseTypes> findById(Long id);

	@Query("SELECT ltp FROM LicenseTypes ltp WHERE UPPER(ltp.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<LicenseTypes> getListByOrganizationAndName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(ltp) FROM LicenseTypes ltp WHERE UPPER(ltp.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(@Param("name") String name);

}
