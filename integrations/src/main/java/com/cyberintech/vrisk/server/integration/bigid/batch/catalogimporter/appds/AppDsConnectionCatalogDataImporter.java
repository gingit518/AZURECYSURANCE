package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.CatalogDataImporterBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.BigIdDatasourceTypeHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.model.jpa.entity.Technologies;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppDsConnectionCatalogDataImporter
	extends CatalogDataImporterBase<AppDsConnectionCatalogDataImporterParamVO, AppDsConnectionCatalogDataImporterResultVO> {

	private final TechnologyRepository technologyRepository;
	private final SystemRepository systemRepository;
	private final BigIdDatasourceTypeHelper bigIdDatasourceTypeHelper;

	public AppDsConnectionCatalogDataImporter(OrganizationRepository organizationRepository,
											  UserRepository userRepository,
											  ApplicationEventPublisher applicationEventPublisher,
											  UserMetadataHelper userMetadataHelper,
											  UserImportHelper userImportHelper,
											  TechnologyRepository technologyRepository,
											  SystemRepository systemRepository, BigIdDatasourceTypeHelper bigIdDatasourceTypeHelper) {
		super(organizationRepository, userRepository, applicationEventPublisher, userMetadataHelper, userImportHelper);
		this.technologyRepository = technologyRepository;
		this.systemRepository = systemRepository;
		this.bigIdDatasourceTypeHelper = bigIdDatasourceTypeHelper;
	}

	@Override
	protected AppDsConnectionCatalogDataImporterResultVO process(Organizations currentOrganization, Users currentUser) {
		Systems system = systemRepository.getFirstByNameForOrganization(getParams().getApplicationVO().getName(), currentOrganization.getId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("Can not find system by %s name and organization %s.", getParams().getApplicationVO().getName(), new OrganizationRefDTO(currentOrganization))));
		Technologies technology = technologyRepository.getFirstByNameAndOrganization(getParams().getDatasourceVO().getName(), currentOrganization.getId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("Can not find technology by %s name and organization %s.", getParams().getDatasourceVO().getName(), new OrganizationRefDTO(currentOrganization))));
		setSystemTechnology(system, technology);
		setSystemType(system, getParams().getDatasourceVO());
		setInfoSecPerson(system, technology);

		getResult().setSystemId(system.getId());
		getResult().setTechnologyId(technology.getId());
		getResult().setSystemAction(ImportAction.SYNCED);
		return getResult();
	}

	private void setSystemTechnology(Systems system, Technologies technology) {
		system.getTechnologies().clear();
		system.getTechnologies().add(technology);
		systemRepository.save(system);
	}

	private void setSystemType(Systems system, DatasourceVO datasource) {
		system.setSystemType(bigIdDatasourceTypeHelper.asSystemType(datasource.getType(), null));
		systemRepository.save(system);
	}

	private void setInfoSecPerson(Systems system, Technologies technology) {
		system.setInfosecFocalPerson(technology.getInfosecFocalPerson());
		systemRepository.save(system);
	}


	@Override
	public boolean supports(Object data) {
		return data instanceof AppDsConnectionCatalogDataImporterParamVO;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected AppDsConnectionCatalogDataImporterResultVO initResult(AppDsConnectionCatalogDataImporterParamVO params) {
		return new AppDsConnectionCatalogDataImporterResultVO(params.getOrganizationId(), params.getUserImporterId(), params.getDatasourceVO(), params.getApplicationVO());
	}
}
