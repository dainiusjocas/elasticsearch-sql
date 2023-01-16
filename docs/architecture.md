# ESQL

The CLI tool and the library to query Elasticsearch SQL API.

## Core 

- The documentation and parameters are extracted from the Elasticsearch specification
- The input validation is done using Clojure Malli
- Each CLI flag has a corresponding prefixed environment variable
- Processing is shaped as a Clojure Transducer

## Testing

[TestContainers](https://www.testcontainers.org) are used for the integration testing.
In particular, we are using the [clj-test-containers](https://github.com/javahippie/clj-test-containers).

Be aware that it takes about 15 seconds for an Elasticsearch Docker container to start.
It would be a bad developer experience to start it for each test namespace.
We want to have one running container per development session.

To achieve that the Elasticsearch container is started only once we've created a namespace that:
- starts an Elasticsearch container in a delay block;
- has a function that returns the current config (e.g. a hostname with a port);
- function to stop the container.
