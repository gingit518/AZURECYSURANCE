package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.util.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;

import java.text.MessageFormat;
import java.util.ArrayList;
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
@Slf4j
public class OrganizationZoomInfoExtendedDetails extends OrganizationEnrichItem {

	private List<OrgChartEnrichItem> orgChartItems;

	private List<OrganizationHierarchyEnrichTreeNodeItem> subsidiaries;

	/**
	 * Default constructor
	 */
	public OrganizationZoomInfoExtendedDetails() {
		super();

		orgChartItems = new ArrayList<>();
	}

	/**
	 * Static cloning method to obtain Enrich data
	 *
	 * @param source
	 * @return
	 */
	public static OrganizationZoomInfoExtendedDetails of(OrganizationEnrichItem source) {
		OrganizationZoomInfoExtendedDetails destination = new OrganizationZoomInfoExtendedDetails();

		long unproxyTime = System.currentTimeMillis();
		ModelMapper modelMapper = BeanUtil.getBean(ModelMapper.class);
		modelMapper.map(source,destination);
		long mappingTime = System.currentTimeMillis() - unproxyTime;

		if (mappingTime > 200) {
			log.warn(MessageFormat.format("## ModelMapper [OrganizationZoomInfoExtendedDetails]. Mapping time: {1}", mappingTime));
		}

		return destination;
	}

}
