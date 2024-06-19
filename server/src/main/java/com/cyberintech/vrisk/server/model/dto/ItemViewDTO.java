package com.cyberintech.vrisk.server.model.dto;

import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Simple Item View Definition. Contains id/name
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-22
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemViewDTO<ENTITY> extends DTOBase<ENTITY> {

	@Schema
	private Long id;

	@Schema
	private String name;

	/**
	 * Entity DTO constructor
	 */
	public ItemViewDTO(ENTITY entity) {
		super(entity);
	}

	/**
	 * Full arguments constructor
	 */
	public ItemViewDTO(Long id, String name) {
		super();

		this.id = id;
		this.name = name;
	}

	public ItemViewDTO(String name){
		super();
		this.name = name;
	}

}
