package com.cinemahub.cinemahub.lambda.movie;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class ConsultarCarteleraHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();
  private static final SecretsManagerClient SECRETS = SecretsManagerClient.create();
  private static final String SQL = """
      SELECT m.id,m.title,m.synopsis,m.duration,m.release_date,m.poster_url,m.trailer_url,m.status,
             c.id classification_id,c.code classification_code,c.description classification_description
      FROM movies m JOIN classifications c ON c.id=m.classification_id
      """;
  private volatile Credentials credentials;

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    String path = Objects.toString(event.get("rawPath"), "");
    if (path.endsWith("/health")) return response(200, "{\"status\":\"ok\"}");
    String status = query(event, "status");
    if (status != null && !List.of("ACTIVE","INACTIVE","COMING_SOON","ARCHIVED").contains(status))
      return error(400, "Bad Request", "El parámetro status no es válido");
    try { return response(200, JSON.writeValueAsString(find(status))); }
    catch (Exception e) {
      context.getLogger().log("Error consultando cartelera: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudo consultar la cartelera");
    }
  }

  private List<Map<String,Object>> find(String status) throws Exception {
    String sql = SQL + (status == null ? " ORDER BY m.id" : " WHERE m.status=? ORDER BY m.id");
    try (Connection c=connect(); PreparedStatement s=c.prepareStatement(sql)) {
      if (status != null) s.setString(1,status);
      try (ResultSet r=s.executeQuery()) {
        List<Map<String,Object>> out=new ArrayList<>();
        while(r.next()) {
          Map<String,Object> classification=new HashMap<>();
          classification.put("id",r.getLong("classification_id"));
          classification.put("code",r.getString("classification_code"));
          classification.put("description",r.getString("classification_description"));
          Map<String,Object> movie=new HashMap<>();
          movie.put("id",r.getLong("id")); movie.put("title",r.getString("title"));
          movie.put("synopsis",r.getString("synopsis")); movie.put("duration",r.getInt("duration"));
          java.sql.Date release=r.getDate("release_date");
          movie.put("releaseDate",release==null?null:release.toLocalDate().toString());
          movie.put("posterUrl",r.getString("poster_url")); movie.put("trailerUrl",r.getString("trailer_url"));
          movie.put("classification",classification); movie.put("status",r.getString("status"));
          out.add(movie);
        }
        return out;
      }
    }
  }

  private Connection connect() throws Exception {
    String url="jdbc:postgresql://%s:%s/%s?sslmode=verify-full".formatted(env("DB_HOST"),env("DB_PORT"),env("DB_NAME"));
    Credentials c=credentials();
    return DriverManager.getConnection(url,c.username(),c.password());
  }
  private Credentials credentials() throws Exception {
    if(credentials==null) synchronized(this) {
      if(credentials==null) {
        String secret=SECRETS.getSecretValue(GetSecretValueRequest.builder().secretId(env("DB_SECRET_ARN")).build()).secretString();
        Map<String,String> values=JSON.readValue(secret,new TypeReference<>() {});
        credentials=new Credentials(values.get("username"),values.get("password"));
      }
    }
    return credentials;
  }
  private static String env(String key) {
    String value=System.getenv(key);
    if(value==null||value.isBlank()) throw new IllegalStateException("Falta la variable "+key);
    return value;
  }
  private static String query(Map<String,Object> event,String name) {
    Object values=event.get("queryStringParameters");
    if(!(values instanceof Map<?,?> map)||map.get(name)==null)return null;
    return map.get(name).toString();
  }
  private static Map<String,Object> error(int status,String error,String message) {
    try {
      return response(status,JSON.writeValueAsString(Map.of("timestamp",Instant.now().toString(),"status",status,"error",error,"message",message)));
    } catch(Exception ignored) { return response(500,"{\"status\":500}"); }
  }
  private static Map<String,Object> response(int status,String body) {
    return Map.of("statusCode",status,"headers",Map.of("content-type","application/json; charset=utf-8"),"isBase64Encoded",false,"body",body);
  }
  private record Credentials(String username,String password) {}
}
