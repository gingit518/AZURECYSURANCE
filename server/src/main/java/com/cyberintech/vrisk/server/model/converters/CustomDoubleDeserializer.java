package com.cyberintech.vrisk.server.model.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Deserializer for Double values, which may contain "," inside
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-13
 */
public class CustomDoubleDeserializer extends StdDeserializer<Double> {

	/**
	 * Default constructor
	 */
	public CustomDoubleDeserializer() {
		super(Double.class);
	}

	/**
	 * Deserialize Double value using simple custom logic
	 *
	 * @param jsonParser
	 * @param deserializationContext
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	@Override
	public Double deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);
		String doubleString = node.asText();

		Double result = null;
		try {
			result = Double.parseDouble(doubleString);
		} catch (NumberFormatException exception) {
			result = Double.parseDouble(doubleString.replaceAll("\\,", ""));
		}

		return result;
	}
}
