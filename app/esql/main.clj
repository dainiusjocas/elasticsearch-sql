(ns esql.main
  (:gen-class)
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [esql.cli :as cli]
    [cheshire.core :as json]
    [clj-yaml.core :as yaml]
    [elasticsearch.sql :as sql]))

(set! *warn-on-reflection* true)

(defn underscore-keys
  "SQL expects all the keys to be underscored."
  [m]
  (reduce-kv
    (fn replace-dash-with-underscore [m k v]
      (assoc m (-> k name (str/replace "-" "_") keyword) v))
    {}
    m))

(defn format-data
  "Depending on the format"
  [config]
  (case (:format config)
    "json" (map (fn [data] (json/generate-string data)))
    "smile" (map (fn [data] (String. (json/generate-smile data))))
    "cbor" (map (fn [data] (String. (json/generate-cbor data))))
    "yaml" (map (fn [data] (yaml/generate-string data)))
    (comp)))

(def initial-value nil)

(defn print-reducing-function
  ([])
  ([_])
  ([_ data]
   (if (string? data)
     (print data)
     (println data))))

(defn execute
  "Fetch data from Elasticsearch and print it to STDOUT.
  Elasticsearch SQL expects config params to be with underscores."
  [config]
  (transduce (format-data config)
             print-reducing-function
             initial-value
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

        :else (execute config))))
  (.flush *out*))
