package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends CrudRepository<Roles, Long> {

	@Query(value = "SELECT r.* FROM roles r INNER JOIN user_roles ur ON ur.role_id=r.id " +
		"INNER JOIN users u ON ur.user_id = u.id WHERE UPPER(u.email) = UPPER(:email)", nativeQuery = true)
	List<Roles> getUserRolesByEmail(@Param("email") String email);
}
