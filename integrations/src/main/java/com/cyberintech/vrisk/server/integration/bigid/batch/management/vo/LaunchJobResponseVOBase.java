package com.cyberintech.vrisk.server.integration.bigid.batch.management.vo;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public abstract class LaunchJobResponseVOBase<P, J> {
	private final P params;
	private final J job;
}
