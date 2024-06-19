package com.cyberintech.vrisk.server.service.utils;

import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.service.BusinessUnitService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExportUtils {

	public static Object asString(Country country) {
		if (country != null) {
			return StringUtils.trim(country.getName());
		}
		return "";
	}

	public static String asString(DataAssetClassification assetClassification) {
		if (assetClassification != null) {
			return StringUtils.trim(assetClassification.getName());
		}
		return "";
	}

	public static String asString(DataDomains dataDomain) {
		if (dataDomain != null) {
			return StringUtils.trim(dataDomain.getName());
		}
		return "";
	}

	public static Object asString(EnvironmentTypes environmentType) {
		if (environmentType != null) {
			return StringUtils.trim(environmentType.getName());
		}
		return "";
	}

	public static Object asString(Organizations organization) {
		if (organization != null) {
			return StringUtils.trim(organization.getName());
		}
		return "";
	}

	public static String asString(TechnologyCategories technologyCategory) {
		if (technologyCategory != null) {
			return StringUtils.trim(technologyCategory.getName());
		}
		return "";
	}

	public static String asString(BusinessUnits businessUnit, BusinessUnitService businessUnitService) {
		if (businessUnit != null) {
			return businessUnitService.getBusinessUnitPath(businessUnit, true, CSVUtils.PATH_SEPARATOR);
		}
		return "";
	}

	public static String businessUnitsAsString(Set<BusinessUnits> businessUnits,
											   BusinessUnitService businessUnitService) {
		Set<String> businessUnitUsedPaths = new HashSet<>();
		if (businessUnits != null) {
			for (BusinessUnits businessUnit : businessUnits) {
				businessUnitUsedPaths.add(asString(businessUnit, businessUnitService));
			}
		}
		return StringUtils.join(businessUnitUsedPaths, CSVUtils.LIST_SEPARATOR);
	}

	public static String dataAssetClassificationsAsString(Set<DataAssetClassification> dataAssetClassifications) {
		if (CollectionUtils.isNotEmpty(dataAssetClassifications)) {
			return StringUtils.join(
				dataAssetClassifications.stream().map(DataAssetClassification::getName).collect(Collectors.toSet()),
				CSVUtils.LIST_SEPARATOR);
		}
		return "";
	}

	public static String dataDomainsAsString(Set<DataDomains> dataDomains) {
		if (CollectionUtils.isNotEmpty(dataDomains)) {
			return StringUtils.join(
				dataDomains.stream().map(DataDomains::getName).collect(Collectors.toSet()), CSVUtils.LIST_SEPARATOR);
		}
		return "";
	}

	public static String dataTypeClassificationAsString(Set<DataTypeClassification> dataTypeClassifications) {
		if (CollectionUtils.isNotEmpty(dataTypeClassifications)) {
			return StringUtils.join(
				dataTypeClassifications.stream().map(DataTypeClassification::getName).collect(Collectors.toSet()),
				CSVUtils.LIST_SEPARATOR);
		}
		return "";
	}

	public static String processesAsString(Set<Processes> processes) {
		if (CollectionUtils.isNotEmpty(processes)) {
			return StringUtils.join(
				processes.stream().map(Processes::getName).collect(Collectors.toSet()), CSVUtils.LIST_SEPARATOR);
		}
		return "";
	}

	public static String systemsAsString(Set<Systems> systems) {
		if (CollectionUtils.isNotEmpty(systems)) {
			return StringUtils.join(
				systems.stream().map(Systems::getName).collect(Collectors.toSet()), CSVUtils.LIST_SEPARATOR);
		}
		return "";
	}

	public static String technologiesAsString(Set<Technologies> technologies) {
		if (CollectionUtils.isNotEmpty(technologies)) {
			return StringUtils.join(
				technologies.stream().map(Technologies::getName).collect(Collectors.toSet()), CSVUtils.LIST_SEPARATOR);
		}
		return "";
	}

	public static String userFullNameAsString(Users user) {
		if (user != null) {
			return StringUtils.trim(user.getFullName());
		}
		return "";
	}

	public static String userEmailAsString(Users user) {
		if (user != null) {
			return StringUtils.trim(user.getEmail());
		}
		return "";
	}

	public static String isRegulationRestrictedAsString(Boolean isRestricted) {
		if (isRestricted != null) {
			if (isRestricted = true) {
				return "YES";
			} else {
				return "NO";
			}
		}
		return "";
	}
	public static String regulationsAsString(Set<Regulations> regulations) {
		if (CollectionUtils.isNotEmpty(regulations)) {
			return StringUtils.join(
				regulations.stream().map(Regulations::getAcronym).collect(Collectors.toSet()), CSVUtils.LIST_SEPARATOR).trim();
		}
		return "";
	}

	public static String metricLevelAsString(QuantMetricLevel metricLevel) {
		if (metricLevel != null) {
			return StringUtils.trim(metricLevel.toString());
		}
		return "";
	}

	public static String technologyCategoriesAsString(Set<TechnologyCategories> technologyCategories) {
		if (CollectionUtils.isNotEmpty(technologyCategories)) {
			return StringUtils.join(
				technologyCategories.stream().map(TechnologyCategories::getName).collect(Collectors.toSet()),
				CSVUtils.LIST_SEPARATOR).trim();
		}
		return "";
	}

	public static String industriesAsString(Set<Industries> industries) {
		if (CollectionUtils.isNotEmpty(industries)) {
			return StringUtils.join(
				industries.stream().map(Industries::getName).collect(Collectors.toSet()), CSVUtils.LIST_SEPARATOR).trim();
		}
		return "";
	}


	public static String metricFormulaItemsAsString(Set<MetricFormulaItems> formulaItems) {
		if (!formulaItems.isEmpty()) {
			List<String> result = formulaItems.stream().map(temp -> {
				String str = temp.getOrdinal().toString();
				if (StringUtils.isNotEmpty(temp.getName())) {
					str += CSVUtils.FIELD_SEPARATOR + temp.getName();
				} else str += CSVUtils.FIELD_SEPARATOR;
				if (StringUtils.isNotEmpty(temp.getDescription())) {
					str += CSVUtils.FIELD_SEPARATOR + temp.getDescription();
				} else str += CSVUtils.FIELD_SEPARATOR;
				if (temp.getValue() != null) {
					str += CSVUtils.FIELD_SEPARATOR + temp.getValue();
				} else str += CSVUtils.FIELD_SEPARATOR;
				if (temp.getOperation() != null) {
					str += CSVUtils.FIELD_SEPARATOR + temp.getOperation();
				} else str += CSVUtils.FIELD_SEPARATOR;
				if (temp.getVariableType() != null) {
					str += CSVUtils.FIELD_SEPARATOR + temp.getVariableType().getName();
				} else str += "";
				return str;
			}).toList();
			return StringUtils.join(result, CSVUtils.LIST_SEPARATOR);
		}
		return "";
	}

}
