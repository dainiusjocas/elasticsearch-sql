# Examples

Index names containing dashes must be escaped with double quotes:
```shell
./esql --query="SELECT title FROM \"articles-index\""
./esql --query='SELECT title FROM "articles-index"'
```

Escaping string parameters when wrapped in double quotes:
```shell
./esql --query="SELECT title FROM \"articles-index\" WHERE MATCH(title, 'clojure') LIMIT 2"
```

Another way escaping string parameter when CLI params is wrapped in single quotes:
```shell
./esql --query='SELECT title FROM "article-items" WHERE MATCH(title, '"'"'tee'"'"') LIMIT 2'
```

Run with Babashka:
```shell
./esql --query="SELECT * FROM index LIMIT 2" --format=csv --fetch-size=1
```

Run with Clojure:
```shell
clojure -M:app -m eqsl.main --query="SELECT * FROM index LIMIT 2" --format=csv --fetch-size=1
```

