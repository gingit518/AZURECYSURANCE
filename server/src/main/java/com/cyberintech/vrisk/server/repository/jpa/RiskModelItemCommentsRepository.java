package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.RiskModelItemComments;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskModelItemCommentsRepository extends CoreRepository<RiskModelItemComments, Long> {

	Optional<RiskModelItemComments> findById(Long id);

	List<RiskModelItemComments> findAllByRiskModelIdAndItemTypeName(Long riskModelId, String itemTypeName);

	List<RiskModelItemComments> findAllByRiskModelIdAndItemTypeNameAndExternalId(Long riskModelId, String itemTypeName, Long externalId);

	List<RiskModelItemComments> findAllByRiskModelIdAndItemTypeNameAndExternalUid(Long riskModelId, String itemTypeName, String externalUid);

	Optional<RiskModelItemComments> findFirstByRiskModelIdAndItemTypeNameAndExternalId(Long riskModelId, String itemTypeName, Long externalId);

}
