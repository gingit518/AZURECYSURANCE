package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Risk Model Item Comments Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-08-16
 */
@Entity
@Table(name = "risk_model_item_comments")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
public class RiskModelItemComments implements IEntityWithDates {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "item_type_name")
	private String itemTypeName;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@Column(name = "external_id")
	private Long externalId;

	@Column(name = "external_uid")
	private String externalUid;

	@Column(name = "comment")
	private String comment;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

}
