# Gradle Kafka Connect

## Motivation

In my organization, several of us had varying versions of scripts (bash, python, ruby, etc...) to create and submit
Kafka Connector Configurations. We often shared these via Slack or Teams and NOBODY seemed to be sure what the correct
version of which connector was deployed where. (Having typed this into this document, the embarrassment I feel is indescribable.)

The first step of solving any problem is admitting there is one. So let's move on...

The goal of this plugin is to give users a way to manage Apache Kafka Connector configurations and 
submit the configurations to a given cluster via the Connect API.

## Prerequisites

* Gradle 6.7+
* Java 8+
* [jsonnet](https://jsonnet.org/)
  
  > **But why JSONNET?** Because I preferred the templating engine of jsonnet to other tools to merge json documents. At
  the time of writing this plugin, I can find no adequate Java/Groovy API for jsonnet (with good reason, I suppose).
  There is a Scala API, but I did not feel like addressing the typical interop issues I have seen in my experience with 
  Java calling Scala.
  > 
  > Think of the things that are environment-specific with regards to a Kafka Connector - number of tasks, database names,
  > hostnames, URLs, credentials (please don't commit these to your repo), etc... These belong in *.jsonnet files.
  > 
  > The rest is boilerplate - such as the name of the connector, the key and value converters, tranforms and their params, etc...
  > These would go in the *.libsonnet files that are referenced in *.jsonnet files using the import functionality.
  > 
  > It's pretty simple to install on MOST platforms (brew, apt-get, yum, etc...).
  > 
  > As for those concerned with "performance," this isn't some high-volume document processing system - it's a BUILD TOOL!
  > However, I will an issue for in Github in the hopes to refactor this functionality. Stay tuned.
  
## Tasks and Usage

Add to your project's `build.gradle` with a valid version number:

```groovy

// ...some gradle stuffs...

plugins {
    // ...other gradle stuffs...
    id 'com.github.sandonjacobs.gradle-kafka-connect' version '*.*.*'
    // ...more gradle stuffs...
}

// ...even more gradle stuffs...

kafkaConnect {
  // BTW: using the defaults here to illustrate this closure...
  connectEndpoint = 'http://localhost:8083'
  sourceBase = 'src/main'
  connectorSourceName = 'connectors'
  defaultConnectorSubDirectory = 'development'
  // if you wanna ignore sourceBase and connectorSourceName with a directory at the root of the project...
  // connectorSourcePath = 'some-connectors'
}

// enough with the gradle stuffs...

```

### Properties and Params

| Property Name | Description | Default Value |
| --- | --- | --- |
| connectEndpoint | HTTP url (with port) to the root of the Kafka Connect API. | http://localhost:8083 |
| connectorSourceName | Where are the connectors to be loaded from. | `connectors` |
| sourceBase | From root of project, the base of the `connectorSourceName` directory. | `src/main` |
| defaultConnectorSubDirectory | Used to determine which subdirectory to process. I typically use this to specify an enviroment (dev, qa, prod, etc...).  | `development`|
| connectorSourcePath | This will override the use of `connectorSourceName` and `sourceBase` if set. | |
| defaultHttpTimeoutMs | Default timeout (in ms) for http calls to kafka connect API. | 5000 |

### listConnectors

List the connectors that are running on a given cluster, 
using the [Connectors API](https://docs.confluent.io/platform/current/connect/references/restapi.html#connectors) endpoint.

#### Params
| Parameter | Description |
| --- | --- |
| connectorEndpoint | override the connector configuration endpoint url. |

#### Usage

#### Task Help
```shell
> ./gradlew help --task listConnectors
```

#### Default Usage
```shell
> ./gradlew listConnectors
```

#### Usage with Params
```shell
> ./gradlew listConnectors --connector-endpoint http://myconnect:8888
```

### submitConnectors

Parse, (jsonnet) merge, and (conditionally) submit the connectors from a project directory to a given cluster, 
using the [Connectors API](https://docs.confluent.io/platform/current/connect/references/restapi.html#connectors) endpoint.

#### Params
| Parameter | Description |
| --- | --- |
| connectorEndpoint | override the connector configuration endpoint url. |
| connectorSubDir | specify a subdirectory to run with, defaults to the value of `defaultConnectorSubDirectory` specified in the plugin closure.
| jsonnet-tla-str-args | Support for additional jsonnet args, maps to `--tla-str <var>[=<val>]`. |
| http-timeout | Used to override the `defaultHttpTimeoutMs` for the project. |

#### Task Help
```shell
> ./gradlew help --task submitConnectors
```

#### Default Usage
```shell
> ./gradlew submitConnectors
```

#### Submit to an Alternate Endpoint
```shell
> ./gradlew submitConnectors --connector-endpoint http://myconnect:8888
```

#### Only Print the Configurations
```shell
> ./gradlew submitConnectors --print-only
```

#### Process the Configurations in a Provided Subdirectory
```shell
> ./gradlew submitConnectors --connector-subdir prod
```

#### Print Only the Configurations in a Provided Subdirectory
```shell
> ./gradlew submitConnectors --print-only --connector-subdir prod
```

#### Print Only the Configurations in a Provided Subdirectory with tla-str Replacements

Suppose we have a connector that contains some value we want to bind late in the lifecycle - perhaps something only known
by the CI/CD environment or an environment variable.

Utilize the `tla-str` functionality when defining the jsonnet templates and then use the `jsonnet-tla-str-args` parameter
of this task to pass the values of those `tla-str` args to the template. If there are multiple `key=value` pairs (mapping to 
multiple arguments in the jsonnet tla function), add them as pipe-delimited pairs. Here's the example:

Suppose we have a template such as:

```jsonnet
local lib = import '../base-tla.libsonnet';

function(value1, value2) {
  "name": "my name is",
  "config": lib.Config("sample-with-tla", value1, value2),
}
```

Add the top-level function lives in the `base-tla.libsonnet` file referenced above, as such:

```jsonnet
{
  Config(name, value1, value2): {
      "name": name,
      "s3.region": "us-east-1",
      "something.of.value1": value1,
      "something.of.value2": value2,
  },
}

```

Executing the task below will pass these values to the templates and functions, replacing the variables in the resulting json:

```shell
> ./gradlew submitConnectors --print-only --connector-subdir prod --jsonnet-tla-str-args='value1=the value|value2=something else'
```