package com.cyberintech.vrisk.server.model.dto.formulas;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableOperation;
import com.cyberintech.vrisk.server.model.jpa.entity.FormulaItems;
import com.cyberintech.vrisk.server.model.jpa.entity.VariableTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Formula Item View Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-12
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name", "ordinal"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class FormulaItemViewDTO extends DTOBase<FormulaItems> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long ordinal;

	@Schema
	private ItemViewDTO<VariableTypes> variableType;

	@Schema
	private Double value;

	@Schema
	private Boolean isOperation;

	@Schema
	private VariableOperation operation;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public FormulaItemViewDTO(FormulaItems entity) {
		super(entity);
	}

	@Override
	public void fromEntity(FormulaItems entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		ordinal = entity.getOrdinal();
		value = entity.getValue();
		isOperation = entity.getIsOperation();
		operation = entity.getOperation();

		if (entity.getVariableType() != null) {
			variableType = new ItemViewDTO<>(entity.getVariableType());
		}
	}

	@Override
	public FormulaItems toEntity(FormulaItems origEntity) {
		FormulaItems result = origEntity != null ? origEntity : new FormulaItems();
		result.setName(this.getName());
		result.setDescription(this.getDescription());
		result.setOrdinal(this.getOrdinal());
		result.setValue(this.getValue());
		result.setIsOperation(this.getIsOperation());
		result.setOperation(this.getOperation());

		if(this.getVariableType() != null) {
			result.setVariableTypeId(this.getVariableType().getId());
		}

		return result;
	}
}
