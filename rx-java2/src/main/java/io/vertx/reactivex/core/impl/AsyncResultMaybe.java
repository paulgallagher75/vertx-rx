package io.vertx.reactivex.core.impl;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class AsyncResultMaybe<T> extends Maybe<T> {

  private final Handler<Handler<AsyncResult<T>>> method;

  public AsyncResultMaybe(Handler<Handler<AsyncResult<T>>> method) {
    this.method = method;
  }

  @Override
  protected void subscribeActual(MaybeObserver<? super T> observer) {
    AtomicBoolean disposed = new AtomicBoolean();
    observer.onSubscribe(new Disposable() {
      @Override
      public void dispose() {
        disposed.set(true);
      }
      @Override
      public boolean isDisposed() {
        return disposed.get();
      }
    });
    if (!disposed.get()) {
      method.handle(ar -> {
        if (!disposed.getAndSet(true)) {
          if (ar.succeeded()) {
            try {
              T val = ar.result();
              if (val != null) {
                observer.onSuccess(val);
              } else {
                observer.onComplete();
              }
            } catch (Throwable ignore) {
            }
          } else if (ar.failed()) {
            try {
              observer.onError(ar.cause());
            } catch (Throwable ignore) {
            }
          }
        }
      });
    }
  }
}
