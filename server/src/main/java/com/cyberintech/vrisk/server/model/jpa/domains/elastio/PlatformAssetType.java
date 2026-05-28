package com.cyberintech.vrisk.server.model.jpa.domains.elastio;

import lombok.Getter;

/**
 * Platform types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2026-03-01
 */
public enum PlatformAssetType {
	AMAZON_S3(PlatformType.AWS, "Object Storage", "Amazon S3 (S3 Bucket)")
	, AZURE_BLOB_STORAGE(PlatformType.Azure, "Object Storage", "Azure Blob Storage (Blob Container)")
	, GOOGLE_CLOUD_STORAGE(PlatformType.GCP, "Object Storage", "Google Cloud Storage (GCS Bucket)")
	, AMAZON_EC2(PlatformType.AWS, "Compute", "Amazon EC2 (EC2 Instance)")
	, AZURE_VIRTUAL_MACHINE(PlatformType.Azure, "Compute", "Azure Virtual Machines (VM)")
	, GOOGLE_COMPUTE_ENGINE(PlatformType.GCP, "Compute", "Google Compute Engine (VM Instance)")
	, AMAZON_EBS(PlatformType.AWS, "Block Storage", "Amazon EBS (EBS Volume)")
	, AZURE_MANAGED_DISKS(PlatformType.Azure, "Block Storage", "Azure Managed Disks")
	, GOOGLE_PERSISTENT_DISK(PlatformType.GCP, "Block Storage", "Google Persistent Disk")
	, RDS_DATABASE_ENGINE(PlatformType.AWS, "Managed Relational Database", "Managed Relational Database")
	, AMAZON_AURORA_MYSQL(PlatformType.AWS, "Managed Relational Database", "Amazon Aurora (MySQL-compatible)")
	, AMAZON_AURORA_POSTGRESQL(PlatformType.AWS, "Managed Relational Database", "Amazon Aurora (PostgreSQL-compatible)")
	, RDS_MYSQL(PlatformType.RDS, "Managed Relational Database", "MySQL")
	, RDS_POSTGRESQL(PlatformType.RDS, "Managed Relational Database", "PostgreSQL")
	, RDS_MARIADB(PlatformType.RDS, "Managed Relational Database", "MariaDB")
	, RDS_ORACLE_DB_SE1(PlatformType.RDS, "Managed Relational Database", "Oracle Database SE1")
	, RDS_ORACLE_DB_SE2(PlatformType.RDS, "Managed Relational Database", "Oracle Database SE2")
	, RDS_ORACLE_DB_ENTERPRISE(PlatformType.RDS, "Managed Relational Database", "Oracle Database Enterprise Edition")
	, RDS_MSSQL_EXPRESS(PlatformType.RDS, "Managed Relational Database", "Microsoft SQL Server Express")
	, RDS_MSSQL_WEB(PlatformType.RDS, "Managed Relational Database", "Microsoft SQL Server Web")
	, RDS_MSSQL_STANDARD(PlatformType.RDS, "Managed Relational Database", "Microsoft SQL Server Standard")
	, RDS_MSSQL_ENTERPRISE(PlatformType.RDS, "Managed Relational Database", "Microsoft SQL Server Enterprise")
	, UNDEFINED(null, "Undefined", "Undefined")
	;

	@Getter
	private final PlatformType platformType;

	@Getter
	private final String type;

	@Getter
	private final String label;

	PlatformAssetType(PlatformType platformType, String type, String label) {
		this.platformType = platformType;
		this.type = type;
		this.label = label;
	}
}
