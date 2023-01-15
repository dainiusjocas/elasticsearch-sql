(ns esql.elasticsearch-test-container
  (:require [clj-test-containers.core :as tc]
            [clojure.string :as str]
            [org.httpkit.client :as http]
            [cheshire.core :as json]))

(def elasticsearch-version-docker-tag "8.6.0")

(def ^{:doc "Name of the index with sample data"}
  index-name "index")

(defn -data
  ([] (-data 10))
  ([n] (-data n index-name))
  ([n index] (mapv (fn [i]
                     [{:index {:_index index :_id i}}
                      {:COL1 i :COL2 i}])
                   (range n))))

(defn -hostname [port]
  (str "http://localhost:" port))

(defn -index-data
  "Data is immediately visible for search."
  [elasticsearch-host]
  @(http/request
     {:url     (str elasticsearch-host "/_bulk?refresh")
      :method  :post
      :headers {"Content-Type" "application/json"}
      :body    (str (->> (-data 10 index-name)
                         (apply concat)
                         (map json/generate-string)
                         (str/join "\n"))
                    "\n")}))

(defonce es
         (delay
           (let [container (-> (tc/create
                                 {:image-name      (str "elasticsearch:" elasticsearch-version-docker-tag)
                                  :exposed-ports   [9200]
                                  :env-vars        {"discovery.type"         "single-node"
                                                    "bootstrap.memory_lock"  "true"
                                                    "xpack.security.enabled" "false"}
                                  :network-aliases ["elasticsearch"]
                                  :wait-for        {:wait-strategy   :http
                                                    :path            "/"
                                                    :port            9200
                                                    :method          "GET"
                                                    :status-codes    [200]
                                                    :tls             false
                                                    :read-timout     5
                                                    :headers         {"Accept" "text/plain"}
                                                    :startup-timeout 20}
                                  :log-strategy    :fn
                                  :function        (fn [log-line] (println "ES>: " log-line))})
                               (tc/start!))
                 elasticsearch-host (-hostname (get (:mapped-ports container) 9200))]
             (-index-data elasticsearch-host)
             container)))

(defn start! [] @es)

(defn stop! [] (tc/stop! es))

(defn host
  "Elasticsearch testcontainer hostname with a proper port"
  []
  (-hostname (get (:mapped-ports @es) 9200)))
