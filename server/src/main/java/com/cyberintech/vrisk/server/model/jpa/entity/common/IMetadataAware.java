package com.cyberintech.vrisk.server.model.jpa.entity.common;

import java.util.Set;

public interface IMetadataAware<M extends IMetadata> {
	Set<M> getMetadata();

	void setMetadata(Set<M> metadataSet);
}
