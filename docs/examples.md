## Some examples

Index names containing dashes must be escaped with double quotes:
```shell
./esql --query="SELECT title FROM \"my-index\""
./esql --query='SELECT title FROM "my-index"'
```

Escaping string parameters when wrapped in double quotes:
```shell
./esql --query="SELECT title FROM \"articles-index\" WHERE MATCH(title, 'clojure') LIMIT 2"
```

Or you can provide parameters list:
```shell
./esql --query="SELECT title FROM \"articles-index\" WHERE MATCH(title, ?) LIMIT 2" --params=clojure
```

Filter can also do selection:
```shell
./esql --query="SELECT * FROM index LIMIT 2" --format=csv --filter='{"term": {"COL1": 5}}'
```

Another way escaping string parameter when CLI params is wrapped in single quotes:
```shell
./esql --query='SELECT title FROM "article-items" WHERE MATCH(title, '"'"'tee'"'"') LIMIT 2'
```

Run with Babashka:
```shell
./esql --query="SELECT * FROM index LIMIT 2" --format=csv --fetch-size=1
```

Run with Clojure CLI:
```shell
clojure -M:app -m esql.main --query="SELECT * FROM index LIMIT 2" --format=csv --fetch-size=1
```
