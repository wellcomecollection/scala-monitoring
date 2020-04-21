package uk.ac.wellcome.monitoring.cloudwatch

import java.time.Instant

import akka.stream.scaladsl.{Flow, Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{Materializer, OverflowStrategy, ThrottleMode}
import akka.{Done, NotUsed}
import grizzled.slf4j.Logging
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.{MetricDatum, PutMetricDataRequest, StandardUnit}
import uk.ac.wellcome.monitoring.{Metrics, MetricsConfig}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class CloudWatchMetrics(cloudWatchClient: CloudWatchClient,
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
        PutMetricDataRequest.builder().namespace(metricsConfig.namespace)
          .metricData(metricDataSeq: _*).build()
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
    val metricDatum = MetricDatum.builder()
      .metricName(metricName)
      .value(1.0)
      .unit(StandardUnit.COUNT)
      .timestamp(Instant.now()).build()

    sourceQueue.offer(metricDatum).map { _ =>
      ()
    }
  }

  override def recordValue(metricName: String,
                           value: Double,
                           maybeUnit: Option[StandardUnit]): Future[Unit] = {
    val metricDatumBuilder = MetricDatum.builder()
      .metricName(metricName)
      .value(value)
      .timestamp(Instant.now())

    val metricDatum = maybeUnit.fold(metricDatumBuilder.build())(unit => metricDatumBuilder.unit(unit).build())

    sourceQueue.offer(metricDatum).map { _ =>
      ()
    }
  }
}
