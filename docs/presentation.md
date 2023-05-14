```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}

(ns simple-slideshow
  (:require
    [nextjournal.clerk :as clerk]
    [nextjournal.clerk-slideshow :as slideshow]
    [nextjournal.clerk.viewer :as v])
  (:import (java.time Instant)))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}

(clerk/add-viewers! [slideshow/viewer])
```

# Elasticsearch SQL Intro

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}
 :nextjournal.clerk/no-cache true}
(clerk/html [:ul
             [:li [:h4 "Dainius Jocas"]]
             [:li [:h4 (.toString (Instant/now))]]])
```
---

## whoami

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :hide}
  :nextjournal.clerk/no-cache true}
{:nextjournal.clerk/visibility {:code :show :result :hide}
 :nextjournal.clerk/no-cache true}
```

```clojure
{:name          "Dainius Jocas"
 :company       {:name    "Vinted"
                 :mission "Make second-hand the first choice worldwide"}
 :role          "Search Engineer"
 :website       "https://www.jocas.lt"
 :twitter       "@dainius_jocas"
 :github        "dainiusjocas"
 :author_of_oss ["lucene-grep" "quarkus-lucene" "clj-jq" "ket" "esql"]}
```

---

## Agenda

- ### What is Elasticsearch SQL?
- ### How to use it?
- ### Demo
- ### Limitations
- ### Bonus :)

---

## What is Elasticsearch?

> Elasticsearch is a search engine based on the Lucene library. It provides a distributed, multitenant-capable full-text search engine with an HTTP web interface and schema-free JSON documents.

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}
  :nextjournal.clerk/no-cache true}
(clerk/html
  [:pre
   "curl -X GET -H 'Content-Type: application/json' \"http://localhost:9200/items/_search\" -d'\n{\n  \"query\": {\n    \"multi_match\" : {\n      \"query\":    \"this is a test\", \n      \"fields\": [ \"subject\", \"message\" ] \n    }\n  }\n}'"])
```

---

## What is Elasticsearch SQL?

> Tap into Elasticsearch with a familiar syntax
> - [Elasticsearch Homepage](https://www.elastic.co/what-is/elasticsearch-sql)

Just write `SELECT * FROM my_table` and enjoy your data :)


---

## How does it work?

- ### `SQL -> Elasticsearch DSL -> Execute Query -> Present Results`
- [Apache Calcite](https://calcite.apache.org)
  - > Apache Calcite is an open source framework for building databases and data management systems. It includes a SQL parser, an API for building expressions in relational algebra, and a query planning engine.
- [Adapter](https://calcite.apache.org/docs/elasticsearch_adapter.html)
- Elasticsearch mappings are mapped into Apache Calcite SQL types
- SQL Queries are rewritten into Elasticsearch DSL

---

## [Supported SQL Commands](https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-commands.html)

- `SHOW TABLES`: List tables available.
- `DESCRIBE TABLE`: Describe a table.
- `SELECT`: Retrieve rows from zero or more tables.
- `SHOW CATALOGS`: List available catalogs.
- `SHOW COLUMNS`: List columns in table.
- `SHOW FUNCTIONS`: List supported functions.

---

# How to use it: JSON over HTTP

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}
  :nextjournal.clerk/no-cache true}
(clerk/html 
  [:pre "curl -X POST \"localhost:9200/_sql?format=txt&pretty\" -H 'Content-Type: application/json' -d'\n{\n  \"query\": \"SELECT * FROM library WHERE release_date < \\u00272000-01-01\\u0027\"\n}\n'\n"])
```

---

# How to use it: Kibana

- "Dev Tools"

---

# How to use it: SQL shell


```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}
  :nextjournal.clerk/no-cache true}
(clerk/html 
  [:pre "docker run -it elasticsearch:8.5.3 \\\n  ./bin/elasticsearch-sql-cli \\\n  http://localhost:9200"])
```

- NOTE: not any-to-any versions are compatible

---

# How to use it: Going Crazy

- DB workbenches e.g. [DBeaver](https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-client-apps-dbeaver.html#sql-client-apps-dbeaver)
  - Requires a paid license :(
- JDBC Driver 
  - Requires a paid license :(
  - *(should be "faster" than the regular "JSON over HTTP")*
- Yes, Microsoft Excel ;)
- ODBC Driver

---

# Translate API

- SQL -> Elasticsearch DSL!
- IMO, it is easier to express the boolean logic in SQL and then translate it to the Elasticsearch SQL.

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}
  :nextjournal.clerk/no-cache true}
(clerk/html
  [:pre "curl -X POST \"localhost:9200/_sql/translate?pretty\" -H 'Content-Type: application/json' -d'\n{\n  \"query\": \"SELECT * FROM library ORDER BY page_count DESC\",\n  \"fetch_size\": 10\n}\n'\n"])

```

---

# Limitations

- Table/index/alias names better be escaped 
  - with `""` 
  - because `-` is not valid character for table names
  - PRO TIP: add aliases that are valid SQL table names to your indices
- Fields with multiple values are not supported
  - `[Arrays (returned by [all_text_combined]) are not supported]`
  - can be avoided with `lenient = true;` in the CLI console
  - In REST API `"field_multi_value_leniency": true`
- Permissions might not allow to list tables.
- No `JOIN`s

---

# Bonus

- #### I believe it is best to learn by building things.
- #### So, I've made a CLI tool.
- #### **ESQL**: A CLI tool query Elasticsearch SQL API
- #### https://github.com/dainiusjocas/elasticsearch-sql

> #### `esql --query='SELECT title, updated_at FROM "items" LIMIT 1' --format=txt`
---

##  Example: fetch 20K docs to CSV file

- #### `brew install dainiusjocas/brew/esql`

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}
  :nextjournal.clerk/no-cache true}
(clerk/html 
  [:pre
   "esql --query='SELECT * FROM \"items\" LIMIT 20000' \\ \n  --format=csv \\ \n  > my.csv"])
```

---

# Thank You!

---
