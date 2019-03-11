package uk.ac.wellcome.monitoring.fixtures

import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import uk.ac.wellcome.akka.fixtures.Akka
import uk.ac.wellcome.fixtures._
import uk.ac.wellcome.monitoring.{MetricsConfig, MetricsSender}

import scala.concurrent.ExecutionContext.Implicits.global

trait MetricsSenderFixture
    extends CloudWatch
    with Akka {

  def withMetricsSender[R](
    cloudWatchClient: AmazonCloudWatch = cloudWatchClient)(
    testWith: TestWith[MetricsSender, R]): R =
    withActorSystem { actorSystem =>
      withMaterializer(actorSystem) { implicit materializer =>
        val metricsSender = new MetricsSender(
          cloudWatchClient = cloudWatchClient,
          metricsConfig = MetricsConfig(
            namespace = awsNamespace,
            flushInterval = flushInterval
          )
        )

        testWith(metricsSender)
      }
    }
}
