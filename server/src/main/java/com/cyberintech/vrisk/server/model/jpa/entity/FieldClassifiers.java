package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.entity.common.IMetadataAware;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "field_classifiers")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name", "description"})
@EqualsAndHashCode(of = {"id"})
public class FieldClassifiers implements IMetadataAware<FieldClassifiersMetadata> {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@OneToMany(mappedBy = "fieldClassifier", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<FieldClassifiersMetadata> metadata = new HashSet<>();
}
