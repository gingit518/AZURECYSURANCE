package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.UsersMetadata;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersMetadataRepository extends CoreRepository<UsersMetadata, Long> {
}
