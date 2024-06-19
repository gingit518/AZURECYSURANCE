package com.cyberintech.vrisk.server.integration.bigid.batch.util;

import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeFormatterUtil {
	public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
	public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_FILE_NAME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
	public static final String UTC_TZ = "UTC";
}
