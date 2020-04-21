package uk.ac.wellcome.monitoring.cloudwatch

import java.net.URI

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient

object CloudWatchClientFactory {
  def create(region: String, endpoint: Option[URI]): CloudWatchClient = {
    val standardClient = CloudWatchClient.builder()
    endpoint match {
      case None =>
        standardClient
          .region(Region.of(region))
          .build()
      case Some(e) =>
        standardClient
          .endpointOverride(e)
          .build()
    }
  }
}
