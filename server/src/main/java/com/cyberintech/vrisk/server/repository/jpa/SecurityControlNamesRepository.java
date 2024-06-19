package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.SecurityControlNames;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityControlNamesRepository extends CoreRepository<SecurityControlNames, Long> {

	Optional<SecurityControlNames> findById(Long id);

	@Query("SELECT cn FROM SecurityControlNames cn WHERE UPPER(cn.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<SecurityControlNames> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(cn) FROM SecurityControlNames cn WHERE UPPER(cn.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	@Query("SELECT cn FROM SecurityControlNames cn JOIN cn.securityControlFamily cf WHERE cf.id = :controlFamilyId AND UPPER(cn.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<SecurityControlNames> getListByNameAndFamilyId(@Param("name") String name, @Param("controlFamilyId") Long controlFamilyId, Pageable pageable);

	@Query("SELECT count(cn) FROM SecurityControlNames cn JOIN cn.securityControlFamily cf WHERE cf.id = :controlFamilyId AND UPPER(cn.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByNameAndFamilyId(@Param("name") String name, @Param("controlFamilyId") Long controlFamilyId);

	Optional<SecurityControlNames> findFirstByNameIgnoreCase(String name);

	Optional<SecurityControlNames> findFirstByNameIgnoreCaseAndOrganizationIdIsNull(String name);

	Optional<SecurityControlNames> findFirstByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);;

}
