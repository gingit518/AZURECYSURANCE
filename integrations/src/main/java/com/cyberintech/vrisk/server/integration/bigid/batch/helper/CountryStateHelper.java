package com.cyberintech.vrisk.server.integration.bigid.batch.helper;

import com.cyberintech.vrisk.server.model.jpa.entity.Country;
import com.cyberintech.vrisk.server.model.jpa.entity.State;
import com.cyberintech.vrisk.server.repository.jpa.CountryRepository;
import com.cyberintech.vrisk.server.repository.jpa.StateRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

@Component
@Slf4j
@AllArgsConstructor
public class CountryStateHelper {

	private final CountryRepository countryRepository;
	private final StateRepository stateRepository;

	public void setCountryStatePair(String stateOrCountry, Consumer<State> stateAccessor, Consumer<Country> countryAccessor) {
		log.info("Processing state or country string: {}", stateOrCountry);
		if (StringUtils.isBlank(stateOrCountry)) {
			stateAccessor.accept(null);
			countryAccessor.accept(null);
			log.warn("State or country string is empty. Reset.");
			return;
		}

		Optional<State> maybeState = stateRepository.findFirstByName(stateOrCountry);
		if (maybeState.isPresent()) {
			log.info("Found state by {} name. Setting.", stateOrCountry);
			Optional.ofNullable(countryAccessor).ifPresent(accessor -> accessor.accept(maybeState.get().getCountry()));
			Optional.ofNullable(stateAccessor).ifPresent(accessor -> accessor.accept(maybeState.get()));
			return;
		}

		Optional<Country> maybeCountry = countryRepository.findFirstByName(stateOrCountry);
		maybeCountry.ifPresent(country -> {
			log.info("Found country by {} name. Setting.", stateOrCountry);
			Optional.ofNullable(countryAccessor).ifPresent(accessor -> accessor.accept(country));
		});
		log.info("Finished to process state or country string.");
	}
}
