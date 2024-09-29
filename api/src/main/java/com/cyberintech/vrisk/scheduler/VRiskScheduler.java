package com.cyberintech.vrisk.scheduler;

import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.RiskModelCalculationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Common Scheduler Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
@Service
@Profile("scheduler")
@Slf4j
public class VRiskScheduler {

	@Autowired
	private RiskModelCalculationsService riskModelCalculationsService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Start first execution in 1 sec after application start and schedule next execution in one hour after previous execution
	 */
	@Scheduled(cron = "0 0 23 * * *")
	public void cacheMetricsRebuildScheduler() {
		runCacheMetricsRebuildScheduler();
	}

	/**
	 * Start first execution in 300 sec after application start and execute it (ONCE)
	 */
	@Scheduled(initialDelay = 300000, fixedDelay=Long.MAX_VALUE)
	public void runCacheMetricsRebuildSchedulerIn3600Secs() {
		runCacheMetricsRebuildScheduler();
	}

	protected void runCacheMetricsRebuildScheduler() {
		log.info("#### Starting VRisk Scheduler");

		riskModelCalculationsService.rebuildAllMetricsCache();

		log.info("#### Executed VRisk Scheduler");
	}

	/**
	 * Start first execution in 30 sec after application start and schedule next execution in 5 minutes after previous execution
	 */
	@Scheduled(initialDelay = 180000, fixedDelay = 300000)
	public void cleanupOrganizationPowerBICapacity() {
		log.debug("#### Starting VRisk Capacity Cleanup Scheduler");
		organizationService.cleanupOrganizationPowerBICapacity();
	}

}
