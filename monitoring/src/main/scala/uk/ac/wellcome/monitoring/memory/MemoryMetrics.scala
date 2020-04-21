package uk.ac.wellcome.monitoring.memory

import uk.ac.wellcome.monitoring.Metrics

import scala.concurrent.Future

class MemoryMetrics[MetricUnit]() extends Metrics[Future, MetricUnit] {
  var incrementedCounts: Seq[String] = Seq.empty

  override def incrementCount(metricName: String): Future[Unit] = {
    incrementedCounts = incrementedCounts :+ metricName
    Future.successful(())
  }

  var recordedValues: Seq[(String, Double, Option[MetricUnit])] = Seq.empty

  override def recordValue(metricName: String,
                           value: Double,
                           maybeUnit: Option[MetricUnit]): Future[Unit] = {
    recordedValues = recordedValues :+ ((metricName, value, maybeUnit))
    Future.successful(())
  }
}
