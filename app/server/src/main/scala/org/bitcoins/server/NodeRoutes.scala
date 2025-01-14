package org.bitcoins.server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import org.bitcoins.core.api.node.NodeApi
import org.bitcoins.node.Node
import org.bitcoins.rpc.client.common.BitcoindRpcClient
import org.bitcoins.server.routes.{Server, ServerCommand, ServerRoute}

import scala.concurrent.duration.DurationInt

case class NodeRoutes(nodeApi: NodeApi)(implicit system: ActorSystem)
    extends ServerRoute {
  import system.dispatcher

  def handleCommand: PartialFunction[ServerCommand, Route] = {
    case ServerCommand("getpeers", _) =>
      complete {
        Server.httpSuccess("TODO implement getpeers")
      }

    case ServerCommand("stop", _) =>
      nodeApi match {
        case node: Node =>
          complete {
            val nodeStopping = node.stop().map { _ =>
              Server.httpSuccess("Node shutting down")
            }

            system.scheduler.scheduleOnce(7.seconds)(sys.exit())
            nodeStopping
          }
        case bitcoind: BitcoindRpcClient =>
          complete {
            val nodeStopping = bitcoind.stop().map { _ =>
              Server.httpSuccess("Node shutting down")
            }

            system.scheduler.scheduleOnce(7.seconds)(sys.exit())
            nodeStopping
          }
      }
  }
}
