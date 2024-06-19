package com.cyberintech.vrisk.server.model.dto.qualitative_question;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Qualitative Question View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-06
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "question"})
@EqualsAndHashCode(of = {"id", "question"}, callSuper = false)
public class QualitativeQuestionRefDTO extends DTOBase<QualitativeQuestions> {

	@Schema
	private Long id;

	@Schema
	private String code;

	@Schema
	private String question;

	@Schema
	private String description;

	@Schema
	private VendorType vendorType;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QualitativeQuestionRefDTO(QualitativeQuestions entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QualitativeQuestions entity) {
//		super.fromEntity(entity);
		this.id = entity.getId();
		this.code = entity.getCode();
		this.question = entity.getQuestion();
		this.description = entity.getDescription();
		this.vendorType = entity.getVendorType();
	}
}
