package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnitLevels;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessUnitLevelsRepository extends CoreRepository<BusinessUnitLevels, Long> {

	Optional<BusinessUnitLevels> findById(Long itemId);

	List<BusinessUnitLevels> findAllByParentId(Long parentId);

	// @Query("SELECT bm FROM BusinessUnitLevels bm WHERE bm.parentId = :parentId AND bm.childId = :childId")
	List<BusinessUnitLevels> findAllByParentIdAndChildIdNot(@Param("parentId") Long parentId, @Param("childId") Long childId);

	List<BusinessUnitLevels> findAllByChildId(Long childId);
}
