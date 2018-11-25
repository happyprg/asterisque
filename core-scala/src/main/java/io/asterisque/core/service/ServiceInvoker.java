package io.asterisque.core.service;

import io.asterisque.core.Debug;
import io.asterisque.core.msg.Abort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * サービスとして定義されているオブジェクトに対して {@link Export} されたメソッドを保持し reflection を用いてその呼び出しを
 * 行うラッパークラスです。
 */
class ServiceInvoker {
  private static final Logger logger = LoggerFactory.getLogger(ServiceInvoker.class);

  /**
   * The ID of this service.
   */
  @Nonnull
  private final String id;

  /**
   * The instance of this service.
   */
  @Nonnull
  private final Object service;

  /**
   * Function id to method definition map.
   */
  @Nonnull
  private final Map<Short, FunctionDef> functions;

  ServiceInvoker(@Nonnull String id, @Nonnull Object service) {
    this.id = id;
    this.service = service;
    this.functions = getServiceInterface(service);
  }

  public Object invoke(short functionId, @Nonnull Object[] args) throws Abort {
    FunctionDef func = functions.get(functionId);
    if (func == null) {
      String msg = String.format("function %d is not defined for service %s", functionId, id);
      logger.info(msg);
      throw new Abort(Abort.FunctionUndefined, msg);
    }
    try {
      return func.method.invoke(func.isStatic ? null : service, args);
    } catch (InvocationTargetException ex) {
      Throwable e = Optional.ofNullable(ex.getTargetException()).orElse(ex);
      String msg = String.format("service %s (%s) encountering an error", id, service.getClass().getName());
      logger.error(msg, e);
      throw new Abort(Abort.FunctionAborted, msg);
    } catch (IllegalAccessException ex) {
      logger.error("inaccessible method detected: " + Debug.getSimpleName(func.method), ex);
      throw new Abort(Abort.Unexpected, "unexpected error");
    }
  }

  private static class FunctionDef {
    public final boolean isStatic;
    public final Annotation export;
    public final Method method;

    private FunctionDef(@Nonnull Method method) {
      this.isStatic = Modifier.isStatic(method.getModifiers());
      this.export = method.getAnnotation(Export.class);
      this.method = method;
    }
  }

  /**
   * クラスごとのサービスインターフェースをキャッシュするマップです。
   */
  private static final ConcurrentHashMap<Class<?>, Map<Short, FunctionDef>> SERVICE_INTERFACES = new ConcurrentHashMap<>();

  /**
   * 指定されたオブジェクトに対するサービスのインターフェースを参照します。function id に対する呼び出し定義のマップを
   * 返します。
   *
   * @param service サービス
   * @return サービス定義のマップ
   */
  @Nonnull
  private static Map<Short, FunctionDef> getServiceInterface(@Nonnull Object service) {
    return SERVICE_INTERFACES.computeIfAbsent(service.getClass(), clazz -> {
      List<Method> methods = Stream.of(Optional.ofNullable(clazz.getMethods()).orElse(new Method[0]))
          .filter(method -> method.getAnnotation(Export.class) != null)
          .collect(Collectors.toList());

      // detect duplication of function id
      List<List<Method>> duplicates = methods.stream()
          .collect(Collectors.groupingBy(m -> m.getAnnotation(Export.class).value()))
          .entrySet().stream().filter(e -> e.getValue().size() > 1).map(Map.Entry::getValue)
          .sorted((o1, o2) -> {
            short id1 = o1.get(0).getAnnotation(Export.class).value();
            short id2 = o2.get(0).getAnnotation(Export.class).value();
            return Short.compare(id1, id2);
          }).collect(Collectors.toList());
      if (!duplicates.isEmpty()) {
        String msg = String.join("\n", duplicates.stream().flatMap(List::stream).map(m -> {
              short id = m.getAnnotation(Export.class).value();
              String label = Debug.getSimpleName(m);
              return String.format("[%d] %s", id, label);
            }
        ).collect(Collectors.toList()));
        throw new IllegalArgumentException("duplicate function-id detected\n" + msg);
      }

      return methods.stream()
          .collect(Collectors.toMap(method -> method.getAnnotation(Export.class).value(), FunctionDef::new));
    });
  }

  static class Manager {
    private final Executor threads;
    private final Map<String, ServiceInvoker> services;

    Manager(@Nonnull Executor threads, @Nonnull Map<String, Object> services) {
      this.threads = threads;
      this.services = services.entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getKey, e -> new ServiceInvoker(e.getKey(), e.getValue())));
    }

    @Nullable
    public Object invoke(@Nonnull Pipe pipe, short functionId, @Nonnull Object[] args){

    }

  }
}
