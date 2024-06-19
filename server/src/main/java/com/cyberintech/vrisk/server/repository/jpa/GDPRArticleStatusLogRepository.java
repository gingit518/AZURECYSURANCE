package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleStatusLog;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GDPRArticleStatusLogRepository extends CoreRepository<GDPRArticleStatusLog, Long> {

	Optional<GDPRArticleStatusLog> findById(Long id);

	Optional<GDPRArticleStatusLog> findByIdAndOrganizationId(Long id, Long organizationId);

}
