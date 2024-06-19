package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.TechnologiesMetadata;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnologyMetadataRepository extends CoreRepository<TechnologiesMetadata, Long> {
}
