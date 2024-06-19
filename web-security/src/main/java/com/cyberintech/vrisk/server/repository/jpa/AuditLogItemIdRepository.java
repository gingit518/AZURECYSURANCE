package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AuditLog;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuditLogItemIdRepository extends CoreRepository<AuditLogItemId, Long> {
}
