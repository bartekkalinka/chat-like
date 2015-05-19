# chat-like
Self-educational, hello-world-level basic chat-like stdin broadcasting with akka clustering

# running
Needs java 7 to run, scala/sbt to build

First, in application.conf you need to edit: seed-nodes = ["akka.tcp://ChatLikeSystem@192.168.52.8:2552"] - enter ip and port you're going to use for seed node.

To run with sbt: sbt "run [port]" (use above port for first node/JVM so you strat seed node first).

To run on machine with no sbt/scala, just with JRE 7, first build it with "sbt assembly", then copy ChatLike-assembly-1.0.jar to target host and run with "java -jar ChatLike-assembly-1.0.jar [port]".