package uk.ac.wellcome.monitoring

/** Should be thrown to indicate any exception which is "recognised" -- that
  * is, the cause is understood.
  *
  * Instances of this exception are counted as a separate metric.
  */
@deprecated(
  "This is only used for control flow in MetricsSender.count, which is deprecated",
  "messaging 1.0")
case class RecognisedFailureException(e: Throwable)
    extends Exception(e.getMessage)
