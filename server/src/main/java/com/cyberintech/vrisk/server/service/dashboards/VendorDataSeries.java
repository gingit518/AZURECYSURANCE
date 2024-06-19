package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class VendorDataSeries implements Cloneable {

	private Organizations vendor;
	private List<Double> items;

	public VendorDataSeries() {
		items = new ArrayList<>();
	}

	public List<String> getValues() {
		return items.stream().map(aDouble -> aDouble != null ? String.format("%,.2f", aDouble) : "").collect(Collectors.toList());
	}

	public VendorDataSeries clone() {
		VendorDataSeries result = new VendorDataSeries();
		result.setVendor(this.getVendor());
		result.setItems(this.getItems().stream().map(source -> new Double(source)).collect(Collectors.toList()));

		return result;
	}
}
