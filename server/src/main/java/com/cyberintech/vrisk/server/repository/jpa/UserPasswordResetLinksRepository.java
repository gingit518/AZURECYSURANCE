package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.DocumentAccessTokens;
import com.cyberintech.vrisk.server.model.jpa.entity.UserPasswordResetLinks;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserPasswordResetLinksRepository extends CoreRepository<UserPasswordResetLinks, Long> {

	Optional<UserPasswordResetLinks> findById(Long id);

	Optional<UserPasswordResetLinks> findFirstByCode(String code);

	@Query("select d from UserPasswordResetLinks d where d.expiredAt<:expiredAt")
	Set<DocumentAccessTokens> getAllExpiredTokens(@Param("expiredAt") Date expiredAt);

	@Modifying
	@Query("delete from UserPasswordResetLinks d where d.expiredAt<:expiredAt")
	void deleteAllExpiredTokens(@Param("expiredAt") Date expiredAt);

}
