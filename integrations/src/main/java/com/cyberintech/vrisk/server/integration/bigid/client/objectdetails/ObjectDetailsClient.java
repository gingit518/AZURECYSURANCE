package com.cyberintech.vrisk.server.integration.bigid.client.objectdetails;

import static com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil.wrap;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.exception.RemoteClientException;
import com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo.ObjectDetailsVO;
import com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo.ObjectDetailsVOWrapper;
import com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo.application.AssociatedApplicationListWrapper;
import com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo.application.ObjectDetailsApplicationsAwareVO;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

public class ObjectDetailsClient extends BigIdClient {

	public ObjectDetailsClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public ObjectDetailsVO getObjectDetailsByFullyQualifiedName(String fullyQualifiedName) {
		ObjectDetailsVO objectDetailsVO = wrap(
			() -> Optional
				.ofNullable(getRestTemplate().getForObject(
					formatRequestUrl("/api/v1/data-catalog/object-details/?object_name={qualified_name}&format=json"),
					ObjectDetailsVOWrapper.class, Map.ofEntries(Map.entry("qualified_name", fullyQualifiedName))))
				.map(ObjectDetailsVOWrapper::getData)
				.orElseThrow(() -> new RemoteClientException(
					String.format("Object details by '%s' fully qualified name are expected.", fullyQualifiedName))),
			String.format("get object details by fully qualified name = %s", fullyQualifiedName));
		ObjectDetailsApplicationsAwareVO associatedApplicationsAware = getAssociatedApplications(fullyQualifiedName);
		objectDetailsVO.setApplications(associatedApplicationsAware.getApplications());
		objectDetailsVO.setTags(associatedApplicationsAware.getTags());
		return objectDetailsVO;
	}

	public ObjectDetailsApplicationsAwareVO getAssociatedApplications(String fullyQualifiedName) {
		return wrap(
			() -> Optional
				.ofNullable(getRestTemplate().getForObject(
					formatRequestUrl("/api/v1/data-catalog/?format=json&limit=1&filter={filter}"),
					AssociatedApplicationListWrapper.class,
					Map.ofEntries(
						Map.entry("filter", String.format("fullyQualifiedName = \"%s\"", fullyQualifiedName)))))
				.map(AssociatedApplicationListWrapper::getResults)
				.filter(CollectionUtils::isNotEmpty)
				.map(c -> c.get(0))
				.orElseThrow(() -> new RemoteClientException(String
					.format("Associated applications by '%s' fully qualified name are expected.", fullyQualifiedName))),
			String.format("get object details by fully qualified name '%s' with associated applications.",
				fullyQualifiedName));
	}

}
