package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import com.cyberintech.vrisk.server.model.jpa.entity.ExternalAnalytics;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * External Analytics Repository
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@Repository
public interface ExternalAnalyticsRepository extends CoreRepository<ExternalAnalytics, Long> {

	Optional<ExternalAnalytics> findById(Long id);

	Optional<ExternalAnalytics> findByIdAndOrganizationId(Long id, Long organizationId);

	List<ExternalAnalytics> findAllByOrganizationId(Long organizationId);

	@Query(value = "SELECT distinct ea.*, random() as randomOrder FROM external_analytics ea LEFT JOIN external_analytics_to_roles r on r.external_analytic_id=ea.id " +
		"WHERE ea.organization_id = :organizationId AND (ea.is_public = true OR r.role_id IN :roles) ORDER BY randomOrder LIMIT :limit", nativeQuery = true)
	List<ExternalAnalytics> getRandomListByRoles(@Param("roles") List<Long> roles, @Param("organizationId") Long organizationId, @Param("limit") Long limit);

	@Query("SELECT distinct ea FROM ExternalAnalytics ea LEFT JOIN ea.roles r LEFT JOIN FETCH ea.externalAnalyticsParameters eap WHERE ea.organizationId = :organizationId AND (ea.isPublic = true OR r.id IN :roles) ORDER BY ea.name ASC")
	List<ExternalAnalytics> getListByRolesAndOrganizationId(@Param("roles") List<Long> roles, @Param("organizationId") Long organizationId);

	@Query("SELECT distinct ea FROM ExternalAnalytics ea LEFT JOIN ea.roles r WHERE ea.organizationId = :organizationId AND (ea.isPublic = true OR r.id IN :roles) AND ea.externalAnalyticsType = :externalAnalyticsType ORDER BY ea.name ASC")
	List<ExternalAnalytics> getListByRolesAndOrganizationIdAndType(@Param("roles") List<Long> roles, @Param("organizationId") Long organizationId, @Param("externalAnalyticsType") ExternalAnalyticsType externalAnalyticsType);

	@Query("SELECT distinct ea FROM ExternalAnalytics ea LEFT JOIN FETCH ea.externalAnalyticsParameters p WHERE ea.organizationId = :organizationId AND ea.externalAnalyticsType = :externalAnalyticsType ORDER BY ea.name ASC")
	List<ExternalAnalytics> getListByOrganizationIdAndType(@Param("organizationId") Long organizationId, @Param("externalAnalyticsType") ExternalAnalyticsType externalAnalyticsType);

	@Query("SELECT distinct ea FROM ExternalAnalytics ea LEFT JOIN ea.roles r WHERE ea.isPublic = true OR r.id IN :roles")
	List<ExternalAnalytics> getListByRoles(@Param("roles") List<Long> roles);

	@Query("SELECT ea FROM ExternalAnalytics ea WHERE UPPER(ea.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<ExternalAnalytics> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(ea) FROM ExternalAnalytics ea WHERE UPPER(ea.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	@Query("SELECT ea FROM ExternalAnalytics ea WHERE ea.organizationId = :organizationId and UPPER(ea.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<ExternalAnalytics> getListByNameAndOrganization(@Param("name") String name, @Param("organizationId") Long organizationId, Pageable pageable);

	@Query("SELECT count(ea) FROM ExternalAnalytics ea WHERE ea.organizationId = :organizationId and UPPER(ea.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByNameAndOrganization(@Param("name") String name, @Param("organizationId") Long organizationId);

}
