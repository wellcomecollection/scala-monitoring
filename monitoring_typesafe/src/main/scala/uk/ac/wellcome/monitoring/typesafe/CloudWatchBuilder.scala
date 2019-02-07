package uk.ac.wellcome.monitoring.typesafe

import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.typesafe.config.Config
import uk.ac.wellcome.config.models.AWSClientConfig
import uk.ac.wellcome.monitoring.CloudWatchClientFactory
import uk.ac.wellcome.typesafe.config.builders.AWSClientConfigBuilder

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
}
