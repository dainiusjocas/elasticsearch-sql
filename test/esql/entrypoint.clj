(ns esql.entrypoint
  (:require
    [cognitect.test-runner :as tr]
    [esql.elasticsearch-test-container :as es-test-cointainer]))

(defn -main [& args]
  (let [start (System/currentTimeMillis)]
    (println "Starting Elasticsearch test container...")
    (es-test-cointainer/start!)
    (println "Elasticsearch started in:"
             (- (System/currentTimeMillis) start)
             "ms"))
  ; run the test suite
  (tr/-main args)
  (es-test-cointainer/stop!))
