package uk.ac.wellcome.monitoring

import scala.language.higherKinds

trait Metrics[F[_], MetricUnit] {
  def incrementCount(metricName: String): F[Unit]
  def recordValue(
    metricName: String,
    value: Double,
    maybeUnit: Option[MetricUnit] = None
  ): F[Unit]
}
