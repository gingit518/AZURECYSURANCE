package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.PackagePlans;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends CoreRepository<Organizations, Long> {

	Optional<Organizations> findById(Long id);

	Optional<Organizations> findByIdAndPackagePlan(Long id, PackagePlans packagePlan);

	Optional<Organizations> findByUidAndPackagePlan(String uid, PackagePlans packagePlan);

	Optional<Organizations> findFirstByNameAndIdIsNotIn(String name, Collection<Long> excludeIds);

	Optional<Organizations> findFirstByNameAndOrganizationTypeAndIdIsNotIn(String name, OrganizationType organizationType, Collection<Long> excludeIds);

	Optional<Organizations> findFirstByNameAndOrganizationTypeAndPackagePlan(String name, OrganizationType organizationType, PackagePlans packagePlan);

	Optional<Organizations> findFirstByNameAndOrganizationTypeAndRootParent(String name, OrganizationType organizationType, Organizations root);

	Optional<Organizations> findFirstByNameAndRootParentAndIdIsNotIn(String name, Organizations root, Collection<Long> excludeIds);

	List<Organizations> findAllByOrganizationTypeAndNameIsStartingWith(OrganizationType organizationType, String name, Pageable pageable);

	Long countAllByOrganizationTypeAndNameIsStartingWith(OrganizationType organizationType, String name);

	List<Organizations> findAllByNameIsStartingWith(String name, Pageable pageable);

	List<Organizations> findAllByOrganizationTypeAndRootParentIdOrderByName(OrganizationType organizationType, Long rootParentId);

	Long countAllByNameIsStartingWith(String name);

	@Query("SELECT o FROM Organizations o LEFT JOIN FETCH o.country ct " +
		"LEFT JOIN FETCH o.state st LEFT JOIN FETCH o.city ci " +
		"LEFT JOIN FETCH o.currency cu LEFT JOIN FETCH o.language ln LEFT JOIN FETCH o.status sa " +
		"WHERE UPPER(o.name) LIKE (CONCAT(UPPER(:name), '%')) AND o.id NOT IN :excludeIds")
	List<Organizations> filterOrganizations(@Param("name") String name, @Param("excludeIds") Collection<Long> excludeIds, Pageable pageable);

	@Query("SELECT o FROM Organizations o LEFT JOIN FETCH o.country ct " +
		"LEFT JOIN FETCH o.state st LEFT JOIN FETCH o.city ci " +
		"LEFT JOIN FETCH o.currency cu LEFT JOIN FETCH o.language ln LEFT JOIN FETCH o.status sa " +
		"WHERE o.organizationType = :organizationType AND UPPER(o.name) LIKE (CONCAT(UPPER(:name), '%')) AND o.id NOT IN :excludeIds")
	List<Organizations> filterOrganizationsByType(@Param("organizationType") OrganizationType organizationType, @Param("name") String name, @Param("excludeIds") Collection<Long> excludeIds, Pageable pageable);

	@Query("SELECT COUNT(o) FROM Organizations o " +
		"WHERE UPPER(o.name) LIKE (CONCAT(UPPER(:name), '%')) AND o.id NOT IN :excludeIds")
	Long getOrganizationsCount(@Param("name") String name, @Param("excludeIds") Collection<Long> excludeIds);

	@Query("SELECT COUNT(o) FROM Organizations o " +
		"WHERE o.organizationType = :organizationType AND UPPER(o.name) LIKE (CONCAT(UPPER(:name), '%')) AND o.id NOT IN :excludeIds")
	Long getOrganizationsCountByType(@Param("organizationType") OrganizationType organizationType, @Param("name") String name, @Param("excludeIds") Collection<Long> excludeIds);

	@Query("SELECT o FROM Organizations o WHERE o.id = :organizationId AND o.rootParent.id = :rootParentId AND o.organizationType = :organizationType")
	Organizations getSubsidiaryForRootOrganization(@Param("organizationId") Long organizationId, @Param("rootParentId") Long rootParentId, @Param("organizationType") OrganizationType organizationType);

	@Query("SELECT DISTINCT o FROM Organizations o WHERE o.rootParent.id = :rootParentId AND o.organizationType = :organizationType ORDER BY o.name")
	List<Organizations> getListForRootOrganization(@Param("rootParentId") Long rootParentId, @Param("organizationType") OrganizationType organizationType);

	@Query("SELECT DISTINCT o FROM Organizations o WHERE o.rootParent.id = :rootParentId AND o.organizationType = :organizationType AND o.isCloudVendor = true")
	List<Organizations> getCloudListForRootOrganization(@Param("rootParentId") Long rootParentId, @Param("organizationType") OrganizationType organizationType);

	@Query("SELECT DISTINCT o FROM Organizations o WHERE o.rootParent.id = :rootParentId AND o.name=:name AND o.parent.id=:parentId")
	Optional<Organizations> getByParentNameForRootOrganization(@Param("name") String name, @Param("parentId") Long parentId, @Param("rootParentId") Long rootParentId);

	@Query("SELECT DISTINCT o FROM Organizations o WHERE o.rootParent.id = :rootParentId AND o.name=:name AND o.parent.id=:parentId AND o.organizationType=:organizationType")
	Optional<Organizations> getByParentNameForRootOrganizationAndType(
		@Param("name") String name,
		@Param("parentId") Long parentId,
		@Param("rootParentId") Long rootParentId,
		@Param("organizationType") OrganizationType organizationType
	);

	@Query("SELECT DISTINCT o FROM Organizations o WHERE o.rootParent.id = :rootParentId AND o.name=:name AND o.parent.id IS NULL")
	Optional<Organizations> getByNameAndNoParentForRootOrganization(@Param("name") String name, @Param("rootParentId") Long rootParentId);

	@Query("SELECT DISTINCT o FROM Organizations o WHERE o.rootParent.id = :rootParentId AND o.name=:name AND o.parent.id IS NULL AND o.organizationType=:organizationType")
	Optional<Organizations> getByNameAndNoParentForRootOrganizationAndType(@Param("name") String name, @Param("rootParentId") Long rootParentId, @Param("organizationType") OrganizationType organizationType);

	@Query("SELECT DISTINCT v FROM Organizations v JOIN AssociateVendors av ON av.vendor=v JOIN av.systems s JOIN s.dataTypeClassifications dtc " +
		"WHERE av.organizationId = :organizationId AND dtc.id IN :dataTypeIdList AND v.organizationType = :organizationType")
	List<Organizations> getVendorsListWithDataTypes(@Param("organizationId") Long organizationId, @Param("dataTypeIdList") List<Long> dataTypeIdList, @Param("organizationType") OrganizationType organizationType);

	@Query("SELECT DISTINCT v FROM Organizations v " +
		"WHERE  v.rootParent.id = :organizationId AND v.owner.id = :ownerId AND v.organizationType = :organizationType")
	List<Organizations> getAllByOwnerAndOrganization(@Param("ownerId") Long ownerId, @Param("organizationId") Long organizationId, @Param("organizationType") OrganizationType organizationType);

	Optional<Organizations> findByNameAndOrganizationType(String name, OrganizationType organizationType);

	@Query("SELECT DISTINCT o FROM Organizations o WHERE o.rootParent.id = :rootParentId AND o.id=:id AND o.organizationType=:organizationType")
	Optional<Organizations> getByIdAndOrganizationType(@Param("id") Long id, @Param("rootParentId") Long rootParentId, @Param("organizationType") OrganizationType organizationType);

	@Query("SELECT o FROM Organizations o WHERE o.powerbiCapacityExpirationDate < :expirationDate")
	List<Organizations> filterOrganizationsByPowerBIExpirationDate(@Param("expirationDate") Date expirationDate);

	@Query("SELECT o FROM Organizations o WHERE o.powerbiCapacityExpirationDate < :expirationDate AND o.powerbiCapacityStatus in :powerbiCapacityStatus")
	List<Organizations> filterOrganizationsByPowerBIExpirationDateAndStatus(@Param("expirationDate") Date expirationDate, @Param("powerbiCapacityStatus") List<String> powerbiCapacityStatus);

}
