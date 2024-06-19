package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class SystemDataSeries implements Cloneable {

	private Systems system;
	private List<Double> items;

	public SystemDataSeries() {
		items = new ArrayList<>();
	}

	public List<String> getValues() {
		return items.stream().map(aDouble -> aDouble != null ? String.format("%,.2f", aDouble) : "").collect(Collectors.toList());
	}

	public SystemDataSeries clone() {
		SystemDataSeries result = new SystemDataSeries();
		result.setSystem(this.getSystem());
		result.setItems(this.getItems().stream().map(source -> new Double(source)).collect(Collectors.toList()));

		return result;
	}
}
