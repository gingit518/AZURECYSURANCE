package com.cyberintech.vrisk.server.model.jpa.entity;


import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "bigid_dstype_system_type_mappings")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "bigIdDatasourceType", "systemType"})
@EqualsAndHashCode(of = {"id"})
public class BigIdDSSystemTypeMappings {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@NotEmpty
	@NotNull
	@Column(name = "bigid_datasource_type", nullable = false)
	private String bigIdDatasourceType;

	@NotEmpty
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "system_type")
	private SystemType systemType;

}
