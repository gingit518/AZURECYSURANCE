package com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo.application;

import lombok.Data;

import java.util.List;

@Data
public class AssociatedApplicationListWrapper {
	private List<ObjectDetailsApplicationsAwareVO> results;
}
