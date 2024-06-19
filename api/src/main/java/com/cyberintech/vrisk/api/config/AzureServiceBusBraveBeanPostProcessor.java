package com.cyberintech.vrisk.api.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * We need to disable cache producers because Azure Service Bus kills connections
 * after some time. Sleuth uses BeanPostProcessor for wrapping a JMS
 * ConnectionFactory and then there is no way how to get the original
 * ConnectionFactory. This BeanPostProcessor with higher precedence disables
 * cache producers before Sleuth wraps the ConnectionFactory.
 */
@Component
public class AzureServiceBusBraveBeanPostProcessor implements BeanPostProcessor, Ordered {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof CachingConnectionFactory) {
			CachingConnectionFactory cachingConnectionFactory = (CachingConnectionFactory) bean;
			cachingConnectionFactory.setCacheProducers(false);
		}
		return bean;
	}

	/**
	 * It has to run before Sleuth/Brave TracingConnectionFactoryBeanPostProcessor
	 *
	 * @return
	 */
	@Override
	public int getOrder() {
		return 0;
	}

}
