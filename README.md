# scala-monitoring

[![Build Status](https://travis-ci.org/wellcometrust/scala-monitoring.svg?branch=master)](https://travis-ci.org/wellcometrust/scala-monitoring)

Monitoring libraries in use at Wellcome:

-   `MetricsSender`: A wrapper for sending CloudWatch metrics.

This library is used as part of the [Wellcome Digital Platform][platform].

[platform]: https://github.com/wellcometrust/platform

## Installation

This library is only published to a private S3 bucket.

Wellcome projects have access to this S3 bucket -- you can use our build
scripts to publish a copy to your own private package repository, or vendor
the library by copying the code into your own repository.

Read [the changelog](CHANGELOG.md) to find the latest version.
