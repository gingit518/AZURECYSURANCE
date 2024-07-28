package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.City;
import com.cyberintech.vrisk.server.model.jpa.entity.Country;
import com.cyberintech.vrisk.server.model.jpa.entity.State;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends CoreRepository<City, Long> {

	Optional<City> findById(Long id);

	Optional<City> findFirstByNameAndCountryAndState(String name, Country country, State state);

	Optional<City> findFirstByNameAndCountry(String name, Country country);

	Optional<City> findFirstByNameAndState(String name, State state);

	@Query("SELECT ct FROM City ct JOIN ct.country c WHERE c.id = :countryId AND UPPER(ct.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<City> getListByCountryAndName(@Param("countryId") Long countryId, @Param("name") String name, Pageable pageable);

	@Query("SELECT count(ct) FROM City ct JOIN ct.country c WHERE c.id = :countryId AND UPPER(ct.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByCountryAndName(@Param("countryId") Long countryId, @Param("name") String name);

	@Query("SELECT ct FROM City ct JOIN ct.state st WHERE st.id = :stateId AND UPPER(ct.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<City> getListByStateAndName(@Param("stateId") Long stateId, @Param("name") String name, Pageable pageable);

	@Query("SELECT count(ct) FROM City ct JOIN ct.state st WHERE st.id = :stateId AND UPPER(ct.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByStateAndName(@Param("stateId") Long stateId, @Param("name") String name);

}
