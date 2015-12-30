# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
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
