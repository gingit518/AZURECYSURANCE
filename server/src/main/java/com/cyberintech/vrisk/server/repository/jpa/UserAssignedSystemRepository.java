package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.model.jpa.entity.UserAssignedSystem;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAssignedSystemRepository extends CoreRepository<UserAssignedSystem, Long> {

	Optional<UserAssignedSystem> findById(Long id);

	void deleteAllBySystem(Systems system);

	List<UserAssignedSystem> findAllByUser(Users user);

	@Query("SELECT uas FROM UserAssignedSystem uas JOIN FETCH uas.user u JOIN uas.system s WHERE s.organizationId = :organizationId")
	List<UserAssignedSystem> getSystemAssignmentsForOrganization(@Param("organizationId") Long organizationId);

	@Query("SELECT DISTINCT s FROM UserAssignedSystem uas JOIN uas.user u JOIN uas.system s WHERE s.organizationId = :organizationId AND u.id = :userId")
	List<Systems> getSystemsForUser(@Param("userId") Long userId, @Param("organizationId") Long organizationId);

	@Query("SELECT DISTINCT u FROM UserAssignedSystem uas JOIN uas.user u JOIN uas.system s WHERE s.organizationId = :organizationId AND s.id = :systemId")
	List<Users> getUsersForSystem(@Param("systemId") Long systemId, @Param("organizationId") Long organizationId);

	UserAssignedSystem findFirstByUserAndSystem(Users user, Systems system);

}
