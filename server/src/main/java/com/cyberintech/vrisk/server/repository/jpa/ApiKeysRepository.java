package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.ApiKeys;
import com.cyberintech.vrisk.server.model.jpa.entity.UserRates;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeysRepository extends CoreRepository<ApiKeys, Long> {

	Optional<ApiKeys> findById(Long id);

	Optional<ApiKeys> findOneByIdAndOrganizationId(Long id, Long organizationId);

	Optional<ApiKeys> findFirstByApiKeyPublic(String publicKey);

	Optional<ApiKeys> findFirstByApiKeyPrivate(String apiKeyPrivate);

	@Query("SELECT a FROM ApiKeys a JOIN FETCH a.user u LEFT JOIN FETCH u.roles r WHERE a.apiKeyPrivate = :apiKeyPrivate")
	Optional<ApiKeys> findFirstByApiKeyPrivateFetchRoles(@Param("apiKeyPrivate") String apiKeyPrivate);

}
