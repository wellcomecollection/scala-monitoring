# CHANGELOG

## v1.1.0 - 2018-07-27

This add three new methods to MetricsSender that can directly decide which
metric to increment:

```
def countSuccess(metricName: String): Future[QueueOfferResult]

def countRecognisedFailure(metricName: String): Future[QueueOfferResult]

def countFailure(metricName: String): Future[QueueOfferResult]
```

This release also deprecates `MetricsSender.count`, as its use in the platform
is being replaced in favour of the specific methods above.

Alongside it, `RecognisedFailureException` is deprecated as it was only
introduced in v1.0.0 for control flow in `count`, and nothing is using it yet.

## v1.0.1 - 2018-07-26

This renames the internal helper `withActorSystem` to
`withMonitoringActorSystem` to avoid clashes with the helper in the main
platform repo.

## v1.0.0 - 2018-07-26

Initial release of the separate monitoring library.
