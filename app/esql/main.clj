(ns esql.main
  (:gen-class)
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [esql.cli :as cli]
    [elasticsearch.sql :as sql]))

(defn underscore-keys
  "SQL expects all the keys to be underscored."
  [m]
  (reduce-kv
    (fn replace-dash-with-underscore [m k v]
      (assoc m (-> k name (str/replace "-" "_") keyword) v))
    {}
    m))

(def noop-transducer (comp))
(def initial-value nil)

(defn print-reducing-function
  ([])
  ([_])
  ([_ data]
   (if (string? data)
     (.print System/out data)
     (.println System/out data))))

(defn execute
  "Fetch data from Elasticsearch and print it to STDOUT.
  Elasticsearch SQL expects config params to be with underscores."
  [config]
  (transduce noop-transducer print-reducing-function initial-value
             (sql/reducible (underscore-keys config))))

(defn -main [& args]
  (if (empty? args)
    (do
      (println cli/summary)
      (System/exit 1))
    (let [config (cli/parse-args args)]
      (cond
        (:help config)
        (println cli/summary)

        (:dry-run config)
        (pp/pprint config)

        (cli/invalid? config)
        (do
          (println (cli/explain-errors config))
          (System/exit 1))

        :else (execute config)))))
