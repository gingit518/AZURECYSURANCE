package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.SecurityAuditComments;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityAuditCommentRepository extends CoreRepository<SecurityAuditComments, Long> {

	Optional<SecurityAuditComments> findById(Long id);

}
