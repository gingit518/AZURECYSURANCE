package com.cyberintech.vrisk.server.model.dto.qualitative_question;

import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleItemDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

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
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class GDPRQualitativeQuestionViewDTO extends QualitativeQuestionWithAnswersViewDTO {

	@Schema
	private GDPRArticleItemDTO article;

	@Schema
	private DocumentDTO document;

	@Schema
	private String answerText;

	@Schema
	private List<AnswerViewDTO> selectedAnswers;

	@Schema
	private List<QuestionBranchingLogicViewDTO> branchingLogic;

	@Schema
	private Boolean useColorCoding;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRQualitativeQuestionViewDTO(QualitativeQuestions entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QualitativeQuestions entity) {
		super.fromEntity(entity);
	}

	public GDPRQualitativeQuestionViewDTO applyArticle(GDPRArticleItemDTO article) {
		this.setArticle(article);

		return this;
	}

	public static GDPRQualitativeQuestionViewDTO of(QualitativeQuestionWithAnswersViewDTO entity) {

		GDPRQualitativeQuestionViewDTO result = new GDPRQualitativeQuestionViewDTO();

		result.setId(entity.getId());
		result.setCode(entity.getCode());
		result.setQuestion(entity.getQuestion());
		result.setDescription(entity.getDescription());
		result.setVendorType(entity.getVendorType());
		// result.setOrdinal(entity.getOrdinal());
		result.setAllVendorsSelected(entity.getAllVendorsSelected());
		result.setAllowUploadAsAnswer(entity.getAllowUploadAsAnswer());
		result.setAllowTextAsAnswer(entity.getAllowTextAsAnswer());
		result.setIsTechnologyVendor(entity.getIsTechnologyVendor());
		result.setIsSystemVendor(entity.getIsSystemVendor());
		result.setIsServiceVendor(entity.getIsServiceVendor());
		result.setUseColorCoding(entity.getUseColorCoding());
//		result.setIsInternal(entity.getIsInternal());

		/*
		if (entity.getQualitativeMetric() != null) {
			result.setQualitativeMetric(entity.getQualitativeMetric());
		}
		*/
		result.setAnswers(entity.getAnswers());

		result.setSelectedAnswers(entity.getSelectedAnswers());
		result.setAnswerText(entity.getAnswerText());
		result.setAnswerComment(entity.getAnswerComment());
		result.setDocument(entity.getDocument());
		result.setBranchingLogic(entity.getBranchingLogic());

		return result;
	}
}
