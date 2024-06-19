package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.ApiKeys;
import com.cyberintech.vrisk.server.model.jpa.entity.UserRates;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeysRepository extends CoreRepository<ApiKeys, Long> {

	Optional<ApiKeys> findById(Long id);

	Optional<ApiKeys> findOneByIdAndOrganizationId(Long id, Long organizationId);

	Optional<ApiKeys> findFirstByApiKeyPublic(String publicKey);

}
