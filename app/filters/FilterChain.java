package filters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Action.Simple;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

public class FilterChain extends ArrayList<Filter> {

  private static final long serialVersionUID = 1L;

  public Action<Void> getAction(final Request request, final Method method) {
    int filterCount = size();
    Action<Void> action = new Simple() {
      @Override
      public Promise<Result> call(Context context) throws Throwable {
        return delegate.call(context);
      }
    };
    for (int i = filterCount - 1; i >= 0; i--) {
      final Action<Void> nextAction = action;
      final Filter filter = get(i);
      action = new Simple() {
        @Override
        public Promise<Result> call(final Context context) throws Throwable {
          nextAction.delegate = delegate;
          return filter.apply(request, method, context, nextAction);
        }
      };
    }
    final Action<?> firstAction = action;
    return new Simple() {
      @Override
      public Promise<Result> call(Context context) throws Throwable {
        firstAction.delegate = delegate;
        stream().forEach(filter -> filter.start(request, method, context));
        Promise<Result> result = firstAction.call(context);
        for (ListIterator<Filter> iterator = listIterator(size() - 1); iterator.hasPrevious();) {
          iterator.previous().end(request, method, context);
        }
        return result;
      }
    };
  }

  public void setFilters(List<Filter> filters) {
    clear();
    addAll(filters);
  }
}
