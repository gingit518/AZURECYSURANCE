package com.cyberintech.vrisk.server.integration.bigid.client.util;

import com.cyberintech.vrisk.server.integration.bigid.client.exception.RemoteClientException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

@UtilityClass
@Slf4j
public class RemoteClientWrapperUtil {

	public static <T> T wrap(Supplier<T> func, String operation) {
		try {
			LocalDateTime startAt = LocalDateTime.now();
			T result = func.get();
			LocalDateTime finishedAt = LocalDateTime.now();
			log.info("Executed '{}' operation. Total time elapsed: {} ms. Result = {}",
				operation, ChronoUnit.MILLIS.between(startAt, finishedAt), result);
			return result;
		} catch (Exception ex) {
			log.warn("Got exception processing '{}' operation. Ex = {}.", operation, ex.getMessage(), ex);
			throw new RemoteClientException(ex.getMessage());
		}
	}

}
