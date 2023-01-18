[![Clojars Project](https://img.shields.io/clojars/v/lt.jocas/elasticsearch-sql.svg)](https://clojars.org/lt.jocas/elasticsearch-sql)
[![cljdoc badge](https://cljdoc.org/badge/lt.jocas/elasticsearch-sql)](https://cljdoc.org/d/lt.jocas/elasticsearch-sql/CURRENT)
[![Tests](https://github.com/dainiusjocas/elasticsearch-sql/actions/workflows/test.yml/badge.svg)](https://github.com/dainiusjocas/elasticsearch-sql/actions/workflows/test.yml)
<a href="https://babashka.org" rel="nofollow"><img src="https://github.com/babashka/babashka/raw/master/logo/badge.svg" alt="bb compatible" style="max-width: 100%;"></a>

# esql - Elasticsearch SQL API CLI Client

A CLI tool and a Clojure library to query [Elasticearch](https://www.elastic.co/what-is/elasticsearch) with SQL.

## Use cases

1. Filter with SQL query and export millions of docs from Elasticsearch to a CSV file:

```shell
ESQL_ELASTICSEARCH_HOSTS=http://localhost:9200 ./eqsl --query="SELECT * FROM logs LIMIT 1000000" --format=csv
```

2. Clojure library that exposes hits as an `IReduceInit`, so it is easy to use
   with [transducers](https://clojure.org/reference/transducers):

```clojure
(into [] (comp) (esql/reducible {:elasticsearch_hosts "http://localhost:9200"
                                 :query               "SELECT * FROM index LIMIT 1"
                                 :format              "csv"}))
```

## Install

```shell
 brew install dainiusjocas/brew/esql
```

## Why?

The initial idea was to play with
the [Elasticsearch specification](https://github.com/elastic/elasticsearch-specification):

- Convert it to a [Malli](https://github.com/metosin/malli) schema;
- Generate a CLI API from the Malli schema for the [malli-cli](https://github.com/piotr-yuxuan/malli-cli);
- Make a useful CLI tool.

## CLI

Available params:

```txt
ESQL CLI parameters:
  Short  Long option                    Default                  Description
         --columnar                                              If true, the results in a columnar fashion: one row represents all the values of a certain column from the current page of results.
         --delimiter                    ","                      The CSV format accepts a formatting URL query attribute, delimiter, which indicates which character should be used to separate the CSV values.
         --dry-run                                               Prints configuration map with defaults.
         --elasticsearch-hosts          "http://localhost:9200"  Elasticsearch host
         --fetch-size                   1000                     The maximum number of rows (or entries) to return in one response
         --field-multi-value-leniency   true                     Throw an exception when encountering multiple values for a field (default) or be lenient and return the first value from the list (without any guarantees of what that will be - typically the first in natural ascending order).
         --filter                                                Optional Elasticsearch query DSL for additional filtering.
         --format                       "csv"                    Elasticsearch SQL can return the data in several formats
  -h     --help                                                  Display usage summary and exit.
         --index-using-frozen           false                    If true, the search can run on frozen indices. Defaults to false.
         --keep-alive                   "5d"                     Retention period for an async or saved synchronous search.
         --keep-on-completion           false                    If true, Elasticsearch stores synchronous searches if you also specify the wait_for_completion_timeout parameter. If false, Elasticsearch only stores async searches that don’t finish before the wait_for_completion_timeout.
         --page-timeout                 "45s"                    The timeout before a pagination request fails.
         --params                                                Values for parameters in the query.
         --query                                                 SQL query to execute
         --request-timeout              "90s"                    The timeout before the request fails.
         --time-zone                                             Time-zone in ISO 8601 used for executing the query on the server. More information available here.
         --wait-for-completion-timeout                           Period to wait for complete results. Defaults to no timeout, meaning the request waits for complete search results. If the search doesn’t finish within this period, the search becomes async.
```

## Library

From the [Clojars](https://clojars.org/lt.jocas/elasticsearch-sql/):
```clojure
lt.jocas/elasticsearch-sql {:mvn/version "RELEASE"}
```

## Development

Install:

- [Clojure](https://clojure.org/guides/install_clojure)
- [Babashka](https://github.com/babashka/babashka)
- [Docker](https://docs.docker.com/get-docker/): tests are
  using [Testcontainers](https://github.com/javahippie/clj-test-containers).
- Optional: [clj-kondo](https://github.com/clj-kondo/clj-kondo)

Run `bb tasks` for a list of all available tasks.

## Roadmap

- [x] Babashka compatible (with an exception of cbor and smile formats)
- [x] `bb` task to generate Malli schema for Elasticsearch SQL params input
- [x] CLI interface that documents all params based on [malli-cli](https://github.com/piotr-yuxuan/malli-cli)
- [x] Transducer ready
- [ ] Installable GraalVM native-image
  - brew
  - windows
- [ ] Install with [bbin](https://github.com/babashka/bbin)
- [x] Test in the JVM with [testcontainers](https://github.com/javahippie/clj-test-containers)
- [x] Generate an environment variable defaults for the Malli schema:
  - [x] "UPPERCASE_REPLACE_WITH_UNDERSCORES"
  - [x] with prefix? yes ESQL_*
  - [x] ELASTICSEARCH_HOSTS from kibana [docker](https://www.elastic.co/guide/en/kibana/current/docker.html)
- [x] Split into lib, and app `deps.edn` aliases. Native-image builds the `app` alias
- [x] Deploy lib to Clojars
- [x] Handle `params` schema `[:vector :any]`, cli `--params 1 --params 2` into a vector
- [ ] Babashka pod
- [ ] Docker image
- [ ] "Patient reverse fetch" (if possible)

## License

Copyright &copy; 2023 [Dainius Jocas](https://www.jocas.lt).

Distributed under The Apache License, Version 2.0.
