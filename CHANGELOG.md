# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [2.0.0] - 2017-09-19
- Upgrade to SBT 1.0/Scala 2.12
- Thanks to [Jeff Wilde](https://github.com/jeffwilde) for the PR!

## [1.5.5] - 2017-03-08
- Fix Windows bugs caused by Windows paths

## [1.5.4] - 2017-02-27
- Enable different settings for different configurations

## [1.5.3] - 2017-02-02
- Update to new URL scheme for DynamoDB Local jar

## [1.5.2] - 2017-01-19
- Fix PidUtil class collision when using multiple localytics/sbt-* projects

## [1.5.1] - 2017-01-17
- Fix #26 - bug in shutdown

## [1.5.0] - 2016-12-13
- Refactor to best practices and SBT 1.0 syntax

## [1.4.3] - 2016-11-10
- Use new syntax to remove warnings in sbt 0.13.13. Thanks @philwills.

## [1.4.2] - 2016-08-15
- Skip unnecessary untargz
- Verify gz and jar files during deploy stage

## [1.4.1] - 2016-05-03
- Ensure we kill the right DynamoDB process

## [1.4.0] - 2016-03-09
- Refactor cleanup after tests to work with `testOnly`

## [1.3.1] - 2016-03-09
- Fail if the tar.gz file is corrupt

## [1.3.0] - 2016-02-22
- Add HeapSize configuration

## [1.2.1] - 2016-01-26
- Fix broken task names

## [1.2] - 2016-01-12
- Make lifecycle management explict and easy to use outside of the default test config
- Make use of autoplugin to automatically import keys and settings
- Converted packages to com.localtyics
- Support test configuration where DynamoDB was never started ([grahamar/sbt-dynamodb#8](https://github.com/grahamar/sbt-dynamodb/pull/8))
- adds -sharedDB flag ([grahamar/sbt-dynamodb#7](https://github.com/grahamar/sbt-dynamodb/pull/7))
- Do not download the "latest" jar on every test run ([grahamar/sbt-dynamodb#4](https://github.com/grahamar/sbt-dynamodb/pull/4))

## [1.1] - 2015-03-10
- Improved Cygwin/Windows support
- Killing PID with 'Taskkill' in Windows environment
- Downloading DynamoDB from S3 only when file does not exist locally or version set to latest
- Additional log output

## [1.0] - 2015-01-20
