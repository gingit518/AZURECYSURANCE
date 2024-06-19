package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.SupportedLanguages;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupportedLanguagesRepository extends CoreRepository<SupportedLanguages, Long> {

	Optional<SupportedLanguages> findById(Long itemId);

	List<SupportedLanguages> findAll();

	@Query("SELECT DISTINCT sl FROM SupportedLanguages sl LEFT JOIN sl.organizations o WHERE o.id = :organizationId")
	List<SupportedLanguages> getListByOrganizationsId (
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT DISTINCT sl FROM SupportedLanguages sl LEFT JOIN sl.organizations " +
		"WHERE UPPER(sl.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<SupportedLanguages> getListByName(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(sl) FROM SupportedLanguages sl " +
		"WHERE UPPER(sl.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(
		@Param("name") String name
	);

	@Query("SELECT DISTINCT sl FROM SupportedLanguages sl LEFT JOIN sl.organizations " +
		"WHERE UPPER(sl.name) LIKE (CONCAT(UPPER(:name), '%')) AND sl.isPublic = true")
	List<SupportedLanguages> getListByNameAndIsPublic(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(sl) FROM SupportedLanguages sl " +
		"WHERE UPPER(sl.name) LIKE (CONCAT(UPPER(:name), '%')) AND sl.isPublic = true")
	Long getCountByNameAndIsPublic(
		@Param("name") String name
	);

	List<SupportedLanguages> findAllByIsPublicIsTrue();

	Optional<SupportedLanguages> findFirstByCode(String code);

}
