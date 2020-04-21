package uk.ac.wellcome.monitoring.typesafe

import java.net.URI

import akka.stream.Materializer
import com.typesafe.config.Config
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import uk.ac.wellcome.config.models.AWSClientConfig
import uk.ac.wellcome.monitoring.MetricsConfig
import uk.ac.wellcome.monitoring.cloudwatch.{CloudWatchClientFactory, CloudWatchMetrics}
import uk.ac.wellcome.monitoring.typesafe.MetricsBuilder.buildMetricsConfig
import uk.ac.wellcome.typesafe.config.builders.AWSClientConfigBuilder

import scala.concurrent.ExecutionContext

object CloudWatchBuilder extends AWSClientConfigBuilder {
  private def buildCloudWatchClient(
    awsClientConfig: AWSClientConfig): CloudWatchClient =
    CloudWatchClientFactory.create(
      region = awsClientConfig.region,
      endpoint = awsClientConfig.endpoint.map(new URI(_))
    )

  def buildCloudWatchClient(config: Config): CloudWatchClient =
    buildCloudWatchClient(
      awsClientConfig = buildAWSClientConfig(config, namespace = "cloudwatch")
    )

  private def buildCloudWatchMetrics(
    cloudWatchClient: CloudWatchClient,
    metricsConfig: MetricsConfig
  )(implicit
    materializer: Materializer,
    ec: ExecutionContext): CloudWatchMetrics =
    new CloudWatchMetrics(
      cloudWatchClient = cloudWatchClient,
      metricsConfig = metricsConfig
    )

  def buildCloudWatchMetrics(config: Config)(
    implicit
    materializer: Materializer,
    ec: ExecutionContext): CloudWatchMetrics =
    buildCloudWatchMetrics(
      cloudWatchClient = CloudWatchBuilder.buildCloudWatchClient(config),
      metricsConfig = buildMetricsConfig(config)
    )
}
