package uk.ac.wellcome.monitoring.cloudwatch

import java.util.Date

import akka.stream.scaladsl.{Flow, Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{Materializer, OverflowStrategy, ThrottleMode}
import akka.{Done, NotUsed}
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.model._
import grizzled.slf4j.Logging
import uk.ac.wellcome.monitoring.{Metrics, MetricsConfig}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class CloudWatchMetrics(cloudWatchClient: AmazonCloudWatch,
                        metricsConfig: MetricsConfig)(
                         implicit mat: Materializer,
                         ec: ExecutionContext)
    extends Metrics[Future, StandardUnit]
    with Logging {

  // According to https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_limits.html
  // PutMetricData supports a maximum of 20 MetricDatum per PutMetricDataRequest.
  // The maximum number of PutMetricData requests is 150 per second.
  private val metricDataListMaxSize = 20
  private val maxPutMetricDataRequestsPerSecond = 150

  val sink: Sink[Seq[MetricDatum], Future[Done]] = Sink.foreach(
    metricDataSeq =>
      cloudWatchClient.putMetricData(
        new PutMetricDataRequest()
          .withNamespace(metricsConfig.namespace)
          .withMetricData(metricDataSeq: _*)
    ))

  val source: Source[MetricDatum, SourceQueueWithComplete[MetricDatum]] =
    Source
      .queue[MetricDatum](
        bufferSize = 5000,
        overflowStrategy = OverflowStrategy.backpressure
      )

  val materializer: Flow[MetricDatum, immutable.Seq[MetricDatum], NotUsed] =
    Flow[MetricDatum]
      .groupedWithin(metricDataListMaxSize, metricsConfig.flushInterval)

  val sourceQueue: SourceQueueWithComplete[MetricDatum] =
    source
      .viaMat(materializer)(Keep.left)
      // Make sure we don't exceed aws rate limit
      .throttle(
        elements = maxPutMetricDataRequestsPerSecond,
        per = 1 second,
        maximumBurst = 0,
        mode = ThrottleMode.shaping
      )
      .to(sink)
      .run()

  override def incrementCount(metricName: String): Future[Unit] = {
    val metricDatum = new MetricDatum()
      .withMetricName(metricName)
      .withValue(1.0)
      .withUnit(StandardUnit.Count)
      .withTimestamp(new Date())

    sourceQueue.offer(metricDatum).map { _ =>
      ()
    }
  }

  override def recordValue(metricName: String,
                           value: Double,
                           maybeUnit: Option[StandardUnit]): Future[Unit] = {
    val metricDatum = new MetricDatum()
      .withMetricName(metricName)
      .withValue(value)
      .withTimestamp(new Date())

    maybeUnit.foreach(metricDatum.setUnit)

    sourceQueue.offer(metricDatum).map { _ =>
      ()
    }
  }
}
