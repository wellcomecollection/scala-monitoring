package uk.ac.wellcome.monitoring.fixtures

import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import org.mockito.Matchers.anyString
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.scalatest.mockito.MockitoSugar
import uk.ac.wellcome.akka.fixtures.Akka
import uk.ac.wellcome.fixtures._
import uk.ac.wellcome.monitoring.{MetricsConfig, MetricsSender}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MetricsSenderFixture
    extends CloudWatch
    with Akka
    with MockitoSugar {

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

  def withMockMetricsSender[R](testWith: TestWith[MetricsSender, R]): R = {
    val metricsSender = mock[MetricsSender]

    when(
      metricsSender.incrementCount(anyString())
    ).thenAnswer((invocation: InvocationOnMock) => {
      Future.successful(())
    })

    testWith(metricsSender)
  }
}
