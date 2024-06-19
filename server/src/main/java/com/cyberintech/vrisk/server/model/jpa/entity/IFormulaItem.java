package com.cyberintech.vrisk.server.model.jpa.entity;


import com.cyberintech.vrisk.server.model.jpa.domains.VariableOperation;

/**
 * Formula Item Entity Interface
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-03-02
 */
public interface IFormulaItem {

	Long getId();

	String getName();

	Long getOrdinal();

	VariableTypes getVariableType();

	Double getValue();

	Boolean getIsOperation();

	VariableOperation getOperation();

}
