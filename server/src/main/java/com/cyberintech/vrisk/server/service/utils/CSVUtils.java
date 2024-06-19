package com.cyberintech.vrisk.server.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.nio.charset.Charset;

/**
 * CSV common Utils
 *
 * @author Eugene A. Kalosha <ekalosha@risk-q.com>
 * @version 0.1.1
 * @since 2022-05-23
 */
@Slf4j
public class CSVUtils {

	public static final String LIST_SEPARATOR = ";";
	public static final String PATH_SEPARATOR = "/";
	public static final String FIELD_SEPARATOR = ":";

	/**
	 * Create builder for {@linkCSVFormat}.
	 *
	 * @return {@linkplain CSVFormat.Builder}
	 */
	public static CSVFormat.Builder createCSVFormatBuilder() {
		return CSVFormat.Builder.create().setQuote('"');
	}

	/**
	 * Create builder with header for {@linkCSVFormat}.
	 *
	 * @param header the header, {@code null} if disabled, empty if parsed
	 *               automatically, user specified otherwise.
	 *
	 * @return {@linkplain CSVFormat.Builder}
	 */
	public static CSVFormat.Builder createCSVFormatBuilder(String... header) {
		return createCSVFormatBuilder().setHeader(header);
	}

	/**
	 * Create CSV parser from file content Stream
	 *
	 * @param fileContentStream
	 * @return
	 * @throws IOException
	 */
	public static CSVParser createCSVParser(InputStream fileContentStream) throws IOException {
		Reader reader = createCSVReader(fileContentStream);
		CSVFormat csvFormat = createCSVFormatBuilder().setHeader()
			.setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build();
		CSVParser csvParser = csvFormat.parse(reader);

		return csvParser;
	}

	/**
	 * Create a printer for CSV files.
	 *
	 * @param outputStream stream to which to print. Must not be null.
	 * @param header       the header, {@code null} if disabled, empty if parsed
	 *                     automatically, user specified otherwise.
	 * @return a printer that will print values to the given stream.
	 * @throws IOException
	 */
	public static CSVPrinter createCSVPrinter(OutputStream outputStream, String... header) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVUtils.createCSVFormatBuilder(header).build();
		return new CSVPrinter(writer, csvFormat);
	}

	/**
	 * Create CSV reader from file content Stream
	 *
	 * @param fileContentStream
	 * @return
	 * @throws IOException
	 */
	public static Reader createCSVReader(InputStream fileContentStream) throws IOException {

		Reader reader = null;

		try {
			fileContentStream.reset();
		} catch (IOException e) {
			log.debug("Uploaded file stream already reset");
		}

		BOMInputStream bOMInputStream = new BOMInputStream(fileContentStream);
		ByteOrderMark bom = bOMInputStream.getBOM();
		String charsetName = bom == null ? Charset.defaultCharset().name() : bom.getCharsetName();

		// Parse CSV file
		reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), Charset.forName(charsetName));

		return reader;
	}

	/**
	 * Get values of {@link CSVRecord} column as a {@code Double}.
	 *
	 * @param csvRecord CSV record.
	 * @param name      column name.
	 * @return The value from the column {@code name}. If column is not mapped, then
	 *         {@code null}
	 */
	public static Double getAsDouble(CSVRecord csvRecord, String name) {
		Double result = null;
		if (csvRecord.isMapped(name)) {
			result = NumberUtils.toDouble(csvRecord.get(name));
		}
		return result;
	}

	/**
	 * Get values of {@link CSVRecord} column as a {@code String}.
	 *
	 * @param csvRecord CSV record.
	 * @param name      column name.
	 * @return The value from the column {@code name}. If column is not mapped, then
	 *         empty string.
	 */
	public static String getAsString(CSVRecord csvRecord, String name) {
		String result = null;
		if (csvRecord.isMapped(name)) {
			result = csvRecord.get(name);
		}
		if (result == null) {
			return "";
		}
		return result.trim();
	}

	/**
	 * Get values of {@link CSVRecord} column as array of {@code String}.
	 *
	 * @param csvRecord CSV record.
	 * @param name      column name.
	 * @return The trimmed values from the column {@code name}. If column is not mapped,
	 *         then empty array.
	 */
	public static String[] getAsStrings(CSVRecord csvRecord, String name) {
		String value = null;
		if (csvRecord.isMapped(name)) {
			value = csvRecord.get(name);
		}
		if (value == null) {
			return new String[0];
		}
		String[] result = StringUtils.split(value, CSVUtils.LIST_SEPARATOR);
		for (int i = result.length - 1; i >= 0; i--) {
			result[i] = StringUtils.trim(result[i]);
		}
		return result;
	}
}
