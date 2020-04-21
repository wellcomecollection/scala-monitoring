package uk.ac.wellcome.monitoring.cloudwatch

import java.time.Instant
import java.time.temporal.ChronoUnit

import org.mockito.ArgumentCaptor
import org.scalatest.Assertion
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.{PutMetricDataRequest, StandardUnit}
import uk.ac.wellcome.akka.fixtures.Akka
import uk.ac.wellcome.fixtures.TestWith
import uk.ac.wellcome.monitoring.MetricsConfig

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Random

class CloudWatchMetricsTest
    extends AnyFunSpec
    with MockitoSugar
    with Matchers
    with ScalaFutures
    with Eventually
    with Akka
    with IntegrationPatience {

  import org.mockito.Mockito._

  it("counts a metric") {
    val amazonCloudWatch = mock[CloudWatchClient]
    withMetricsSender(amazonCloudWatch) { metricsSender =>
      val metricName = createMetricName

      val future = metricsSender.incrementCount(metricName)

      whenReady(future) { _ =>
        assertSingleDataPoint(amazonCloudWatch, metricName, maybeExpectedUnit = Some(StandardUnit.COUNT))
      }
    }
  }

  it("records a value metric") {
    val amazonCloudWatch = mock[CloudWatchClient]
    withMetricsSender(amazonCloudWatch) { metricsSender =>
      val metricName = createMetricName

      val future = metricsSender.recordValue(metricName, 10.0)

      whenReady(future) { _ =>
        assertSingleDataPoint(amazonCloudWatch, metricName, 10.0)
      }
    }
  }

  it("records a value metric with a unit") {
    val amazonCloudWatch = mock[CloudWatchClient]
    withMetricsSender(amazonCloudWatch) { metricsSender =>
      val metricName = createMetricName

      val future = metricsSender.recordValue(metricName, 11.0, Some(StandardUnit.SECONDS))

      whenReady(future) { _ =>
        assertSingleDataPoint(amazonCloudWatch, metricName, 11.0, Some(StandardUnit.SECONDS))
      }
    }
  }

  it("groups 20 MetricDatum into one PutMetricDataRequest") {
    val amazonCloudWatch = mock[CloudWatchClient]
    withMetricsSender(amazonCloudWatch) { metricsSender =>
      val capture = ArgumentCaptor.forClass(classOf[PutMetricDataRequest])

      val metricName = createMetricName

      val futures =
        (1 to 40).map { _ => metricsSender.incrementCount(metricName) }

      whenReady(Future.sequence(futures)) { _ =>
        eventually {
          verify(amazonCloudWatch, times(2)).putMetricData(capture.capture())

          val putMetricDataRequests = capture.getAllValues
          putMetricDataRequests should have size 2

          putMetricDataRequests.asScala.head.metricData() should have size 20
          putMetricDataRequests.asScala.tail.head.metricData() should have size 20
        }
      }
    }
  }

  it("takes at least one second to make 150 PutMetricData requests") {
    val amazonCloudWatch = mock[CloudWatchClient]
    withMetricsSender(amazonCloudWatch) { metricsSender =>
      val capture = ArgumentCaptor.forClass(classOf[PutMetricDataRequest])

      val metricName = createMetricName

      val expectedDuration = (1 second).toMillis
      val startTime = Instant.now

      // Each PutMetricRequest is made of 20 MetricDatum so we need
      // 20 * 150 = 3000 calls to incrementCount to get 150 PutMetricData calls
      val futures =
        (1 to 3000).map { i => metricsSender.incrementCount(s"${i}_$metricName") }

      val promisedInstant = Promise[Instant]

      whenReady(Future.sequence(futures)) { _ =>
        eventually {
          verify(amazonCloudWatch, times(150))
            .putMetricData(capture.capture())

          val putMetricDataRequests = capture.getAllValues

          putMetricDataRequests should have size 150

          promisedInstant.success(Instant.now())
        }
      }

      whenReady(promisedInstant.future) { endTime =>
        val gap: Long = ChronoUnit.MILLIS.between(startTime, endTime)
        gap shouldBe >(expectedDuration)
      }
    }
  }

  // TODO: This should use RandomGenerators
  private def createMetricName: String =
    (Random.alphanumeric take 10 mkString) toLowerCase

  private def withMetricsSender[R](
    cloudWatchClient: CloudWatchClient)(
    testWith: TestWith[CloudWatchMetrics, R]): R =
    withActorSystem { actorSystem =>
      withMaterializer(actorSystem) { implicit materializer =>
        val metricsSender = new CloudWatchMetrics(
          cloudWatchClient = cloudWatchClient,
          metricsConfig = MetricsConfig(
            namespace = "test",
            flushInterval = 1 second
          )
        )

        testWith(metricsSender)
      }
    }

  private def assertSingleDataPoint(amazonCloudWatch: CloudWatchClient,
                                    metricName: String,
                                    expectedValue: Double = 1.0,
                                    maybeExpectedUnit: Option[StandardUnit] = None): Assertion = {
    val capture = ArgumentCaptor.forClass(classOf[PutMetricDataRequest])
    eventually {
      verify(amazonCloudWatch, times(1)).putMetricData(capture.capture())

      val putMetricDataRequest = capture.getValue
      val metricData = putMetricDataRequest.metricData()
      metricData should have size 1
      val metricDatum = metricData.asScala.head
      metricDatum.value() shouldBe expectedValue
      maybeExpectedUnit.foreach { expectedUnit =>
        metricDatum.unit() shouldBe expectedUnit
      }
      metricDatum.metricName() shouldBe metricName
    }
  }
}
