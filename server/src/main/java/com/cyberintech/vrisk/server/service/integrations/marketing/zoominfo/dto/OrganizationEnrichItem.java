package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * ZoomInfo Organization search item
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-02-23
 */
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class OrganizationEnrichItem extends DTOBase<Object> {

	public static String[] OUTPUT_COLUMNS = new String[]{
		"id",
		"ticker",
		"name",
		"website",
		"domainList",
		"logo",
		"SocialMediaUrls",
		"revenue",
		"employeeCount",
		"numberOfContactsInZoomInfo",
		"phone",
		"fax",
		"street",
		"city",
		"state",
		"zipCode",
		"country",
		"continent",
		"companyStatus",
		"companyStatusDate",
		"descriptionList",
		"sicCodes",
		"naicsCodes",
		"competitors",
		"ultimateParentId",
		"ultimateParentName",
		"ultimateParentRevenue",
		"ultimateParentEmployees",
		"subUnitCodes",
		"subUnitType",
		"subUnitIndustries",
		"primaryIndustry",
		"industries",
		"parentId",
		"parentName",
		"locationCount",
		"alexaRank",
		"metroArea",
		"lastUpdatedDate",
		"createdDate",
		"certificationDate",
		"certified",
		"hashtags",
		"products",
		"techAttributes",
		"revenueRange",
		"employeeRange",
		"companyFunding",
		"recentFundingAmount",
		"recentFundingDate",
		"totalFundingAmount",
		"employeeGrowth"
	};

	@Schema
	private Long id;

	@Schema
	private String ticker;

	@Schema
	private String name;

	@Schema
	private String website;

	@Schema
	private List<String> domainList;

	@Schema
	private String logo;

	@Schema
	private List<Map<String, String>> socialMediaUrls;

	@Schema
	private Double revenue;

	@Schema
	private Long employeeCount;

	@Schema
	private Long numberOfContactsInZoomInfo;

	@Schema
	private String phone;

	@Schema
	private String fax;

	@Schema
	private String street;

	@Schema
	private String city;

	@Schema
	private String state;

	@Schema
	private String zipCode;

	@Schema
	private String country;

	@Schema
	private String continent;

	@Schema
	private String companyStatus;

	@Schema
	private String companyStatusDate;

	@Schema
	private List<SearchItem> descriptionList;

	@Schema
	private List<SearchItem> sicCodes;

	@Schema
	private List<SearchItem> naicsCodes;

	@Schema
	private List<SearchItem> competitors;


	@Schema
	private Long ultimateParentId;

	@Schema
	private String ultimateParentName;

	@Schema
	private Long ultimateParentRevenue;

	@Schema
	private Long ultimateParentEmployees;

	@Schema
	private List<String> subUnitCodes;

	@Schema
	private String subUnitType;

	@Schema
	private List<String> subUnitIndustries;

	@Schema
	private List<String> primaryIndustry;

	@Schema
	private List<String> industries;

	@Schema
	private String revenueRange;

	@Schema
	private String employeeRange;

	@Schema
	private List<TechnologyEnrichItem> techAttributes;

	/*
	"parentId": 0,
	"parentName": "",
	"locationCount": 0,
	"alexaRank": "0",
	"metroArea": "",
	"lastUpdatedDate": "2021-08-11T20:18:00.000Z",
	"createdDate": "2021-04-10T05:11:00.000Z",
	"certificationDate": "",
	"certified": false,
	"hashtags": [
		{
			"tag": "#b2c",
			"searchString": "b2c",
			"displayLabel": "B2C",
			"description": "This is a company that does business-to-consumer selling",
			"group": "Other",
			"parentCategory": "Other",
			"displayScore": "",
			"scoreUnit": "",
			"hidden": false,
			"label": "B2C",
			"categorizedFlag": false
		}
	],
	"products": [],
	"techAttributes": [
		{
			"tag": "42477",
			"categoryParent": "IT Infrastructure",
			"category": "Domain Name Services",
			"vendor": "GoDaddy Operating Company, LLC",
			"product": "GoDaddy DNS",
			"attribute": "334.4.75",
			"website": "https://www.godaddy.com/",
			"logo": "https://res.cloudinary.com/zoominfo-com/image/upload/w_100,h_100,c_fit/godaddy.com",
			"domain": "godaddy.com",
			"createdTime": "2017-04-25 07:42:01+00:00",
			"modifiedTime": "2020-12-08 20:57:53+00:00",
			"description": "GoDaddy is an internet domain registrar and web hosting company facilitating online businesses."
		}
	],
	"revenueRange": "",
	"employeeRange": "",
	"companyFunding": [],
	"recentFundingAmount": 0,
	"recentFundingDate": "",
	"totalFundingAmount": 0,
	"employeeGrowth": {
		"oneYearGrowthRate": "0.0",
		"twoYearGrowthRate": "0.0",
		"employeeGrowthDataPoints": []
	}
	*/

	/**
	 * Default constructor
	 */
	public OrganizationEnrichItem() {
	}

}
