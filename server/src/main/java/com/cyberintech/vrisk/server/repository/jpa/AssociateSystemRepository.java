package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssociateSystems;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssociateSystemRepository extends CoreRepository<AssociateSystems, Long> {

	Optional<AssociateSystems> findById(Long id);

	void deleteAllBySystem(Systems systems);

}
