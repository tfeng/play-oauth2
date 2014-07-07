package filters;

import java.lang.reflect.Method;

import org.springframework.stereotype.Component;

import play.Logger;
import play.Logger.ALogger;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

@Component
public class LogFilter implements Filter {

  private ALogger logger = Logger.of("request");

  @Override
  public Promise<Result> apply(Request request, Method method, Context context, Action<?> action)
      throws Throwable {
    return action.call(context);
  }

  @Override
  public void end(Request request, Method method, Context context) {
    logger.info("End " + request + " -> " + method.getDeclaringClass().getName() + "."
        + method.getName());
  }

  @Override
  public void start(Request request, Method method, Context context) {
    logger.info("Start " + request + " -> " + method.getDeclaringClass().getName() + "."
        + method.getName());
  }
}
