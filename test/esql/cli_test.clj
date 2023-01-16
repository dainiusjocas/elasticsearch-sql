(ns esql.cli-test
  (:require [clojure.test :refer [deftest is testing]]
            [esql.cli :as cli]))

(def parse-opts
  {:with-defaults?              false
   :with-optional-key-defaults? false})

(deftest arg-parsing
  (testing "no args"
    (is (= {:elasticsearch-hosts "http://localhost:9200"
            :query               nil}
           (cli/parse-args [] parse-opts))))

  (testing "boolean flags"
    (is (= {:elasticsearch-hosts "http://localhost:9200"
            :query               nil
            :dry-run             true}
           (cli/parse-args ["--dry-run"] parse-opts))))

  (testing "a known key with ="
    (is (= {:elasticsearch-hosts "http://localhost:9200"
            :query               nil
            :fetch-size          1}
           (cli/parse-args ["--fetch-size=1"] parse-opts))))
  (testing "a known key with separate value"
    (is (= {:elasticsearch-hosts "http://localhost:9200"
            :query               nil
            :fetch-size          1}
           (cli/parse-args ["--fetch-size" "1"] parse-opts))))

  (testing "params handling"
    (is (= {:elasticsearch-hosts "http://localhost:9200"
            :query               nil
            :params              ["a" "b"]}
           (cli/parse-args
             ["--params=a" "--params=b"]
             parse-opts))))

  (testing "Some lesser used options parsing"
    (is (= {:columnar                    true
            :elasticsearch-hosts         "http://localhost:9200"
            :field-multi-value-leniency  true
            :index-using-frozen          true
            :keep-alive                  "11s"
            :keep-on-completion          true
            :query                       nil
            :request-timeout             "12s"
            :wait-for-completion-timeout "123s"
            :page-timeout                "45s"}
           (cli/parse-args
             ["--columnar"
              "--request-timeout=12s"
              "--field-multi-value-leniency"
              "--wait-for-completion-timeout=123s"
              "--keep-alive=11s"
              "--keep-on-completion"
              "--index-using-frozen"
              "--page-timeout=45s"]
             parse-opts)))))


(deftest validation
  (testing "passing string instead of integer is invalid"
    (let [config {:query               "a"
                  :elasticsearch-hosts "a"
                  :fetch-size          "a"}]
      (is (cli/invalid? config))
      (is (= "Invalid configuration value: {:fetch-size [\"should be an integer\"]}"
             (cli/explain-errors config))))))
