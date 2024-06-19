package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Permissions;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends CoreRepository<Permissions, Long> {

	@Query(value = "SELECT p FROM Users u JOIN u.roles r JOIN r.permissions p WHERE u.id = :userId")
	Set<Permissions> getPermissionsByUser(@Param("userId") Long userId);

	@Query("SELECT count(p) FROM Users u JOIN u.roles r JOIN r.permissions p WHERE u.id = :userId AND p.name=:name")
	Long getCountPermissionsByUserAndName(@Param("userId") Long userId, @Param("name") String name);

	Optional<Permissions> findById(Long id);

	Permissions findOneByNameIgnoreCase(String name);

	@Query(value = "SELECT p FROM Permissions p ORDER BY p.itemOrder ASC, p.id ASC")
	List<Permissions> getAllOrderedByItemOrder();

}
