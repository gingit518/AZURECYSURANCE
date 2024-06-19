package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.FieldClassifiers;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FieldClassifierRepository extends CoreRepository<FieldClassifiers, Long> {

	Optional<FieldClassifiers> findByName(String name);

	@Query("select fc from FieldClassifiers fc join fc.metadata meta where meta.key = :metadataKey and meta.value = :metadataValue")
	Optional<FieldClassifiers> findByMeta(@Param("metadataKey") String metadataKey,
										  @Param("metadataValue") String metadataValue);

}
