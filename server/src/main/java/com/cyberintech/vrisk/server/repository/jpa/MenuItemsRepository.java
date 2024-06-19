package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.MenuItems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemsRepository extends CoreRepository<MenuItems, Long> {

	Optional<MenuItems> findById(Long id);

	Optional<MenuItems> findFirstByCode(String code);

	Optional<MenuItems> findFirstByCodeAndIdNotIn(String code, List<Long> excludes);

	List<MenuItems> findAllByOrganizationId(Long organizationId);

	@Query("SELECT h FROM MenuItems h WHERE UPPER(h.code) IN :codes")
	List<MenuItems> getListByCodes(@Param("codes") Collection<String> codes);


	@Query("SELECT h FROM MenuItems h WHERE UPPER(h.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<MenuItems> getListByName(@Param("name") String name, Pageable pageable );

	@Query("SELECT count(h) FROM MenuItems h WHERE UPPER(h.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

}
