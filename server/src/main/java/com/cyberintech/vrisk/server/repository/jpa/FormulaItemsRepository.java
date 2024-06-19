package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.FormulaItems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormulaItemsRepository extends CoreRepository<FormulaItems, Long> {

	Optional<FormulaItems> findById(Long id);
}
