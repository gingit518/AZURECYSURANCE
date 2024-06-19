package com.cyberintech.vrisk.server.repository;

import com.cyberintech.vrisk.server.model.jpa.entity.AccessToken;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTokenRepository extends CoreRepository<AccessToken, String> {

	Optional<AccessToken> findById(String id);

}
