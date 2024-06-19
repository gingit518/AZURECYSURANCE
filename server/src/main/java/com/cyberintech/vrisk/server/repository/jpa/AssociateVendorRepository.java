package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.AssociateVendors;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import com.cyberintech.vrisk.server.repository.results.AssociateVendorResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssociateVendorRepository extends CoreRepository<AssociateVendors, Long> {

	Optional<AssociateVendors> findById(Long id);

	Optional<AssociateVendors> findByOrganizationIdAndVendor(Long organizationId, Organizations vendor);

	Optional<AssociateVendors> findByOrganizationIdAndVendorId(Long organizationId, Long vendorId);

	@Query("SELECT distinct v FROM AssociateVendors av JOIN av.vendor v JOIN av.systems s WHERE s.id = :systemId AND v.id = :vendorId")
	Optional<Organizations> getSystemVendor(@Param("systemId") Long systemId, @Param("vendorId") Long vendorId);

	@Query("SELECT distinct v FROM AssociateVendors av JOIN av.vendor v JOIN av.systems s WHERE s.id = :systemId")
	List<Organizations> getVendorsListForSystem(@Param("systemId") Long systemId);

	@Query("SELECT distinct v FROM AssociateVendors av JOIN av.vendor v JOIN av.systems s WHERE s.id IN :systemIds")
	List<Organizations> getVendorsListForSystemsList(
		@Param("systemIds") List<Long> systemIds
	);

	@Query("SELECT distinct av FROM AssociateVendors av JOIN av.vendor v JOIN av.systems s WHERE s.id IN :systemIds")
	List<AssociateVendors> getAssociateVendorsListForSystemsList(
		@Param("systemIds") List<Long> systemIds
	);

	@Query("SELECT distinct av FROM AssociateVendors av JOIN av.vendor v JOIN av.systems s JOIN s.owner o WHERE o.id IN :userId")
	List<AssociateVendors> getAssociateVendorsListForSystemOwner(
		@Param("userId") Long userId
	);

	@Query("SELECT distinct s FROM AssociateVendors av JOIN av.vendor v JOIN av.systems s WHERE v.id = :vendorId")
	List<Systems> getSystemsListForVendor(@Param("vendorId") Long vendorId);

	@Query("SELECT distinct s FROM AssociateVendors av JOIN av.vendor v JOIN av.systems s JOIN s.dataAssetClassification dac " +
		"WHERE av.id = :associateVendorId AND dac.id=:dataAssetClassId")
	List<Systems> getSystemsListForAssociateVendorAndDataAssetClass(
		@Param("associateVendorId") Long associateVendorId,
		@Param("dataAssetClassId") Long dataAssetClassId
	);

	@Query("SELECT distinct av FROM AssociateVendors av JOIN av.organization org JOIN av.vendor v " +
		"WHERE av.organizationId = :organizationId AND v.id = :vendorId")
	List<AssociateVendors> getListForOrganizationAndVendor(
		@Param("organizationId") Long organizationId,
		@Param("vendorId") Long vendorId
	);

	@Query("SELECT distinct av FROM AssociateVendors av LEFT JOIN FETCH av.organization org JOIN av.vendor v LEFT JOIN FETCH av.systems s " +
		"WHERE av.organizationId = :organizationId")
	List<AssociateVendors> getListForOrganization(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT distinct av FROM AssociateVendors av LEFT JOIN FETCH av.organization org JOIN av.vendor v LEFT JOIN FETCH av.systems s JOIN s.dataTypeClassifications dtc " +
		"WHERE av.organizationId = :organizationId AND dtc.id IN :dataTypeIdList")
	List<AssociateVendors> getListForOrganizationAndSystemDataTypes(
		@Param("organizationId") Long organizationId,
		@Param("dataTypeIdList") List<Long> dataTypeIdList
	);

	@Query("SELECT av FROM AssociateVendors av LEFT JOIN FETCH av.organization org JOIN av.vendor v LEFT JOIN FETCH av.systems s " +
		"LEFT JOIN FETCH av.createdBy LEFT JOIN FETCH av.updatedBy " +
		"WHERE av.organizationId = :organizationId AND UPPER(v.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<AssociateVendors> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(av) FROM AssociateVendors av JOIN av.vendor v " +
		"WHERE av.organizationId = :organizationId AND UPPER(v.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT new com.cyberintech.vrisk.server.repository.results.AssociateVendorResult(allv, av) " +
		"FROM Organizations allv LEFT OUTER JOIN AssociateVendors av ON av.vendor=allv " +
		"WHERE allv.rootParent.id = :organizationId AND allv.organizationType=:organizationType AND UPPER(allv.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<AssociateVendorResult> getAllVendorsListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("organizationType") OrganizationType organizationType,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(allv) FROM Organizations allv " +
		"WHERE allv.rootParent.id = :organizationId AND allv.organizationType=:organizationType AND UPPER(allv.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getAllVendorsCountByOrganizationAndName(@Param("organizationId") Long organizationId, @Param("organizationType") OrganizationType organizationType, @Param("name") String name);

	void deleteAllByVendor(Organizations organization);
}
