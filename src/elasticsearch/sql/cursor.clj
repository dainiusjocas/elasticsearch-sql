(ns elasticsearch.sql.cursor
  (:require [elasticsearch.sql.client :as client]))

(defn extract
  "Handle cursor for different formats.
  - if csv, tst, txt -> take from headers
  - if json, cbor, smile, yaml -> take from body"
  [headers body {:keys [format]}]
  (let [cursor (cond
                 (contains? #{"csv" "tsv" "txt"} format) (:cursor headers)
                 (contains? #{"json" "cbor" "smile"} format) (:cursor body)
                 ; TODO: handle yaml
                 :else nil)]
    (when cursor
      {:cursor cursor})))

(defn clear
  [host request-headers cursor]
  (when cursor
    (let [resp (:body (client/close-cursor host cursor request-headers))]
      (when (System/getenv "DEBUG_MODE")
        (.println System/err (str "Closed cursor: " resp))))))
