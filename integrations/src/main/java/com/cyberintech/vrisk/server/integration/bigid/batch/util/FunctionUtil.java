package com.cyberintech.vrisk.server.integration.bigid.batch.util;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class FunctionUtil {
	public static <T> T getOr(Supplier<T> supplier, T defValue) {
		try {
			return supplier.get();
		} catch (Exception ex) {
			return defValue;
		}
	}
}
