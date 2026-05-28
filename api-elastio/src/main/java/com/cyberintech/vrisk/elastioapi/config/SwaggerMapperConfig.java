package com.cyberintech.vrisk.elastioapi.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;

import java.io.IOException;

// public class SwaggerMapperConfig implements ApplicationListener<ObjectMapperConfigured> {
public class SwaggerMapperConfig /*implements ApplicationListener<ObjectMapperConfigured>*/ {

	/*
    @Override
    public void onApplicationEvent(ObjectMapperConfigured event) {
        event.getObjectMapper()
                .registerModule(customDeserializerModule());
    }
	*/

    private SimpleModule customDeserializerModule() {
        return new SimpleModule("SwaggerCustomModule")
                .addSerializer(DateTime.class, new DateTimeSerializer())
                .addSerializer(Enum.class, new EnumSerializer())
                .addKeySerializer(Enum.class, new EnumKeySerializer())
                .setDeserializerModifier(new EnumDeserializerModifier());
    }

    public static class EnumKeySerializer extends JsonSerializer<Enum> {
        @Override
        public void serialize(Enum value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeFieldName(value.name().toLowerCase());
        }
    }

    private static class DateTimeSerializer extends JsonSerializer<DateTime> {
        @Override
        public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            if (value != null) {
                jgen.writeString(value.toString());
            } else {
                jgen.writeNull();
            }
        }
    }

    private static class EnumSerializer extends JsonSerializer<Enum> {
        @Override
        public void serialize(Enum value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            if (value != null) {
                jgen.writeString(value.name().toLowerCase());
            } else {
                jgen.writeNull();
            }
        }
    }

    private static class EnumDeserializerModifier extends BeanDeserializerModifier {
        @Override
        public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config, final JavaType type, BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
            return new EnumDeserializer(type);
        }
    }

    private static class EnumDeserializer extends JsonDeserializer<Enum> {

        private JavaType type;

        protected EnumDeserializer(JavaType type) {
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Enum deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
            Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
            return findEnum(rawClass.getEnumConstants(), jp.getValueAsString());
        }

        private Enum findEnum(Enum[] enumConstants, String key) {
            for (Enum entry : enumConstants) {
                if (entry.name().equalsIgnoreCase(key)) {
                    return entry;
                }
            }
            return null;
        }
    }

}
