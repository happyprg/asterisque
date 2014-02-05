Asterisk\*
========

Asterisk\* is lightweight bidirectional RPC with asynchronous messaging framework, protocol and implementation.

Documents
=========

* [Motive](http://prezi.com/ia6rjvjrhe6d/asterisk-motivation/)
* [Brain Storming](http://prezi.com/ktjdnfshx8dv/asterisk-brain-storming/)
* [Introduction](docs/introduction.md)

Getting Started
===============

To build asterisk\* JAR library, you may clone asterisk\* GitHub repository and build `asterisk_2.10-0.1.jar` by
`sbt package`.

```sh
$ git clone https://github.com/torao/asterisk.git
$ cd asterisk
$ ./sbt package
```

Or, you can also run directory following sample code of bi-directional RPC by using `sbt run`.

```scala
import com.kazzla.asterisk._
import java.net.InetSocketAddress
import scala.concurrent.{Await,Future,Promise}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util._
// 1) Define scala traits or java interfaces as IDL, those every methods
//    has @Export(function-id) and Future return type to agree strict interface
//    statically between client and server.
trait Sample1 {
  @Export(10)
  def greeting(name:String):Future[String]
}
trait Sample2 {
  @Export(10)
  def surround(text:String):Future[String]
}
// 2) Implementation class is used as remote service on server node.
class Sample1Impl extends Service with Sample1 {
  def greeting(name:String) = Session() match {
    case Some(session) =>
      val promise = Promise[String]()
      session.bind(classOf[Sample2]).surround(name).onComplete {
        case Success(str) => promise.success(s"hello, $str")
        case Failure(ex) => promise.failure(ex)
      }
      promise.future
    case None => Promise.failed(new Exception()).future
  }
}
class Sample2Impl extends Service with Sample2 {
  def surround(text:String) = Promise.successful(s"*$text*").future
}
object SampleImpl {
  // 3) Instantiate client and server nodes that use Netty as messenger bridge.
  val node1 = Node("node 1").serve(new Sample1Impl).bridge(netty.Netty).build()
  val node2 = Node("node 2").serve(new Sample2Impl).bridge(netty.Netty).build()
  def close() = Seq(node1,node2).foreach{ _.shutdown() }
  def main(args:Array[String]):Unit = {
    // 4) The server listen on port 9999 without any action in accept.
    node1.listen(new InetSocketAddress(9999)){ _ => None }
    // 5) Client retrieve `Future[Session]` by connecting to server port 9999.
    val future = node2.connect(new InetSocketAddress(9999))
    val session = Await.result(future, Duration.Inf)
    // 6) Bind remote interfaces from client session.
    val sample = session.bind(classOf[Sample1])
    // 7) Call remote procedure and action asynchronously.
    sample.greeting("asterisk").onComplete {
      case Success(str) =>
        println(str)
        close()
      case Failure(ex) =>
        ex.printStackTrace()
        close()
    }
  }
}
```

```sh
$ ./sbt run
[info] Set current project to asterisk (in build file:/Users/torao/git/asterisk/)
[info] Compiling 1 Scala source to /Users/torao/git/asterisk/target/scala-2.10/classes...
[info] Running SampleImpl
hello, *asterisk*
[success] Total time: 11 s, completed 2014/02/03 5:53:31
```

License
=======
All sources and related resources are available [Apache License Version 2.0](LICENSE).