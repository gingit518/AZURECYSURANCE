package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.PolicyReviews;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyReviewsRepository extends CoreRepository<PolicyReviews, Long> {

	@NotNull
	Optional<PolicyReviews> findById(Long id);

	List<PolicyReviews> findAllByPolicyId(Long policyId);

}
