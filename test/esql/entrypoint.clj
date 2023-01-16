(ns esql.entrypoint
  (:require
    [cognitect.test-runner :as tr]
    [esql.elasticsearch-test-container :as elasticsearch-test-container]))

(defn -main [& args]
  (let [start (System/currentTimeMillis)]
    (println "Starting Elasticsearch test container...")
    (elasticsearch-test-container/start!)
    (println "Elasticsearch started in:"
             (- (System/currentTimeMillis) start)
             "ms"))
  ; run the test suite
  (tr/-main args)
  (elasticsearch-test-container/stop!))
