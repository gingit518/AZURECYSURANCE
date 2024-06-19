package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.IdpType;
import com.cyberintech.vrisk.server.model.jpa.entity.IdpUsers;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdpUserRepository extends CoreRepository<IdpUsers, Long> {

	Optional<IdpUsers> findById(Long id);

	IdpUsers findByUserIdentity(String email);

	Optional<IdpUsers> findFirstByUserIdentityIgnoreCase(String email);

	Optional<IdpUsers> findFirstByUserIdentityIgnoreCaseAndIdpId(String email, IdpType idpType);

}
