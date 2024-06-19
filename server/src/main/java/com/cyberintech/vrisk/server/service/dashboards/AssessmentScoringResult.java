package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentFindings;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemControlTestResults;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AssessmentScoringResult implements Cloneable {

	private String formula;

	private Double result;

	private Double cumulativeWeightedScore;

	private Double cumulativeWeight;

	private List<AssessmentFindings> assessmentFindings;

	private List<Double> weights;

	private List<Double> scores;

	private List<Double> weightedScores;

	public AssessmentScoringResult() {
		result = 0D;
		cumulativeWeightedScore = 0D;
		cumulativeWeight = 0D;
		this.weights = new ArrayList<>();
		this.scores = new ArrayList<>();
		this.weightedScores = new ArrayList<>();
		this.assessmentFindings = new ArrayList<>();
	}

	public AssessmentScoringResult(Double result) {
		this();
		this.result = result;
	}

	/**
	 * Add Score and Weight and calculate score stats
	 *
	 * @param assessmentFinding
	 */
	public void add(AssessmentFindings assessmentFinding) {
		double percentage = assessmentFinding.getPercentage() != null ? assessmentFinding.getPercentage() : 0d;
		double weight = assessmentFinding.getValue() != null ? assessmentFinding.getValue() : 0d;

		add(percentage, weight);
		assessmentFindings.add(assessmentFinding);
	}

	/**
	 * Add Score and Weight and calculate score stats
	 *
	 * @param score
	 * @param weight
	 */
	public void add(double score, double weight) {
		this.weights.add(weight);
		this.scores.add(score);
		this.weightedScores.add(score * weight);

		cumulativeWeightedScore += score * weight;
		cumulativeWeight += weight;
	}

	/**
	 * Calculate Assessment Score
	 *
	 * @return
	 */
	public double calculateScore() {
		double result = 0d;

		if (cumulativeWeight > 0) {
			result = cumulativeWeightedScore / (100 * cumulativeWeight);
		}

		return result;
	}
}
