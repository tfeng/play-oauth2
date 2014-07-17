import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:spring/*.xml"})
public class IntegrationTest {

  private static final int TIMEOUT = 10000;

  @Value("${security.client.id}")
  private String clientId;

  @Value("${security.client.secret}")
  private String clientSecret;

  @Value("${security.user.password}")
  private String userPassword;

  @Test
  public void testClientAuthenticationFailure() {
    running(testServer(3333), () -> {
      WSResponse response;

      response = authenticateClient(clientId, "wrong_" + clientSecret);
      assertThat(response.getStatus()).isEqualTo(401);

      response = authenticateClient("wrong_" + clientId, clientSecret);
      assertThat(response.getStatus()).isEqualTo(401);
    });
  }

  @Test
  public void testClientAuthenticationSuccess() {
    running(testServer(3333), () -> {
      WSResponse response = authenticateClient(clientId, clientSecret);
      JsonNode json = response.asJson();
      assertThat(json.findPath("accessToken").textValue()).isNotEmpty();
      assertThat(json.findPath("clientId").textValue()).isEqualTo(clientId);
      assertThat(json.findPath("expiration").longValue()).isGreaterThan(System.currentTimeMillis());
    });
  }

  @Test
  public void testUserAccessDenied() {
    running(testServer(3333), () -> {
      WSResponse response;

      response = authenticateClient(clientId, clientSecret);
      String clientAccessToken = response.asJson().findPath("accessToken").textValue();

      response = authenticateUser(clientAccessToken, "test", "wrong_" + userPassword);
      assertThat(response.getStatus()).isEqualTo(401);
    });
  }

  @Test
  public void testUserAccessTokenRefreshSuccess() {
    running(testServer(3333), () -> {
      WSResponse response;
      JsonNode json;

      response = authenticateClient(clientId, clientSecret);
      String clientAccessToken = response.asJson().findPath("accessToken").textValue();

      response = authenticateUser(clientAccessToken, "test", userPassword);
      json = response.asJson();
      assertThat(json.findPath("accessToken").textValue()).isNotEmpty();
      assertThat(json.findPath("refreshToken").textValue()).isNotEmpty();
      assertThat(json.findPath("username").textValue()).isEqualTo("test");
      assertThat(json.findPath("expiration").longValue()).isGreaterThan(System.currentTimeMillis());
      String oldUserAccessToken = json.findPath("accessToken").textValue();
      String refreshToken = json.findPath("refreshToken").textValue();

      response = getUser(oldUserAccessToken);
      json = response.asJson();
      assertThat(json.findPath("username").textValue()).isEqualTo("test");
      assertThat(json.findPath("isActive").booleanValue()).isEqualTo(true);

      response = refreshUserAccessToken(clientAccessToken, refreshToken);
      json = response.asJson();
      assertThat(json.findPath("accessToken").textValue()).isNotEmpty();
      assertThat(json.findPath("refreshToken").textValue()).isEqualTo(refreshToken);
      assertThat(json.findPath("expiration").longValue()).isGreaterThan(System.currentTimeMillis());
      String newUserAccessToken = json.findPath("accessToken").textValue();

      response = getUser(newUserAccessToken);
      json = response.asJson();
      assertThat(json.findPath("username").textValue()).isEqualTo("test");
      assertThat(json.findPath("isActive").booleanValue()).isEqualTo(true);

      response = getUser(oldUserAccessToken);
      assertThat(response.getStatus()).isEqualTo(401);
    });
  }

  @Test
  public void testUserAuthenticationFailure() {
    running(testServer(3333), () -> {
      WSResponse response = getUser("1234-abcd");
      assertThat(response.getStatus()).isEqualTo(401);
    });
  }

  @Test
  public void testUserAuthenticationSuccess() {
    running(testServer(3333), () -> {
      WSResponse response;
      JsonNode json;

      response = authenticateClient(clientId, clientSecret);
      String clientAccessToken = response.asJson().findPath("accessToken").textValue();

      response = authenticateUser(clientAccessToken, "test", userPassword);
      json = response.asJson();
      assertThat(json.findPath("accessToken").textValue()).isNotEmpty();
      assertThat(json.findPath("refreshToken").textValue()).isNotEmpty();
      assertThat(json.findPath("username").textValue()).isEqualTo("test");
      assertThat(json.findPath("expiration").longValue()).isGreaterThan(System.currentTimeMillis());
      String userAccessToken = json.findPath("accessToken").textValue();

      response = getUser(userAccessToken);
      json = response.asJson();
      assertThat(json.findPath("username").textValue()).isEqualTo("test");
      assertThat(json.findPath("isActive").booleanValue()).isEqualTo(true);
    });
  }

  @Test
  public void testUserNotAuthenticated() {
    running(testServer(3333), () -> {
      WSResponse response = getUser(null);
      assertThat(response.getStatus()).isEqualTo(401);

      response = authenticateClient(clientId, clientSecret);
      String clientAccessToken = response.asJson().findPath("accessToken").textValue();

      response = getUser(clientAccessToken);
      assertThat(response.getStatus()).isEqualTo(500);
    });
  }

  private WSResponse authenticateClient(String clientId, String clientSecret) {
    ObjectNode request = Json.newObject();
    request.putAll(ImmutableMap.of("clientId", request.textNode(clientId),
        "clientSecret", request.textNode(clientSecret)));
    return WS.url("http://localhost:3333/client/authenticate").post(request).get(TIMEOUT);
  }

  private WSResponse authenticateUser(String clientAccessToken, String username, String password) {
    ObjectNode request = Json.newObject();
    request.putAll(ImmutableMap.of("username", request.textNode(username),
        "password", request.textNode(password)));
    return WS.url("http://localhost:3333/user/authenticate")
        .setHeader("Authorization", "Bearer " + clientAccessToken).post(request).get(TIMEOUT);
  }

  private WSResponse getUser(String userAccessToken) {
    WSRequestHolder requestHolder = WS.url("http://localhost:3333/user/get");
    if (userAccessToken != null) {
      requestHolder.setHeader("Authorization", "Bearer " + userAccessToken);
    }
    return requestHolder.get().get(TIMEOUT);
  }

  private WSResponse refreshUserAccessToken(String clientAccessToken, String refreshToken) {
    ObjectNode request = Json.newObject();
    request.putAll(ImmutableMap.of("refreshToken", request.textNode(refreshToken)));
    return WS.url("http://localhost:3333/user/refresh")
        .setHeader("Authorization", "Bearer " + clientAccessToken).post(request).get(TIMEOUT);
  }
}
