package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Contract;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends CoreRepository<Contract, Long> {

	Optional<Contract> findById(Long id);

	Optional<Contract> findByOrganizationId(Long organizationId);

	Optional<Contract> findFirstByNumber(String contractNumber);

	@Query("SELECT c FROM Contract c " +
		"WHERE UPPER(c.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Contract> getListByName(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(c) FROM Contract c " +
		"WHERE UPPER(c.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(
		@Param("name") String name
	);
}
