package uk.ac.wellcome.monitoring.fixtures

import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import uk.ac.wellcome.monitoring.CloudWatchClientFactory

import scala.concurrent.duration._

trait CloudWatch {
  protected val awsNamespace: String = "test"

  protected val localCloudWatchEndpointUrl: String = "http://localhost:4582"
  private val regionName: String = "eu-west-1"

  protected val flushInterval: FiniteDuration = 1 second

  def cloudWatchLocalFlags =
    Map(
      "aws.cloudWatch.endpoint" -> localCloudWatchEndpointUrl
    )

  val cloudWatchClient: AmazonCloudWatch =
    CloudWatchClientFactory.create(
      region = regionName,
      endpoint = localCloudWatchEndpointUrl
    )
}
