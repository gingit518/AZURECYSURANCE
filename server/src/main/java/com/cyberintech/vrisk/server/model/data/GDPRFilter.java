package com.cyberintech.vrisk.server.model.data;

import com.cyberintech.vrisk.server.model.jpa.domains.HierarchyLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Implementation of GDPR Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-25
 */
@NoArgsConstructor
@Setter
@Getter
public class GDPRFilter extends NameFilter {

	private HierarchyLevel level;

	private Long chapterId;

	private Long sectionId;

	private Long articleId;

	private Long paragraphId;

	private Long systemId;

	private Boolean isSystemLevel;

	private Boolean isOrganizationLevel;

	private List<Long> dataTypeClassification;

}
