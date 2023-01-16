(ns elasticsearch.sql
  (:require
    [elasticsearch.sql.client :as client]
    [elasticsearch.sql.headers :as headers]
    [elasticsearch.sql.body :as body]
    [elasticsearch.sql.cursor :as cursor]
    [elasticsearch.sql.query-params :as query-params])
  (:import
    (clojure.lang IReduceInit)))

(defn reducible
  "Creates an IReduceInit given Elasticsearch SQL query.
  Docs: https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html"
  [{host :elasticsearch_hosts query :query :as conf}]
  (assert (some? host) "Elasticsearch host is required!")
  (assert (some? query) "Query is required!")
  (let [body (body/prep-initial conf)
        query-url-params (query-params/prepare conf)
        request-headers (headers/prepare conf)]
    (reify IReduceInit
      (reduce [_ rf init]
        (try
          (loop [state init
                 {:keys [headers status]
                  :as   resp} (client/start host body query-url-params request-headers)]
            (if (= 200 status)
              (let [body (body/decode resp)
                    cursor (cursor/extract headers body conf)]
                (if (reduced? state)
                  (do
                    ; Downstream stopped consuming; closing the cursor
                    (cursor/clear! host request-headers cursor)
                    (unreduced state))
                  (if cursor
                    ; next page
                    (recur (body/apply-rf rf state body)
                           (client/continue host
                                            (cursor/prepare-body cursor conf)
                                            query-url-params
                                            request-headers))
                    ; last page
                    (do
                      (body/apply-rf rf state body)
                      state))))
              ; Pass the full error for further processing
              (rf state resp)))
          (catch Exception e
            (.println System/err (.getMessage e))))))))
