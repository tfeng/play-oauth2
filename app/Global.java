import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

import play.Application;
import play.GlobalSettings;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;
import filters.FilterChain;

public class Global extends GlobalSettings {

  private ApplicationContext context;
  private FilterChain filterChain;

  @Override
  public <A> A getControllerInstance(Class<A> clazz) {
    return context.getBean(clazz);
  }

  @Override
  public Promise<Result> onError(RequestHeader request, Throwable t) {
    Throwable cause = t.getCause();
    if (cause instanceof AuthenticationException
        || cause instanceof ClientAuthenticationException
        || cause instanceof ClientRegistrationException) {
      return Promise.pure(Results.unauthorized());
    } else {
      return Promise.pure(Results.internalServerError());
    }
  }

  @Override
  public Action<?> onRequest(Request request, Method method) {
    return filterChain.getAction(request, method);
  }

  @Override
  public void onStart(Application app) {
    context = new ClassPathXmlApplicationContext("classpath*:spring/*.xml");

    filterChain = context.getBean("filterChain", FilterChain.class);
    if (filterChain == null) {
      filterChain = new FilterChain();
    }
  }
}
