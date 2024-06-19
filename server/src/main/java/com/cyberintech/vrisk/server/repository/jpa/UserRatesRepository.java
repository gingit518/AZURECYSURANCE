package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.model.jpa.entity.UserRates;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRatesRepository extends CoreRepository<UserRates, Long> {

	Optional<UserRates> findById(Long id);

	Optional<UserRates> findOneByIdAndOrganizationId(Long id, Long organizationId);

}
