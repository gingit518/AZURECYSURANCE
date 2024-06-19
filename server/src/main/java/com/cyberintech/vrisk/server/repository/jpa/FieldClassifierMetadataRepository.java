package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.FieldClassifiers;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldClassifierMetadataRepository extends CoreRepository<FieldClassifiers, Long> {
}
