package com.cyberintech.vrisk.server.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
public class AWSConfiguration {

	private static final String AWS_S3_ENDPOINT_PROPERTY = "cloud.aws.s3.endpoint";

	@Value("${cloud.aws.credentials.accessKey:}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secretKey:}")
	private String secretKey;

	@Value("${cloud.aws.region.static:}")
	private String region;

	@Value("${cloud.aws.s3.bucket:}")
	private String bucket;

	@Autowired
	private Environment env;

	@Bean
	public BasicAWSCredentials basicAWSCredentials() {
		if (StringUtils.isNotEmpty(accessKey)) {
			return new BasicAWSCredentials(accessKey, secretKey);
		}

		return null;
	}

	@Bean
	public AmazonS3 amazonS3(AWSCredentials awsCredentials) {
		AmazonS3 amazonS3 = null;
		try {
			// If access key is empty - return null
			if (StringUtils.isEmpty(accessKey)) {
				return null;
			}

			AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
			builder.withCredentials(new AWSStaticCredentialsProvider(awsCredentials));

			if (env.containsProperty(AWS_S3_ENDPOINT_PROPERTY)) {
				builder.setEndpointConfiguration(new EndpointConfiguration(env.getProperty(AWS_S3_ENDPOINT_PROPERTY), region));
			} else {
				builder.setRegion(region);
			}

			amazonS3 = builder.build();

			// Creating bucket if it is not exists
			if (!amazonS3.doesBucketExistV2(bucket)) {
				log.info(MessageFormat.format("## Creating AWS bucket as it not exists: {0}", bucket));
				amazonS3.createBucket(bucket);
			}
		} catch (AmazonS3Exception exception) {
			log.error(String.format("Failed to initialize Amazon S3 account %s", accessKey), exception);
		}

		return amazonS3;
	}

}
