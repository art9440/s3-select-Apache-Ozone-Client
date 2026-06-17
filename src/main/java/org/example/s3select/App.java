package org.example.s3select;

import org.example.s3select.csv.CsvSelectEngine;
import org.example.s3select.s3.S3ClientFactory;
import org.example.s3select.s3.S3ObjectReader;
import org.example.s3select.sql.ParsedQuery;
import org.example.s3select.sql.QueryParser;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class App {
  public static void main(String[] args) throws Exception {
    Map<String, String> params = parseArgs(args);

    String endpoint = require(params, "--endpoint");
    String accessKey = require(params, "--access-key");
    String secretKey = require(params, "--secret-key");
    String bucket = require(params, "--bucket");
    String key = require(params, "--key");
    String query = require(params, "--query");

    String outputPath = params.get("--output");

    QueryParser queryParser = new QueryParser();
    ParsedQuery parsedQuery = queryParser.parse(query);

    try (
        S3Client s3Client = S3ClientFactory.create(endpoint, accessKey, secretKey);
        ResponseInputStream<GetObjectResponse> objectInput =
            new S3ObjectReader(s3Client).read(bucket, key);
        OutputStream output = outputPath == null
            ? System.out
            : new FileOutputStream(outputPath)
    ) {
      CsvSelectEngine engine = new CsvSelectEngine();
      engine.execute(objectInput, output, parsedQuery);
    }
  }

  private static Map<String, String> parseArgs(String[] args) {
    if (args.length % 2 != 0) {
      printUsageAndExit();
    }

    Map<String, String> params = new HashMap<>();

    for (int i = 0; i < args.length; i += 2) {
      params.put(args[i], args[i + 1]);
    }

    return params;
  }

  private static String require(Map<String, String> params, String name) {
    String value = params.get(name);

    if (value == null || value.isBlank()) {
      System.err.println("Missing required argument: " + name);
      printUsageAndExit();
    }

    return value;
  }

  private static void printUsageAndExit() {
    System.err.println("""
                Usage:
                  java -jar s3-csv-select.jar \\
                    --endpoint http://localhost:9878 \\
                    --access-key <accessKey> \\
                    --secret-key <secretKey> \\
                    --bucket <bucket> \\
                    --key <key> \\
                    --query "SELECT * FROM S3Object" \\
                    [--output result.csv]

                Examples:
                  java -jar s3-csv-select.jar \\
                    --endpoint http://localhost:9878 \\
                    --access-key test \\
                    --secret-key test \\
                    --bucket test-bucket \\
                    --key users.csv \\
                    --query "SELECT name, age FROM S3Object WHERE age > 30"
                """);

    System.exit(1);
  }
}
