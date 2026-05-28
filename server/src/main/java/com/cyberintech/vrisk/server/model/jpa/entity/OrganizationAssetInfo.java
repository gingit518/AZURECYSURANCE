package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.elastio.PlatformAssetType;
import com.cyberintech.vrisk.server.model.jpa.domains.elastio.PlatformType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;

import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "organization_asset_info")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "uid", "platformType", "assetType", "amountOfDataInTerabytes"})
public class OrganizationAssetInfo {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organizations organization;

	@Column(name = "uid", nullable = true)
	private String uid;

	@Column(name = "platform_type")
	@Enumerated(EnumType.STRING)
	private PlatformType platformType;

	@Column(name = "asset_type")
	@Enumerated(EnumType.STRING)
	private PlatformAssetType assetType;

	@Column(name = "amount_of_data_in_terabytes")
	private Double amountOfDataInTerabytes;

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
		Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
		if (thisEffectiveClass != oEffectiveClass) {
			return false;
		}
		OrganizationAssetInfo that = (OrganizationAssetInfo) o;
		return getId() != null && Objects.equals(getId(), that.getId());
	}

	@Override
	public final int hashCode() {
		return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
	}
}
