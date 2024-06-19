package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

/**
 * Predefined Technology Category Domains
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since	 2020-05-21
 */
@Getter
@ToString(of = {"id", "name"})
public enum TechnologyCategoryDomain {

	/**
	 * A server is a computer program or device that provides a service to another computer program and its user, also known as the client.
	 * In a data center, the physical computer that a server program runs on is also frequently referred to as a server.
	 * That machine may be a dedicated server or it may be used for other purposes as well.
	 */
	HARDWARE_SERVER(101L, "Hardware - Server"),

	/**
	 * SAP
	 */
	SOFTWARE_SAP(102L, "Software - SAP"),

	/**
	 * Middleware web services consists of auxiliary products that work at the margins of a primary Web services application or facilitate the functionality between an
	 * application and an operating system. Its also known as Web services management.
	 */
	MIDDLEWARE_WEB_SERVICE(103L, "Middleware Web Service"),

	/**
	 * Appliance computing is a type of computing platform that provides the entire client workstation with software resources over the Internet.
	 */
	APPLIANCE(104L, "Appliance"),

	/**
	 * Network
	 */
	NETWORK(105L, "Network"),

	/**
	 * Database software is primarily used to store and manage data/databases, typically in a structured format.
	 * It generally provides a graphical interface that allows users to create, edit and manage data fields and records in a tabular or organized form.
	 * The data/database stored using this software can be retrieved in a raw or report based format.
	 */
	DATABASE(106L, "Database"),

	/**
	 * Mobile
	 */
	MOBILE(107L, "Mobile"),

	/**
	 * Cloud computing makes computer system resources, storage and computing power, available on demand without direct active management by the user.
	 */
	CLOUD_SERVICE(108L, "Cloud Service"),

	/**
	 * A human resources software will integrate recruiting, onboarding, workforce management, time scheduling and management, payroll solutions and strategic human capital management.
	 */
	SOFTWARE_HR(109L, "Software - HR"),

	/**
	 * An endpoint is a remote computing device that communicates back and forth with a network to which is it connected.
	 */
	ENDPOINT(110L, "Endpoint"),

	/**
	 * The internet of things, or IoT, is a system of interrelated computing devices, mechanical and digital machines, objects, animals or people that are provided with unique identifiers (UIDs)
	 * and the ability to transfer data over a network without requiring human-to-human or human-to-computer interaction.
	 */
	IOT(111L, "Internet of things");

	private final Long id;

	private final String name;

	private TechnologyCategoryDomain(Long domainId, String domainName) {
		this.id = domainId;
		this.name = domainName;
	}

	/**
	 *
	 */
	public static TechnologyCategoryDomain of(Long id) {
		TechnologyCategoryDomain result = null;

		if(id != null) {
			switch(id.intValue()) {
				case 101:
					return HARDWARE_SERVER;
				case 102:
					return SOFTWARE_SAP;
				case 103:
					return MIDDLEWARE_WEB_SERVICE;
				case 104:
					return APPLIANCE;
				case 105:
					return NETWORK;
				case 106:
					return DATABASE;
				case 107:
					return MOBILE;
				case 108:
					return CLOUD_SERVICE;
				case 109:
					return SOFTWARE_HR;
				case 110:
					return ENDPOINT;
				case 111:
					return IOT;
			}
		}

		return result;
	}
}
