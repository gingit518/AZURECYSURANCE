package com.cyberintech.vrisk.server.integration.bigid.batch.helper;

import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.entity.BigIdDSSystemTypeMappings;
import com.cyberintech.vrisk.server.repository.jpa.BigIdDatasourceTypeSystemTypeMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class BigIdDatasourceTypeHelper {
	private final BigIdDatasourceTypeSystemTypeMappingRepository mappingRepository;

	public SystemType asSystemType(String dsType, SystemType defaultValue) {
		Optional<BigIdDSSystemTypeMappings> maybeMapping = mappingRepository.findByBigIdDatasourceType(dsType);
		if (maybeMapping.isEmpty()) {
			log.warn("Can not find appropriate '{}' datasource type to system type mapping.", dsType);
			return defaultValue;
		}
		return maybeMapping.get().getSystemType();
	}
}
