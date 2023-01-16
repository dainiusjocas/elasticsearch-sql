(ns esql.integration-test
  (:require [clojure.test :refer [deftest is]]
            [esql.main :as esql]
            [esql.elasticsearch-test-container :as elasticsearch]))

(deftest invoke-main-and-capture-output
  (let [cli-params ["--elasticsearch-hosts" (elasticsearch/host)
                    "--query" (format "SELECT * FROM %s LIMIT 1" elasticsearch/index-name)
                    ; "--columnar=false" ; malli-cli doesn't handle negative booleans
                    "--request-timeout=12s"
                    "--field-multi-value-leniency"
                    "--wait-for-completion-timeout=123s"
                    "--keep-alive=11m"
                    "--keep-on-completion"
                    "--index-using-frozen"
                    "--page-timeout=45s"]]
    (is (= "COL1,COL2\r\n0,0\r\n" (with-out-str (apply esql/-main cli-params))))))
