package uk.ac.wellcome.monitoring.fixtures

import akka.actor.ActorSystem
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import grizzled.slf4j.Logging
import org.scalatest.mockito.MockitoSugar
import uk.ac.wellcome.fixtures._
import uk.ac.wellcome.monitoring.{MetricsConfig, MetricsSender}

trait MetricsSenderFixture
    extends Logging
    with MockitoSugar
    with CloudWatch
    with Akka {

  val QUEUE_RETRIES = 3

  def withMetricsSender[R](
    actorSystem: ActorSystem,
    amazonCloudWatch: AmazonCloudWatch = cloudWatchClient): Fixture[MetricsSender, R] =
    fixture[MetricsSender, R](
      create = {
        val metricsSender = new MetricsSender(
          amazonCloudWatch = amazonCloudWatch,
          actorSystem = actorSystem,
          metricsConfig = MetricsConfig(
            namespace = awsNamespace,
            flushInterval = flushInterval
          )
        )
        metricsSender
      }
    )
}
