package com.cyberintech.vrisk.server.repository.results;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentWeights;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlSubcategories;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlTests;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metric Result Answers Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-18
 */
@Getter
@Setter
@NoArgsConstructor
@Data
public class ControlTestResult {

	private ControlTests controlTest;

	private ControlSubcategories controlSubcategory;

	private AssessmentWeights assessmentWeight;

	public ControlTestResult(ControlTests controlTest, ControlSubcategories controlSubcategory, AssessmentWeights assessmentWeight) {
		this.controlTest = controlTest;
		this.controlSubcategory = controlSubcategory;
		this.assessmentWeight = assessmentWeight;
	}
}
