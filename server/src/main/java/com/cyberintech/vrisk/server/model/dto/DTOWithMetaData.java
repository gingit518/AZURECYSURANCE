package com.cyberintech.vrisk.server.model.dto;

import com.cyberintech.vrisk.server.model.jpa.entity.IEntityWithDates;
import com.cyberintech.vrisk.server.model.jpa.entity.IEntityWithMetadata;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Risk Model Domain View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
public abstract class DTOWithMetaData<ENTITY> extends DTOBase<ENTITY> {

	private static final long serialVersionUID = 5885051703261357062L;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class ItemMetadata implements Serializable {

		private static final long serialVersionUID = 4599883130229460256L;

		@Schema
		private Date createdAt;

		@Schema
		private Date updatedAt;

		@Schema
		private IDTitle createdBy;

		@Schema
		private IDTitle updatedBy;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IDTitle implements Serializable {

		private static final long serialVersionUID = -4108576503753778702L;

		@Schema
		private Long id;

		@Schema
		private String title;
	}

	@Getter
	@Setter
	@Schema
	private ItemMetadata metadata;

	/**
	 * Default constructor
	 */
	public DTOWithMetaData() {
		super();
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public DTOWithMetaData(ENTITY entity) {
		super(entity);

		// Loading items metadata
		loadMetadata(entity);
	}

	/**
	 * Load default metadata for the item
	 *
	 * @param entity
	 */
	protected void loadMetadata(ENTITY entity) {
		// Set Metadata
		metadata = new ItemMetadata();
		if (entity instanceof IEntityWithDates) {
			metadata.setCreatedAt(((IEntityWithDates) entity).getCreatedAt());
			metadata.setUpdatedAt(((IEntityWithDates) entity).getUpdatedAt());
		}

		if (entity instanceof IEntityWithMetadata) {
			Users createdBy = ((IEntityWithMetadata) entity).getCreatedBy();
			if (createdBy != null) {
				metadata.setCreatedBy(new IDTitle(createdBy.getId(), createdBy.getFullName()));
			}

			Users updatedBy = ((IEntityWithMetadata) entity).getUpdatedBy();
			if (updatedBy != null) {
				metadata.setUpdatedBy(new IDTitle(updatedBy.getId(), updatedBy.getFullName()));
			}
		}
	}

	@Override
	public void fromEntity(ENTITY entity) {
		super.fromEntity(entity);
	}

	@JsonIgnore
	public void setMetadataFromEntity(ENTITY entity) {
		metadata = new ItemMetadata();
		if (entity instanceof IEntityWithDates) {
			metadata.setCreatedAt(((IEntityWithDates) entity).getCreatedAt());
			metadata.setUpdatedAt(((IEntityWithDates) entity).getUpdatedAt());
		}

		if (entity instanceof IEntityWithMetadata) {
			Users createdBy = ((IEntityWithMetadata) entity).getCreatedBy();
			if (createdBy != null) {
				metadata.setCreatedBy(new IDTitle(createdBy.getId(), createdBy.getFullName()));
			}

			Users updatedBy = ((IEntityWithMetadata) entity).getUpdatedBy();
			if (updatedBy != null) {
				metadata.setUpdatedBy(new IDTitle(updatedBy.getId(), updatedBy.getFullName()));
			}
		}
	}
}
