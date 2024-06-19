package com.cyberintech.vrisk.server.integration.bigid.client.common.datacatalog.tag;

import lombok.Data;

@Data
public class TagVO {
	private String tagId;
	private String valueId;
	private String tagName;
	private String tagValue;
	private String tagType;
	private TagPropertiesVO properties;
}
