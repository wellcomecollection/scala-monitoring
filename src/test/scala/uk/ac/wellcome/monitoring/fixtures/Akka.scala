package uk.ac.wellcome.monitoring.fixtures

import akka.actor.ActorSystem
import org.scalatest.concurrent.Eventually
import uk.ac.wellcome.fixtures._

trait Akka extends Eventually {
  private[monitoring] def withMonitoringActorSystem[R] = fixture[ActorSystem, R](
    create = ActorSystem(),
    destroy = eventually { _.terminate() }
  )
}
