package uk.ac.wellcome.monitoring

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.model.{PutMetricDataRequest, StandardUnit}
import org.mockito.ArgumentCaptor
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Assertion, FunSpec, Matchers}
import uk.ac.wellcome.monitoring.fixtures.MetricsSenderFixture

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.Random

class MetricsSenderTest
    extends FunSpec
    with MockitoSugar
    with Matchers
    with ScalaFutures
    with Eventually
    with IntegrationPatience
    with MetricsSenderFixture {

  import org.mockito.Mockito._

  it("counts a metric") {
    val amazonCloudWatch = mock[AmazonCloudWatch]
    withMetricsSender(amazonCloudWatch) { metricsSender =>
      val metricName = createMetricName

      val future = metricsSender.incrementCount(metricName)

      whenReady(future) { _ =>
        assertSingleDataPoint(amazonCloudWatch, metricName, maybeExpectedUnit = Some("Count"))
      }
    }
  }

  it("records a value metric") {
    val amazonCloudWatch = mock[AmazonCloudWatch]
    withMetricsSender(amazonCloudWatch) { metricsSender =>
      val metricName = createMetricName

      val future = metricsSender.recordValue(metricName, 10.0)

      whenReady(future) { _ =>
        assertSingleDataPoint(amazonCloudWatch, metricName, 10.0)
      }
    }
  }

  it("records a value metric with a unit") {
    val amazonCloudWatch = mock[AmazonCloudWatch]
    withMetricsSender(amazonCloudWatch) { metricsSender =>
      val metricName = createMetricName

      val future = metricsSender.recordValue(metricName, 11.0, Some(StandardUnit.Seconds))

      whenReady(future) { _ =>
        assertSingleDataPoint(amazonCloudWatch, metricName, 11.0, Some("Seconds"))
      }
    }
  }

  it("groups 20 MetricDatum into one PutMetricDataRequest") {
    val amazonCloudWatch = mock[AmazonCloudWatch]
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

          putMetricDataRequests.asScala.head.getMetricData should have size 20
          putMetricDataRequests.asScala.tail.head.getMetricData should have size 20
        }
      }
    }
  }

  it("takes at least one second to make 150 PutMetricData requests") {
    val amazonCloudWatch = mock[AmazonCloudWatch]
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

  private def createMetricName: String =
    (Random.alphanumeric take 10 mkString) toLowerCase

  private def assertSingleDataPoint(amazonCloudWatch: AmazonCloudWatch,
                                    metricName: String,
                                    expectedValue: Double = 1.0,
                                    maybeExpectedUnit: Option[String] = None): Assertion = {
    val capture = ArgumentCaptor.forClass(classOf[PutMetricDataRequest])
    eventually {
      verify(amazonCloudWatch, times(1)).putMetricData(capture.capture())

      val putMetricDataRequest = capture.getValue
      val metricData = putMetricDataRequest.getMetricData
      metricData should have size 1
      val metricDatum = metricData.asScala.head
      metricDatum.getValue shouldBe expectedValue
      maybeExpectedUnit.foreach { expectedUnit =>
        metricDatum.getUnit shouldBe expectedUnit
      }
      metricDatum.getMetricName shouldBe metricName
    }
  }
}
