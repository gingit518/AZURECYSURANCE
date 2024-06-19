package com.cyberintech.vrisk.server.service.dashboards;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormulaResult implements Cloneable {

	private String formula;
	private Double result;

	public FormulaResult() {
		result = 0D;
	}

	public FormulaResult(String formula, Double result) {
		this.formula = formula;
		this.result = result;
	}
}
