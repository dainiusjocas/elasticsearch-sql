- The documentation and parameters are extracted fromt the Elasticsearch specification
- The input validation is done using Clojure Malli
- Each CLI flag has a corresponding environment variable
- Processing is shaped as a Clojure Transducer


## Testing

We'll use [TestContainers](https://www.testcontainers.org).
In particular we'll use the [clj-test-containers](https://github.com/javahippie/clj-test-containers).

It takes about 15 seconds for Elasticsearch Docker container to start.
It would be a bad developer experience to start it for each namespace.
We want to have one running container per development session.

To achieve that Elasticsearch container is started only once we'll create a namespace that:
- starts Elasticsearch container in a delay block;
- has a function that returns current config (e.g. port);
- function to stop the container
