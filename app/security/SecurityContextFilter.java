package security;

import java.lang.reflect.Method;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import play.cache.Cache;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import play.mvc.Result;
import filters.Filter;

@Component
public class SecurityContextFilter implements Filter {

  public static final int EXPIRATION_IN_SECONDS = 3600;
  public static final String SECURITY_CONTEXT_COOKIE = "secctx";

  @Override
  public Promise<Result> apply(Request request, Method method, Context context, Action<?> action)
      throws Throwable {
    return action.call(context);
  }

  @Override
  public void end(Request request, Method method, Context context) {
    SecurityContextHolder.clearContext();
    saveSecurityContext(context, SecurityContextHolder.getContext());
  }

  public SecurityContext loadSecurityContext(Context context) {
    if (context != null) {
      Cookie cookie = context.request().cookie(SECURITY_CONTEXT_COOKIE);
      if (cookie != null) {
        return (SecurityContext) Cache.get(cookie.value());
      }
    }
    return SecurityContextHolder.createEmptyContext();
  }

  public void saveSecurityContext(Context context, SecurityContext securityContext) {
    Cookie cookie = context.request().cookie(SECURITY_CONTEXT_COOKIE);
    if (cookie != null) {
      Cache.remove(cookie.value());
    }
    String randomUuid = UUID.randomUUID().toString();
    context.response().setCookie(SECURITY_CONTEXT_COOKIE, randomUuid, EXPIRATION_IN_SECONDS);
    Cache.set(randomUuid, securityContext, EXPIRATION_IN_SECONDS);
  }

  @Override
  public void start(Request request, Method method, Context context) {
    SecurityContextHolder.setContext(loadSecurityContext(context));
  }
}
