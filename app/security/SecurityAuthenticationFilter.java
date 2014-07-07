package security;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import play.Logger;
import play.Logger.ALogger;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;
import filters.Filter;

@Component
public class SecurityAuthenticationFilter implements Filter {

  private ALogger logger = Logger.of("security");

  @Autowired
  private OAuth2AuthenticationManager oauth2AuthenticationManager;

  @Override
  public Promise<Result> apply(Request request, Method method, Context context, Action<?> action)
      throws Throwable {
    String token = getAuthorizationToken(request);
    if (token == null) {
      token = request.getQueryString(OAuth2AccessToken.ACCESS_TOKEN);
    }
    if (token == null) {
      logger.info("Authentication skipped");
    } else {
      Authentication authRequest = new PreAuthenticatedAuthenticationToken(token, "");
      Authentication authResult = oauth2AuthenticationManager.authenticate(authRequest);
      SecurityContextHolder.getContext().setAuthentication(authResult);
      logger.info("Authenticated successfully");
    }
    return action.call(context);
  }

  @Override
  public void end(Request request, Method method, Context context) {
  }

  @Override
  public void start(Request request, Method method, Context context) {
  }

  protected String getAuthorizationToken(Request request) {
    String[] headers = request.headers().get("Authorization");
    if (headers != null) {
      for (String header : headers) {
        if (header.toLowerCase().startsWith(OAuth2AccessToken.BEARER_TYPE.toLowerCase())) {
          String authHeaderValue = header.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
          return authHeaderValue.split(",")[0];
        }
      }
    }
    return null;
  }
}
