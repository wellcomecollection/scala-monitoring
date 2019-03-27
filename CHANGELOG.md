# CHANGELOG

## v2.1.0 - 2019-03-27

adds an additional method 'recordValue' to MetricsSender that sends values to Cloudwatch.

## v2.0.0 - 2019-03-11

This release cleans up MetricsSender, strips out some deprecated methods and generally tidies up the library.

## v1.3.0 - 2019-02-08

This release adds the `monitoring_typesafe` library for configuring the `monitoring` library using Typesafe.

## v1.2.1 - 2019-02-05

Start using the scala-fixtures lib rather than vendoring fixtures.

## v1.2.0 - 2019-01-10

This release removes Guice from the dependency list.

## v1.1.1 - 2018-07-31

This patch changes `MetricsSenderTest` to use IntegrationPatience like our
other tests, rather than unncessarily setting custom PatienceConfiguration.

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
