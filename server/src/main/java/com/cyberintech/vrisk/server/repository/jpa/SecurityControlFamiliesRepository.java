package com.cyberintech.vrisk.server.repository.jpa;

import com.amazonaws.services.guardduty.model.Organization;
import com.cyberintech.vrisk.server.model.jpa.entity.SecurityControlFamilies;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityControlFamiliesRepository extends CoreRepository<SecurityControlFamilies, Long> {

	Optional<SecurityControlFamilies> findById(Long id);

	@Query("SELECT cf FROM SecurityControlFamilies cf WHERE UPPER(cf.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<SecurityControlFamilies> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(cf) FROM SecurityControlFamilies cf WHERE UPPER(cf.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	Optional<SecurityControlFamilies> findFirstByNameIgnoreCase(String name);

	Optional<SecurityControlFamilies> findFirstByNameIgnoreCaseAndOrganizationIdIsNull(String name);

	Optional<SecurityControlFamilies> findFirstByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);

}
