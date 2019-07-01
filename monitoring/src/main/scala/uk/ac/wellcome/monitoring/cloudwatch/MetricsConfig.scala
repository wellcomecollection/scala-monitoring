package uk.ac.wellcome.monitoring.cloudwatch

import scala.concurrent.duration.FiniteDuration

case class MetricsConfig(
  namespace: String,
  flushInterval: FiniteDuration
)
