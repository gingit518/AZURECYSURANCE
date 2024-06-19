package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.UsersAgreements;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAgreementRepository extends CoreRepository<UsersAgreements, Long> {

	Optional<UsersAgreements> findById(Long id);

	Optional<UsersAgreements> findByIdAndUserId(Long id, Long userId);

	@Query("SELECT DISTINCT ua FROM UsersAgreements ua LEFT JOIN FETCH ua.organizationAgreement orga " +
		"LEFT JOIN FETCH ua.user u " +
		"WHERE orga.id = :organizationAgreementId AND u.id = :userId")
	Optional<UsersAgreements> getByOrganizationAgreementIdAndUserId(
		@Param("organizationAgreementId") Long organizationAgreementId,
		@Param("userId") Long userId
	);

	@Query("SELECT CASE WHEN count(ua) > 0 THEN true ELSE false END " +
		"FROM UsersAgreements ua JOIN ua.organizationAgreement orga JOIN ua.user u " +
		"WHERE orga.id = :organizationAgreementId AND u.id = :userId")
	Boolean existByOrganizationAgreementIdAndUserId(
		@Param("organizationAgreementId") Long organizationAgreementId,
		@Param("userId") Long userId
	);

	@Query("SELECT ua FROM UsersAgreements ua JOIN ua.organizationAgreement orga " +
		"JOIN ua.user u " +
		"WHERE u.id = :userId AND ua.isAnswered = false")
	List<UsersAgreements> getListByUserIdAndIsAnsweredFalse(
		@Param("userId") Long userId
	);

	@Query("SELECT count(ua) FROM UsersAgreements ua JOIN ua.organizationAgreement orga " +
		" JOIN ua.user u " +
		"WHERE u.id = :userId AND ua.isAnswered = false")
	Long getCountByUserIdAndIsAnsweredFalse(
		@Param("userId") Long userId
	);

	@Query("SELECT DISTINCT ua FROM UsersAgreements ua LEFT JOIN FETCH ua.organizationAgreement orga " +
		"LEFT JOIN FETCH ua.user u LEFT JOIN FETCH orga.organizations o " +
		"WHERE o.id = :organizationId AND u.id = :userId")
	List<UsersAgreements> getListByOrganizationIdAndUserId(
		@Param("organizationId") Long organizationId,
		@Param("userId") Long userId
	);

	@Query("SELECT count(DISTINCT ua) FROM UsersAgreements ua JOIN ua.organizationAgreement orga JOIN ua.user u " +
		"LEFT JOIN orga.organizations o " +
		"WHERE o.id = :organizationId AND u.id = :userId")
	Long getCountByOrganizationIdAndUserId(
		@Param("organizationId") Long organizationId,
		@Param("userId") Long userId
	);
}
