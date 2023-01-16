(ns elasticsearch.sql.headers
  (:import (java.util Base64)))

(def format->accept-header
  {"csv"   {"Accept" "text/csv"}
   "tsv"   {"Accept" "text/tab-separated-values"}
   "txt"   {"Accept" "text/plain"}
   "json"  {"Accept" "application/json"}
   "yaml"  {"Accept" "application/yaml"}                    ; Doesn't deserialize
   "cbor"  {"Accept" "application/cbor"}
   "smile" {"Accept" "application/smile"}})

(defn prepare-accept-header [^String format]
  (get format->accept-header format {"Accept" "application/json"}))

(def defaults
  {"Content-Type" "application/json;charset=utf-8"})

(defn authorization-token []
  (let [username (System/getenv "ELASTIC_USERNAME")
        password (System/getenv "ELASTIC_PASSWORD")]
    (when (and username password)
      (.encodeToString (Base64/getEncoder) (.getBytes (str username ":" password))))))

(defn combine [headers]
  ; basic authorization
  ; https://www.elastic.co/guide/en/elasticsearch/reference/current/http-clients.html
  (if-let [auth-token (authorization-token)]
    (assoc headers "Authorization" (str "Basic " auth-token))
    headers))

(defn prepare [{:keys [format]}]
  (let [[accept format] (first (prepare-accept-header format))]
    (combine (assoc defaults accept format))))
