package filters;

import java.lang.reflect.Method;

import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

public interface Filter {

  public Promise<Result> apply(Request request, Method method, Context context, Action<?> action)
      throws Throwable;

  public void end(Request request, Method method, Context context);

  public void start(Request request, Method method, Context context);
}
