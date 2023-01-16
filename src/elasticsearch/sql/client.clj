(ns elasticsearch.sql.client
  (:require
    [org.httpkit.client :as http]
    [cheshire.core :as json]))

(defn -fetch [params]
  @(http/request params))

(defn start [host body query-params headers]
  (-fetch
    {:url          (str host "/_sql")
     :method       :post
     :headers      headers
     :query-params query-params
     :body         (json/generate-string body)}))

(defn continue
  "Fetches more hits.
  https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-pagination.html"
  [host body query-params headers]
  (-fetch
    {:url          (str host "/_sql")
     :method       :post
     :headers      headers
     :query-params query-params
     :body         (json/generate-string body)}))

(defn close-cursor
  "Clears an SQL search cursor
  https://www.elastic.co/guide/en/elasticsearch/reference/current/clear-sql-cursor-api.html"
  [host body headers]
  (-fetch
    {:url     (str host "/_sql/close")
     :method  :post
     :headers headers
     :body    (json/generate-string body)}))
