(ns elasticsearch.sql.cursor
  (:require [elasticsearch.sql.client :as client]))

(defn extract
  "Handles cursor for different formats.
  - if csv, tst, txt -> take from headers
  - if json, cbor, smile, yaml -> take from body
  If cursor is not extracted then returns nil."
  [headers body {:keys [format]}]
  (let [cursor (cond
                 (contains? #{"csv" "tsv" "txt"} format) (:cursor headers)
                 (contains? #{"json" "cbor" "smile" "yaml"} format) (:cursor body)
                 :else nil)]
    (when cursor
      {:cursor cursor})))

(defn prepare-body
  "Only columnar and time_zone parameters are used.
   `cursor` is a map.
  https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-search-api.html"
  [cursor {:keys [columnar time_zone]}]
  (cond-> cursor
          (some? columnar)
          (assoc :columnar columnar)
          (some? time_zone)
          (assoc :time_zone time_zone)))

(defn clear!
  "Clears an SQL search cursor.
  https://www.elastic.co/guide/en/elasticsearch/reference/current/clear-sql-cursor-api.html"
  [host request-headers cursor]
  (when cursor
    (let [resp (:body (client/close-cursor host cursor request-headers))]
      (when (System/getenv "DEBUG_MODE")
        (.println System/err (str "Closed cursor: " resp))))))
