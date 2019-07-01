package uk.ac.wellcome.monitoring.typesafe

import akka.stream.ActorMaterializer
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.typesafe.config.Config
import uk.ac.wellcome.monitoring.cloudwatch.{CloudWatchMetrics, MetricsConfig}
import uk.ac.wellcome.typesafe.config.builders.EnrichConfig._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object MetricsBuilder {
  def buildMetricsConfig(config: Config): MetricsConfig = {
    val namespace =
      config.getOrElse[String]("aws.metrics.namespace")(default = "")
    val flushInterval = config.getOrElse[FiniteDuration](
      "aws.metrics.flushInterval")(default = 10 minutes)

    MetricsConfig(
      namespace = namespace,
      flushInterval = flushInterval
    )
  }

  private def buildMetricsSender(
    cloudWatchClient: AmazonCloudWatch,
    metricsConfig: MetricsConfig
  )(implicit
    materializer: ActorMaterializer,
    ec: ExecutionContext): CloudWatchMetrics =
    new CloudWatchMetrics(
      cloudWatchClient = cloudWatchClient,
      metricsConfig = metricsConfig
    )

  def buildMetricsSender(config: Config)(
    implicit
    materializer: ActorMaterializer,
    ec: ExecutionContext): CloudWatchMetrics =
    buildMetricsSender(
      cloudWatchClient = CloudWatchBuilder.buildCloudWatchClient(config),
      metricsConfig = buildMetricsConfig(config)
    )
}
