package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationSecurityCertificates;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * External Analytics Parameters Repository
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@Repository
public interface OrganizationSecurityCertificatesRepository extends CoreRepository<OrganizationSecurityCertificates, Long> {

	Optional<OrganizationSecurityCertificates> findById(Long id);

	Optional<OrganizationSecurityCertificates> findFirstByUserIdAndCertificateTypeAndIsActive(Long userId, ExternalAnalyticsType certificateType, Boolean isActive);

	Optional<OrganizationSecurityCertificates> findFirstByOrganizationIdAndCertificateTypeAndIsActive(Long organizationId, ExternalAnalyticsType certificateType, Boolean isActive);

}
