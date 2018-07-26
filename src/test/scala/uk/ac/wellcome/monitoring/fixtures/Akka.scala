package uk.ac.wellcome.monitoring.fixtures

import akka.actor.ActorSystem
import org.scalatest.concurrent.Eventually

trait Akka extends Eventually {
  private[monitoring] def withActorSystem[R] = fixture[ActorSystem, R](
    create = ActorSystem(),
    destroy = eventually { _.terminate() }
  )
}
