package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CoreRepository<Users, Long> {

	Optional<Users> findById(Long id);

	Optional<Users> findByEmail(String email);

	@Query("SELECT u FROM Users u LEFT JOIN FETCH u.roles r WHERE UPPER(u.email) = UPPER(:email)")
	Optional<Users> findByEmailFetchRoles(@Param("email") String email);

	Optional<Users> findFirstByEmailIgnoreCase(String email);

	Optional<Users> findFirstByEmailAndOrganization(String email, Organizations organization);

	Optional<Users> findFirstByEmailIgnoreCaseAndOrganization(String email, Organizations organization);

	Optional<Users> findFirstByFullNameAndOrganization(String fullName, Organizations organization);

	Optional<Users> findFirstByEmailAndIdIsNotIn(String email, Collection<Long> excludeIds);

	List<Users> findByEmailIsLike(String email);

	List<Users> findAllByEmailIgnoreCase(String email);

	@Query("SELECT u FROM Users u WHERE u.organization.id = ?1")
	List<Users> getListByOrganization(Long organizationId);

	@Query("SELECT u FROM Users u WHERE u.organization.id = ?1 AND (u.deleted = false OR u.deleted IS NULL)")
	List<Users> getListActiveByOrganization(Long organizationId);

	@Query("SELECT u FROM Users u LEFT JOIN FETCH u.organization o " +
		"LEFT JOIN FETCH u.createdBy cb LEFT JOIN FETCH u.updatedBy ub " +
		"WHERE UPPER(u.fullName) LIKE (CONCAT(UPPER(:name), '%')) AND u.id NOT IN :excludeIds AND u.deleted = false")
	List<Users> filterUsersByName(@Param("name") String name, @Param("excludeIds") Collection<Long> excludeIds, Pageable pageable);

	@Query("SELECT count(u) FROM Users u WHERE UPPER(u.fullName) LIKE (CONCAT(UPPER(:name), '%')) AND u.id NOT IN :excludeIds AND u.deleted = false")
	Long getCountUsersByName(@Param("name") String name, @Param("excludeIds") Collection<Long> excludeIds);

	@Query("SELECT u FROM Users u LEFT JOIN FETCH u.organization o JOIN u.roles r " +
		"LEFT JOIN FETCH u.createdBy cb LEFT JOIN FETCH u.updatedBy ub " +
		"WHERE UPPER(u.fullName) LIKE (CONCAT(UPPER(:name), '%')) AND r.name IN :roles AND u.id NOT IN :excludeIds AND u.deleted = false")
	List<Users> filterUsersByNameAndRoles(@Param("name") String name, @Param("roles") Collection<String> roles, @Param("excludeIds") Collection<Long> excludeIds, Pageable pageable);

	@Query("SELECT count(u) FROM Users u JOIN u.roles r WHERE UPPER(u.fullName) LIKE (CONCAT(UPPER(:name), '%')) AND r.name IN :roles AND u.id NOT IN :excludeIds AND u.deleted = false")
	Long getCountUsersByNameAndRoles(@Param("name") String name, @Param("roles") Collection<String> roles, @Param("excludeIds") Collection<Long> excludeIds);

	@Query("SELECT u FROM Users u JOIN u.organization o " +
		"LEFT JOIN FETCH u.createdBy cb LEFT JOIN FETCH u.updatedBy ub " +
		"WHERE o.id = :organizationId AND UPPER(u.fullName) LIKE (CONCAT(UPPER(:name), '%')) AND u.id NOT IN :excludeIds AND u.deleted = false")
	List<Users> filterUsersByOrganizationAndName(@Param("organizationId") Long organizationId, @Param("name") String name, @Param("excludeIds") Collection<Long> excludeIds, Pageable pageable);

	@Query("SELECT count(u) FROM Users u JOIN u.organization o WHERE o.id = :organizationId AND UPPER(u.fullName) LIKE (CONCAT(UPPER(:name), '%')) AND u.id NOT IN :excludeIds AND u.deleted = false")
	Long getCountUsersByOrganizationAndName(@Param("organizationId") Long organizationId, @Param("name") String name, @Param("excludeIds") Collection<Long> excludeIds);

	@Query("SELECT DISTINCT u FROM Users u JOIN u.organization o JOIN u.roles r " +
		"WHERE o.id = :organizationId AND UPPER(u.fullName) LIKE (CONCAT(UPPER(:name), '%')) AND r.name IN :roles AND u.id NOT IN :excludeIds AND u.deleted = false")
	List<Users> filterUsersByOrganizationAndNameAndRoles(@Param("organizationId") Long organizationId, @Param("name") String name, @Param("roles") Collection<String> roles, @Param("excludeIds") Collection<Long> excludeIds, Pageable pageable);

	@Query("SELECT count(u) FROM Users u JOIN u.organization o JOIN u.roles r WHERE o.id = :organizationId " +
		"AND UPPER(u.fullName) LIKE (CONCAT(UPPER(:name), '%')) AND r.name IN :roles AND u.id NOT IN :excludeIds AND u.deleted = false")
	Long getCountUsersByOrganizationAndNameAndRoles(@Param("organizationId") Long organizationId, @Param("name") String name, @Param("roles") Collection<String> roles, @Param("excludeIds") Collection<Long> excludeIds);

}
