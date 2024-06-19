package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.LanguageConstantScopeType;
import com.cyberintech.vrisk.server.model.jpa.entity.LanguageConstants;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageConstantRepository extends CoreRepository<LanguageConstants, Long> {

	Optional<LanguageConstants> findById(Long itemId);

	Optional<LanguageConstants> findFirstByName(String name);

	Optional<LanguageConstants> findFirstByNameAndScope(String name, LanguageConstantScopeType scopeType);

}
