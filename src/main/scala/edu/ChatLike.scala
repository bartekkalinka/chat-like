package local

import akka.actor._
import akka.cluster._
import com.typesafe.config._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent._
import akka.pattern.ask
import akka.actor.PoisonPill

case class LocalInput(msg: String)
case class Broadcast(msg: String)
object MayExit

object ChatLike {

  def main(args: Array[String]): Unit = {
    val config = getConfig(args)

    implicit val system = ActorSystem("ChatLikeSystem", config)
    val chatActor = system.actorOf(Props[ChatActor], name = "ChatActor")  // the local actor

    for (ln <- io.Source.stdin.getLines.takeWhile(_ != "exit")) processLine(chatActor, ln)

    exitSequence(system, chatActor)

    system.shutdown()
  }

  private def exitSequence(system: ActorSystem, chatActor: ActorRef) = {
    val cluster = Cluster(system)
    cluster.leave(cluster.selfAddress)

    implicit val timeout = Timeout(5 seconds)
    while(!Await.result(chatActor ? MayExit, timeout.duration).asInstanceOf[Boolean]) Thread.sleep(50)

    chatActor ! PoisonPill
  }

  private def processLine(chatActor: ActorRef, ln: String) {
    chatActor ! LocalInput(ln)
  }

  private def getConfig(args: Array[String]): Config  = {
    val port = if (args.isEmpty) "0" else args(0)
    ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").withFallback(ConfigFactory.load())
  }
}


