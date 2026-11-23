package com.cinemahub.cinemahub.lambda.shared;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

public final class DatabaseConnection {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final SecretsManagerClient SECRETS = SecretsManagerClient.create();
  private static volatile Credentials credentials;

  private DatabaseConnection() {}

  public static Connection open() throws Exception {
    String url = "jdbc:postgresql://%s:%s/%s?sslmode=verify-full&connectTimeout=5&socketTimeout=20".formatted(
        required("DB_HOST"), required("DB_PORT"), required("DB_NAME"));
    Credentials db = credentials();
    return DriverManager.getConnection(url, db.username(), db.password());
  }

  private static Credentials credentials() throws Exception {
    if (credentials == null) synchronized (DatabaseConnection.class) {
      if (credentials == null) {
        String secret = SECRETS.getSecretValue(GetSecretValueRequest.builder()
            .secretId(required("DB_SECRET_ARN")).build()).secretString();
        Map<String, String> values = JSON.readValue(secret, new TypeReference<>() {});
        credentials = new Credentials(values.get("username"), values.get("password"));
      }
    }
    return credentials;
  }

  private static String required(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) throw new IllegalStateException("Falta la variable " + key);
    return value;
  }

  private record Credentials(String username, String password) {}
}
