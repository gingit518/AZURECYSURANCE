package com.cyberintech.vrisk.server.model.jpa.domains;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class VendorTypeConverter implements AttributeConverter<VendorType, String> {
	@Override
	public String convertToDatabaseColumn(VendorType vendorType) {
		return vendorType.name();
	}

	@Override
	public VendorType convertToEntityAttribute(String vendorTypeName) {
		return VendorType.valueOf(vendorTypeName);
	}
}
