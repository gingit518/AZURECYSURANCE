package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.DocumentAccessTokens;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentAccessTokensReporitory extends CoreRepository<DocumentAccessTokens, Long> {

	Optional<DocumentAccessTokens> findById(Long id);

	Optional<DocumentAccessTokens> findFirstByCode(String code);

	@Query("select d from DocumentAccessTokens d where d.expiredAt<:expiredAt")
	List<DocumentAccessTokens> getAllExpiredTokens(@Param("expiredAt") Date expiredAt);

	@Modifying
	@Query("delete from DocumentAccessTokens d where d.expiredAt<:expiredAt")
	void deleteAllExpiredTokens(@Param("expiredAt") Date expiredAt);

}
