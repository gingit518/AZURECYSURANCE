package com.cyberintech.vrisk.server.model.dto.formulas;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.FormulaItems;
import com.cyberintech.vrisk.server.model.jpa.entity.Formulas;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Formula View Entity Definition
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
public class FormulaViewDTO extends DTOBase<Formulas> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private String formula;

	@Schema
	private Long organizationId;

	@Schema
	private List<FormulaItemViewDTO> formulaItems;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public FormulaViewDTO(Formulas entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Formulas entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		formula = entity.getFormula();
		organizationId = entity.getOrganizationId();

		formulaItems = Optional.ofNullable(entity.getFormulaItems()).orElse(new HashSet<>()).stream().map(FormulaItemViewDTO::new).collect(Collectors.toList());
	}
}
