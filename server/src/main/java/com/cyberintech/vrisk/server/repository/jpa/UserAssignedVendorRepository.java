package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.UserAssignedSystem;
import com.cyberintech.vrisk.server.model.jpa.entity.UserAssignedVendor;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAssignedVendorRepository extends CoreRepository<UserAssignedVendor, Long> {

	Optional<UserAssignedVendor> findById(Long id);

	List<UserAssignedVendor> findAllByUser(Users user);

	@Query("SELECT DISTINCT v FROM UserAssignedVendor uav JOIN uav.user u JOIN uav.vendor v WHERE v.rootParent.id = :organizationId AND u.id = :userId")
	List<Organizations> getVendorsForUser(@Param("userId") Long userId, @Param("organizationId") Long organizationId);

	UserAssignedVendor findFirstByUserAndVendor(Users user, Organizations organization);

	void deleteAllByVendor(Organizations vendor);

}
