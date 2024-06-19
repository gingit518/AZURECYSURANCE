package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.ExternalAnalyticsParameters;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * External Analytics Parameters Repository
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@Repository
public interface ExternalAnalyticsParametersRepository extends CoreRepository<ExternalAnalyticsParameters, Long> {

	Optional<ExternalAnalyticsParameters> findById(Long id);

}
