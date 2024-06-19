package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Industries;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IndustryRepository extends CoreRepository<Industries, Long> {

	Optional<Industries> findById(Long id);

	Optional<Industries> findFirstByNameAndIdNotIn(String name, List<Long> id);

	List<Industries> findAllByParent(Industries parent);

	@Query("SELECT l FROM Industries l LEFT JOIN l.parent parent WHERE UPPER(l.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	List<Industries> getListByName(@Param("name") String name, Pageable pageable);

	Optional<Industries> findFirstByName(String name);

	Optional<Industries> findFirstByNameIgnoreCase(String name);

	@Query("SELECT count(l) FROM Industries l WHERE UPPER(l.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	@Query("SELECT distinct ind FROM AssociateVendors av JOIN av.vendor v JOIN av.systems s JOIN v.industry ind WHERE s.id = :systemId")
	Set<Industries> getVendorIndustriesForSystem(@Param("systemId") Long systemId);

	@Query("SELECT new org.apache.commons.lang3.tuple.ImmutablePair(s, ind) FROM AssociateVendors av JOIN av.vendor v JOIN av.systems s JOIN v.industry ind WHERE s.organizationId = :organizationId")
	Set<ImmutablePair<Systems, Industries>> getVendorIndustriesForSystemByOrganization(@Param("organizationId") Long organizationId);

}
