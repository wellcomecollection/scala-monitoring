package uk.ac.wellcome.monitoring.typesafe

import akka.stream.ActorMaterializer
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.typesafe.config.Config
import uk.ac.wellcome.config.models.AWSClientConfig
import uk.ac.wellcome.monitoring.MetricsConfig
import uk.ac.wellcome.monitoring.cloudwatch.{
  CloudWatchClientFactory,
  CloudWatchMetrics
}
import uk.ac.wellcome.monitoring.typesafe.MetricsBuilder.buildMetricsConfig
import uk.ac.wellcome.typesafe.config.builders.AWSClientConfigBuilder

import scala.concurrent.ExecutionContext

object CloudWatchBuilder extends AWSClientConfigBuilder {
  private def buildCloudWatchClient(
    awsClientConfig: AWSClientConfig): AmazonCloudWatch =
    CloudWatchClientFactory.create(
      region = awsClientConfig.region,
      endpoint = awsClientConfig.endpoint.getOrElse("")
    )

  def buildCloudWatchClient(config: Config): AmazonCloudWatch =
    buildCloudWatchClient(
      awsClientConfig = buildAWSClientConfig(config, namespace = "cloudwatch")
    )

  private def buildCloudWatchMetrics(
    cloudWatchClient: AmazonCloudWatch,
    metricsConfig: MetricsConfig
  )(implicit
    materializer: ActorMaterializer,
    ec: ExecutionContext): CloudWatchMetrics =
    new CloudWatchMetrics(
      cloudWatchClient = cloudWatchClient,
      metricsConfig = metricsConfig
    )

  def buildCloudWatchMetrics(config: Config)(
    implicit
    materializer: ActorMaterializer,
    ec: ExecutionContext): CloudWatchMetrics =
    buildCloudWatchMetrics(
      cloudWatchClient = CloudWatchBuilder.buildCloudWatchClient(config),
      metricsConfig = buildMetricsConfig(config)
    )
}
