(ns elasticsearch.esql-test
  (:require [clojure.test :refer [deftest is testing]]
            [elasticsearch.sql :as esql]
            [esql.elasticsearch-test-container :as es]))

(def es-host (es/host))
(def index-name es/index-name)
(def query (format "SELECT * FROM %s LIMIT 1" index-name))

(deftest format-handling
  (testing "csv format"
    (let [resp (into [] (esql/reducible {:elasticsearch_hosts es-host
                                         :query               query
                                         :format              "csv"}))]
      (is (vector? resp))
      (is (= 1 (count resp)))
      (is (= ["COL1,COL2\r\n0,0\r\n"] resp))))

  (testing "csv format with delimiter ;"
    (let [resp (into [] (esql/reducible {:elasticsearch_hosts es-host
                                         :query               query
                                         :format              "csv"
                                         :delimiter           ";"}))]
      (is (vector? resp))
      (is (= 1 (count resp)))
      (is (= ["COL1;COL2\r\n0;0\r\n"] resp))))

  (testing "tsv format"
    (let [resp (into [] (esql/reducible {:elasticsearch_hosts es-host
                                         :query               query
                                         :format              "tsv"}))]
      (is (vector? resp))
      (is (= 1 (count resp)))
      (is (= ["COL1\tCOL2\n0\t0\n"] resp))))

  (testing "txt format"
    (let [resp (into [] (esql/reducible {:elasticsearch_hosts es-host
                                         :query               query
                                         :format              "txt"}))]
      (is (vector? resp))
      (is (= 1 (count resp)))
      (is (= ["     COL1      |     COL2      \n---------------+---------------\n0              |0              \n"]
             resp))))

  (testing "json format"
    (let [[columns rows] (into [] (esql/reducible {:elasticsearch_hosts es-host
                                                   :query               query
                                                   :format              "json"}))]
      (is (= [{:name "COL1"
               :type "long"}
              {:name "COL2"
               :type "long"}] columns))
      (is (= [[0 0]] rows))))

  (testing "cbor format"
    (let [[columns rows] (into [] (esql/reducible {:elasticsearch_hosts es-host
                                                   :query               query
                                                   :format              "cbor"}))]
      (is (= [{:name "COL1"
               :type "long"}
              {:name "COL2"
               :type "long"}] columns))
      (is (= [[0 0]] rows))))

  (testing "smile format"
    (let [[columns rows] (into [] (esql/reducible {:elasticsearch_hosts es-host
                                                   :query               query
                                                   :format              "smile"}))]
      (is (= [{:name "COL1"
               :type "long"}
              {:name "COL2"
               :type "long"}] columns))
      (is (= [[0 0]] rows))))

  (testing "yaml format"
    (let [resp (into [] (esql/reducible {:elasticsearch_hosts es-host
                                         :query               query
                                         :format              "yaml"}))]
      (is (vector? resp))
      (is (= 2 (count resp)))
      (is (= [{:name "COL1"
               :type "long"}
              {:name "COL2"
               :type "long"}]
             (first resp)))
      (is (= [[0 0]] (second resp))))))

(deftest cursor-handling
  (let [query (format "SELECT * FROM %s LIMIT 2" index-name)]
    (testing "csv format, trick is to specify delimiter which requires query params with next page…"
      (let [resp (into [] (esql/reducible {:elasticsearch_hosts es-host
                                           :query               query
                                           :format              "csv"
                                           :delimiter           ";"
                                           :fetch_size          1}))]
        (is (vector? resp))
        (is (= 2 (count resp)))
        (is (= ["COL1;COL2\r\n0;0\r\n"
                "1;1\r\n"] resp))))
    (testing "json format"
      (let [[_ & rows
             :as resp] (into [] (esql/reducible {:elasticsearch_hosts es-host
                                                 :query               query
                                                 :format              "json"
                                                 :fetch_size          1}))]
        (is (vector? resp))
        (is (= 2 (count rows)))
        (is (= '([[0 0]]
                 [[1 1]]) rows))))))

(deftest handle-cursor-closing
  (let [query (format "SELECT * FROM %s LIMIT 10" index-name)]
    (testing "early termination"
      (let [resp (into [] (take 1) (esql/reducible {:elasticsearch_hosts es-host
                                                    :query               query
                                                    :format              "csv"
                                                    :fetch_size          1}))]
        (is (vector? resp))
        (is (= 1 (count resp)))
        (is (= ["COL1,COL2\r\n0,0\r\n"] resp))))))


(deftest handle-columnar
  (let [query (format "SELECT * FROM %s LIMIT 2" index-name)]
    (testing "asking columnar data"
      (let [resp (into [] (esql/reducible {:elasticsearch_hosts es-host
                                           :query               query
                                           :format              "json"
                                           :columnar            true}))]
        (is (vector? resp))
        (is (= 2 (count resp)))
        (is (= [[{:name "COL1"
                  :type "long"}
                 {:name "COL2"
                  :type "long"}]
                '([0 1]
                  [0 1])] resp))))

    (testing "asking columnar and paging"
      (let [resp (into [] (esql/reducible
                            {:elasticsearch_hosts es-host
                             :query               (format "SELECT * FROM %s LIMIT 4" index-name)
                             :format              "json"
                             :columnar            true
                             :fetch_size          2}))]
        (is (vector? resp))
        (is (= 3 (count resp)))
        (is (= [[{:name "COL1"
                  :type "long"}
                 {:name "COL2"
                  :type "long"}]
                '([0 1]
                  [0 1])
                '([2 3]
                  [2 3])] resp))))))

(deftest handle-params
  (let [query (format "SELECT * FROM %s WHERE COL1=? LIMIT 2" index-name)]
    (testing "early termination"
      (let [resp (into [] (take 1) (esql/reducible {:elasticsearch_hosts es-host
                                                    :query               query
                                                    :format              "csv"
                                                    :params              [3]}))]
        (is (vector? resp))
        (is (= 1 (count resp)))
        (is (= ["COL1,COL2\r\n3,3\r\n"] resp))))))

(deftest handle-filter
  (let [query (format "SELECT * FROM %s LIMIT 2" index-name)]
    (testing "early termination"
      (let [resp (into [] (esql/reducible {:elasticsearch_hosts es-host
                                           :query               query
                                           :format              "csv"
                                           :params              [3]
                                           :filter              {:term {:COL1 5}}}))]
        (is (vector? resp))
        (is (= 1 (count resp)))
        (is (= ["COL1,COL2\r\n5,5\r\n"] resp))))))
