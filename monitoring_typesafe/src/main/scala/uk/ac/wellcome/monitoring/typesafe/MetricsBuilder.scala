package uk.ac.wellcome.monitoring.typesafe

import com.typesafe.config.Config
import uk.ac.wellcome.monitoring.MetricsConfig
import uk.ac.wellcome.typesafe.config.builders.EnrichConfig._

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
}
