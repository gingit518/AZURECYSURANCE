package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Documents Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-30
 */
@Entity
@Table(name = "documents")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class Documents {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "document_uid")
	private String documentUid;

	@Column(name = "file_type")
	private String fileType;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "remote_path")
	private String remotePath;

	@Column(name = "md5hash")
	private String md5hash;

	@Column(name = "file_size")
	private Double fileSize;

	@Column(name = "item_type")
	private Long itemType;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;



}
