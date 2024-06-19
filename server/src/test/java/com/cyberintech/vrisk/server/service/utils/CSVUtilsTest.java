package com.cyberintech.vrisk.server.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CSVUtilsTest {

	@Test
	public void escapedValue() throws UnsupportedEncodingException, IOException {
		String str = "Hello \"World\"!";

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		CSVPrinter printer = CSVUtils.createCSVPrinter(outputStream, "header");
		printer.printRecord(str);
		printer.flush();

		System.out.println(outputStream.toString(StandardCharsets.UTF_8));

		CSVParser parser = CSVUtils.createCSVParser(new ByteArrayInputStream(outputStream.toByteArray()));
		CSVRecord record = parser.getRecords().iterator().next();
		String value = record.get("header");

		assertEquals(str, value);
	}

	@Test
	public void multilineCSV() throws UnsupportedEncodingException, IOException {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("Technologies.csv");
		assertNotNull(inputStream);

		CSVParser parser = CSVUtils.createCSVParser(inputStream);
		CSVRecord record = parser.getRecords().iterator().next();
		String value = record.get("Technology Name");

		assertEquals("Microsoft SQL Server1", value);
	}

}
