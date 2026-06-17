package org.example.s3select.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import java.net.URI;

public final class S3ClientFactory {
  private S3ClientFactory() {
  }

  public static S3Client create(
      String endpoint,
      String accessKey,
      String secretKey
  ) {
    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.US_EAST_1)
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            )
        )
        .httpClientBuilder(UrlConnectionHttpClient.builder())
        .forcePathStyle(true)
        .build();
  }
}
