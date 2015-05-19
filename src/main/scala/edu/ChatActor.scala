package local

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._

class ChatActor extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  var removedFromCluster = false

  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }
  override def postStop(): Unit = cluster.unsubscribe(self)  

  // references to remote actors
  var memberAddresses: List[Address] = Nil

  def receive = {
    //cluster section
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      memberAddresses ::= member.address
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
      val (pref, suff) = memberAddresses.span(_ != member.address)
      memberAddresses = pref ::: suff.tail
      if(member.address == cluster.selfAddress) {
        removedFromCluster = true
      }
    case _: MemberEvent =>
    //TODO separate app logic from remotes list maintenance 
    //application section
    case LocalInput(msg) => 
      memberAddresses.foreach(getRef(_) ! Broadcast(msg))
    case Broadcast(msg) => 
      println("Broadcast: " + msg)
    //exit section
    case MayExit =>
      sender ! removedFromCluster
  }

  private def getRef(a: Address): ActorSelection = context.actorSelection(RootActorPath(a) / "user" / "ChatActor")
}