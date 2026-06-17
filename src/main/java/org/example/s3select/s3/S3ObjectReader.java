package org.example.s3select.s3;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class S3ObjectReader {
  private final S3Client s3Client;

  public S3ObjectReader(S3Client s3Client) {
    this.s3Client = s3Client;
  }

  public ResponseInputStream<GetObjectResponse> read(String bucket, String key) {
    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    return s3Client.getObject(request);
  }
}
