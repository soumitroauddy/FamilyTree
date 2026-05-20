package com.familytree.usermgmt.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

  private final AppProperties appProperties;

  public S3Config(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  private StaticCredentialsProvider credentialsProvider() {
    return StaticCredentialsProvider.create(
        AwsBasicCredentials.create(appProperties.getAccessKey(), appProperties.getSecretKey()));
  }

  private boolean isLocalMinIO() {
    String endpoint = appProperties.getEndpoint();
    return endpoint != null && endpoint.startsWith("http://");
  }

  private S3Configuration serviceConfiguration() {
    return S3Configuration.builder()
        .pathStyleAccessEnabled(isLocalMinIO())
        .build();
  }

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .endpointOverride(URI.create(appProperties.getEndpoint()))
        .credentialsProvider(credentialsProvider())
        .region(Region.of(appProperties.getRegion()))
        .serviceConfiguration(serviceConfiguration())
        .build();
  }

  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
        .endpointOverride(URI.create(appProperties.getEndpoint()))
        .credentialsProvider(credentialsProvider())
        .region(Region.of(appProperties.getRegion()))
        .serviceConfiguration(serviceConfiguration())
        .build();
  }
}
