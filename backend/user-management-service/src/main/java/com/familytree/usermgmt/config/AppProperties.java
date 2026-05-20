package com.familytree.usermgmt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.s3")
public class AppProperties {

  private String endpoint;
  private String accessKey;
  private String secretKey;
  private String bucket;
  private String region = "us-east-1";
  /** Lifetime of presigned download URLs returned to clients (minutes). */
  private int presignedUrlExpiryMinutes = 60;

  public String getEndpoint() { return endpoint; }
  public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

  public String getAccessKey() { return accessKey; }
  public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

  public String getSecretKey() { return secretKey; }
  public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

  public String getBucket() { return bucket; }
  public void setBucket(String bucket) { this.bucket = bucket; }

  public String getRegion() { return region; }
  public void setRegion(String region) { this.region = region; }

  public int getPresignedUrlExpiryMinutes() { return presignedUrlExpiryMinutes; }
  public void setPresignedUrlExpiryMinutes(int presignedUrlExpiryMinutes) {
    this.presignedUrlExpiryMinutes = presignedUrlExpiryMinutes;
  }
}
