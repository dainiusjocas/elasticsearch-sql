(ns elasticsearch.sql.schema
  (:require
    [clojure.edn :as edn]
    [malli.core :as m]
    [malli.util :as mu]))

(def elasticsearch-host-schema
  [:map
   [:elasticsearch_hosts [:string {:default     "http://localhost:9200"
                                   :description "Elasticsearch host"}]]])

(def es-sql-request-schema
  (m/schema (edn/read-string (slurp "resources/es-sql-request-schema.edn"))))

(def columnar-description
  (str "If true, the results in a columnar fashion: one row represents all "
       "the values of a certain column from the current page of results."))

(defn add-delimiter-schema [schema]
  (let [delimiter-description
        (str "The CSV format accepts a formatting URL query attribute, delimiter,"
             " which indicates which character should be used to separate the CSV values.")]
    (-> schema
        (mu/assoc :delimiter [:and [:string {:max 1 :min 1}] [:not [:enum "\"" \r \n \t]]])
        (mu/optional-keys [:delimiter])
        (mu/update :delimiter mu/update-properties assoc :description delimiter-description)
        (mu/update :delimiter mu/update-properties assoc :default ","))))

(defn add-format-schema [schema]
  (let [description "Elasticsearch SQL can return the data in several formats"]
    (-> schema
        (mu/assoc :format [:enum "csv" "json" "tsv" "txt" "yaml" "cbor" "smile"])
        (mu/optional-keys [:format])
        (mu/update :format mu/update-properties assoc :default "csv")
        (mu/update :format mu/update-properties assoc :description description))))

(def elasticsearch-schema
  (-> es-sql-request-schema
      ; The idea is to always start the new fetching session
      ; Then we don't need cursor
      (mu/dissoc :cursor)
      ; But then query must be provided
      (mu/required-keys [:query])
      ; Strangely, the description is missing
      (mu/update :columnar mu/update-properties assoc :description columnar-description)
      ; Less cryptic errors when multi-value fields are being fetched
      (mu/update :field_multi_value_leniency mu/update-properties assoc :default true)
      ; Fix filter field default value
      (mu/update :filter mu/update-properties dissoc :default)
      (add-format-schema)
      (add-delimiter-schema)))

(def final
  (mu/merge elasticsearch-schema elasticsearch-host-schema))
