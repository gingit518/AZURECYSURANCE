package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends CoreRepository<Roles, Long> {

	@Override
	Iterable<Roles> findAll(Sort sort);

	@Query(value = "SELECT r.* FROM roles r INNER JOIN user_roles ur ON ur.role_id=r.id " +
		"INNER JOIN users u ON ur.user_id = u.id WHERE UPPER(u.email) = UPPER(:email)", nativeQuery = true)
	List<Roles> getUserRolesByEmail(@Param("email") String email);

	@Query(value = "SELECT r FROM Roles r WHERE UPPER(r.name) LIKE (CONCAT(UPPER(:name), '%')) AND r.id > :minRoleId")
	List<Roles> getRolesByNameForNonAdmin(@Param("name") String name, @Param("minRoleId") Long minRoleId, Pageable pageable);

	@Query(value = "SELECT count(r) FROM Roles r WHERE UPPER(r.name) LIKE (CONCAT(UPPER(:name), '%')) AND r.id > :minRoleId")
	Long getCountRolesByNameForNonAdmin(@Param("name") String name, @Param("minRoleId") Long minRoleId);

	@Query(value = "SELECT r FROM PackagePlans p join p.roles r WHERE UPPER(r.name) LIKE (CONCAT(UPPER(:name), '%')) AND p.id = :packagePlanId")
	List<Roles> getRolesByNameForNonAdminAndPackagePlan(@Param("name") String name, @Param("packagePlanId") Long packagePlanId, Pageable pageable);

	@Query(value = "SELECT count(r) FROM PackagePlans p join p.roles r WHERE UPPER(r.name) LIKE (CONCAT(UPPER(:name), '%')) AND p.id = :packagePlanId")
	Long getCountRolesByNameForNonAdminAndPackagePlan(@Param("name") String name, @Param("packagePlanId") Long packagePlanId);

	Optional<Roles> findById(Long id);

	Roles findOneByName(String name);

	Roles findOneByNameIgnoreCase(String name);

}
