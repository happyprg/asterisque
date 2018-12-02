package io.asterisque.core.service;

import io.asterisque.core.codec.JavaTypeVariableCodec;
import io.asterisque.core.codec.MessageCodec;
import io.asterisque.core.codec.TypeVariableCodec;
import io.asterisque.core.msg.SyncConfig;
import io.asterisque.core.wire.Bridge;
import io.asterisque.core.wire.Server;
import io.asterisque.core.wire.Wire;
import io.asterisque.core.wire.netty.NettyBridge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node implements AutoCloseable {
  private final UUID id;
  private final List<Server> servers = new ArrayList<>();
  private final ServiceInvoker.Manager services;
  private final Bridge bridge;
  private final MessageCodec codec;
  private final TypeVariableCodec conversion;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  /**
   * インスタンスは {@link Builder#build()} によって構築されます。
   */
  private Node(@Nonnull UUID id, @Nonnull Map<String, Object> services,
               @Nonnull Bridge bridge, @Nonnull MessageCodec codec, @Nonnull TypeVariableCodec conversion) {
    this.id = id;
    this.services = new ServiceInvoker.Manager(services);
    this.bridge = bridge;
    this.codec = codec;
    this.conversion = conversion;
  }

  @Nonnull
  public UUID id() {
    return id;
  }

  @Nonnull
  public CompletableFuture<Void> bind(@Nonnull URI uri, @Nonnull String subprotocol) {
    return bind(uri, subprotocol, null, 0xFFFF, 0xFFFF);
  }

  @Nonnull
  public CompletableFuture<Void> bind(@Nonnull URI uri, @Nonnull String subprotocol, @Nullable SSLContext sslContext,
                                      int inboundQueueSize, int outbountQueueSize) {
    ensureNotClosed();
    return bridge.newServer(uri, subprotocol, inboundQueueSize, outbountQueueSize, sslContext, this::accept)
        .thenApply(server -> {
          ensureNotClosed(server::close);
          synchronized (this.servers) {
            this.servers.add(server);
          }
          return null;
        });
  }

  public void close() {
    if (closed.compareAndSet(false, true)) {
      synchronized (servers) {
        servers.forEach(Server::close);
        servers.clear();
      }
    }
  }

  private void accept(@Nonnull CompletableFuture<Wire> future) {
    future.thenApply(wire -> {
      UUID sessionId = UUID.randomUUID();
      long utcMillis = System.currentTimeMillis();
      SyncConfig sync = new SyncConfig(id(), sessionId, utcMillis, );
      wire.outbound.offer();
      wire.inbound.poll()
    })
  }

  @Nonnull
  private CompletableFuture<Session> connect(@Nonnull Wire wire) {

  }

  private void ensureNotClosed() {
    ensureNotClosed(() -> {
    });
  }

  private void ensureNotClosed(@Nonnull Runnable ifClosed) {
    if (!closed.get()) {
      ifClosed.run();
    } else {
      throw new IllegalStateException("node has already been wsClosed");
    }
  }

  /**
   * {@link Node} インスタンスを生成するためのビルダー。
   */
  public static class Builder {
    private final UUID id;

    @Nonnull
    private Map<String, Object> services = new HashMap<>();

    @Nonnull
    private MessageCodec codec = MessageCodec.MessagePackCodec;

    @Nonnull
    private TypeVariableCodec typeCodec = new JavaTypeVariableCodec();

    @Nonnull
    private Bridge bridge = new NettyBridge();

    public Builder(@Nonnull UUID id) {
      this.id = id;
    }

    @Nonnull
    public Node build() {
      return new Node(id, services, bridge, codec, typeCodec);
    }

    @Nonnull
    public Builder service(@Nonnull String id, @Nonnull Object service) {
      this.services.put(id, service);
      return this;
    }

    @Nonnull
    public Builder codec(@Nonnull MessageCodec codec) {
      this.codec = codec;
      return this;
    }

    @Nonnull
    public Builder conversion(@Nonnull TypeVariableCodec conversion) {
      this.typeCodec = conversion;
      return this;
    }

    @Nonnull
    public Builder bridge(@Nonnull Bridge bridge) {
      this.bridge = bridge;
      return this;
    }

  }
}