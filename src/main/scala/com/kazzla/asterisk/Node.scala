/*
 * Copyright (c) 2013 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package com.kazzla.asterisk

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import org.slf4j.LoggerFactory
import java.net.SocketAddress
import com.kazzla.asterisk.netty.Netty
import com.kazzla.asterisk.codec.{MsgPackCodec, Codec}
import javax.net.ssl.SSLContext
import scala.concurrent.{ExecutionContext, Promise, Future}
import scala.util.Failure
import scala.util.Success
import java.util.concurrent.Executors

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Node
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
class Node private[Node](name:String, initService:Service, bridge:Bridge, codec:Codec){
	import Node._

	@volatile
	private[this] var _service = initService

	private[this] val servers = new AtomicReference(Seq[Server]())
	private[this] val sessions = new AtomicReference(Seq[Session]())

	val onConnect = new EventHandlers[Session]()

	def service_=(newService:Service):Service = {
		val old = _service
		_service = newService
		old
	}

	private[this] val messagePump = Executors.newSingleThreadExecutor()

	// ==============================================================================================
	// 接続受け付けの開始
	// ==============================================================================================
	/**
	 * リモートのノードからの接続を受け付けます。
	 * @param address バインドアドレス
	 * @param tls 通信に使用する SSLContext
	 * @param onAccept 接続を受け付けた時に実行する処理
	 * @return Server の Future
	 */
	def listen(address:SocketAddress, tls:Option[SSLContext] = None)(implicit onAccept:(Session)=>Unit = {_ => None}):Future[Server] = {
		import ExecutionContext.Implicits.global
		val promise = Promise[Server]()
		bridge.listen(codec, address, tls){ wire => onAccept(bind(wire)) }.onComplete {
			case Success(server) =>
				add(servers, server)
				promise.success(new Server(server.address){
					override def close(){
						remove(servers, server)
						server.close()
					}
				})
			case Failure(ex) => promise.failure(ex)
		}
		promise.future
	}

	// ==============================================================================================
	// ノードへの接続
	// ==============================================================================================
	/**
	 * 指定されたアドレスの別のノードへ接続を行います。
	 * @param address 接続するノードのアドレス
	 * @param tls 通信に使用する SSLContext
	 * @return 接続により発生した Session の Future
	 */
	def connect(address:SocketAddress, tls:Option[SSLContext] = None):Future[Session] = {
		import ExecutionContext.Implicits.global
		val promise = Promise[Session]()
		bridge.connect(codec, address, tls).onComplete{
			case Success(wire) => promise.success(bind(wire))
			case Failure(ex) => promise.failure(ex)
		}
		promise.future
	}

	// ==============================================================================================
	// セッションの構築
	// ==============================================================================================
	/**
	 * 指定された Wire 上で新規のセッションを構築しメッセージング処理を開始します。
	 * このメソッドを使用することで `listen()`, `connect()` によるネットワーク以外の `Wire` 実装を使用すること
	 * が出来ます。
	 * @param wire セッションに結びつける Wire
	 * @return 新規セッション
	 */
	def bind(wire:Wire):Session = {
		logger.trace(s"bind($wire):$name")
		val s = new Session(s"$name[${wire.peerName}]", _service, wire, messagePump)
		add(sessions, s)
		s.onClosed ++ { session => remove(sessions, session) }
		s
	}

	// ==============================================================================================
	// ノードのシャットダウン
	// ==============================================================================================
	/**
	 * このノード上でアクティブなすべてのサーバ及びセッションがクローズされます。
	 */
	def shutdown():Unit = {
		messagePump.shutdown()
		servers.get().foreach{ _.close() }
		sessions.get().foreach{ _.close() }
		logger.debug(s"shutting-down $name; all available ${sessions.get().size} sessions, ${servers.get().size} servers are closed")
	}

}

object Node {
	private[Node] val logger = LoggerFactory.getLogger(classOf[Node])

	def apply(name:String):Builder = new Builder(name)

	class Builder private[Node](name:String) {
		import ExecutionContext.Implicits.global
		private var service:Service = new Service {}
		private var bridge:Bridge = Netty
		private var codec:Codec = MsgPackCodec

		def bridge(bridge:Bridge):Builder = {
			this.bridge = bridge
			this
		}

		// ==============================================================================================
		//
		// ==============================================================================================
		/**
		 * ノードが接続を受け新しいセッションの発生した初期状態でリモートのピアに提供するサービスを指定します。
		 * このサービスはセッション構築後にセッションごとに変更可能です。
		 * @param service 初期状態のサービス
		 */
		def serve(service:Service):Builder = {
			this.service = service
			this
		}

		def codec(codec:Codec):Builder = {
			this.codec = codec
			this
		}

		def build():Node = new Node(name, service, bridge, codec)

	}


	@tailrec
	private[Node] def add[T](container:AtomicReference[Seq[T]], element:T):Unit = {
		val n = container.get()
		if(! container.compareAndSet(n, n.+:(element))){
			add(container, element)
		}
	}

	@tailrec
	private[Node] def remove[T](container:AtomicReference[Seq[T]], element:T):Unit = {
		val n = container.get()
		if(! container.compareAndSet(n, n.filter{ _ != element })){
			remove(container, element)
		}
	}

}
