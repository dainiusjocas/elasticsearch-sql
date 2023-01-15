# esql - Elasticsearch SQL API Client

A CLI tool and a Clojure library to query [Elasticearch](https://www.elastic.co/what-is/elasticsearch) with SQL.

## Use cases

1. Filter with SQL query and export millions of docs from Elasticsearch to a CSV file.
```shell
ESQL_ELASTICSEARCH_HOSTS=http://localhost:9200 ./eqsl --query="SELECT * FROM logs LIMIT 1000000" --format=csv --fetch-size=10000
```

2. Clojure Library implements `IReduceInit`, so it is easy to use with transducers.
```clojure

```

## Why?

The initial idea was to play with the [Elasticsearch specification](https://github.com/elastic/elasticsearch-specification):
- Convert it to [Malli](https://github.com/metosin/malli) schema;
- Generate CLI API from the Malli schema;
- Make a useful tool.

## Roadmap

- [x] Babashka compatible (with an exception of cbor and smile formats)
- [x] `bb` task to generate Malli schema for Elasticsearch SQL params input
- [x] CLI interface that documents all params based on [malli-cli](https://github.com/piotr-yuxuan/malli-cli)
- [x] Transducer ready
- [ ] Installable GraalVM native-image
  - brew
  - windows
- [ ] Install with [bbin](https://github.com/babashka/bbin)
- [x] somehow make one executable
- [x] test in the JVM with testcontainers
- [x] Generate a Environment variables defaults for the malli schema
  - [x] "UPPERCASE_REPLACE_WITH_UNDERSCORES"
  - [x] with prefix? yes ESQL_*
  - [x] ELASTICSEARCH_HOSTS from kibana [docker](https://www.elastic.co/guide/en/kibana/current/docker.html) 
- [x] Split into lib, and app aliases. Native-image builds app alias
  - All the main and CLI stuff
- [ ] Deploy lib to Clojars
- [ ] Handle `params` schema `[:vector :any]`, cli `--params 1 --params 2` into a vector
- [ ] Babashka pod
- [ ] Docker image
- [ ] Patient reverse fetch (if possible)

native-image -jar target/esql.jar -o target/esql --no-fallback --initialize-at-build-time

## Development

Install:
- [Clojure](https://clojure.org/guides/install_clojure)
- [Babashka](https://github.com/babashka/babashka)
- [Docker](https://docs.docker.com/get-docker/): tests are using [Testcontainers](https://github.com/javahippie/clj-test-containers).
- Optional: [clj-kondo](https://github.com/clj-kondo/clj-kondo)

Run `bb tasks` for a list of all available tasks.

## License

Copyright &copy; 2023 [Dainius Jocas](https://www.jocas.lt).

Distributed under The Apache License, Version 2.0.
