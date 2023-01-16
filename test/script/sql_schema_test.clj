(ns script.sql-schema-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [cheshire.core :as json]
    [sql-schema :as parser]))

(def sql-request-spec
  (json/decode (slurp "test/resources/sql-query-spec.json") true))

(def specification
  (let [sql-endpoint-spec (json/decode (slurp "test/resources/endpoint-spec.json") true)
        sql-response-spec (json/decode (slurp "test/resources/sql-response-spec.json") true)]
    {:_info     {}
     :endpoints [sql-endpoint-spec]
     :types     [sql-request-spec sql-response-spec]}))

(deftest spec-finder
  (testing "find endpoint by name"
    (let [endpoint-spec (parser/get-endpoint-spec specification parser/sql-endpoint-spec-name)]
      (is (map? endpoint-spec))
      (is (nil? (parser/get-endpoint-spec specification "DOESNT_EXIST")))
      (testing "find request spec by endpoint spec"
        (let [endpoint-spec (parser/get-endpoint-spec specification parser/sql-endpoint-spec-name)]
          (is (map? (parser/get-request-spec specification endpoint-spec)))
          (is (nil? (parser/get-request-spec specification {}))))))))

(deftest request-spec->schema
  (testing "request-spec->malli-schema"
    (let [ms (parser/es-type-spec->malli-schema sql-request-spec)]
      (is (= :map (first ms)))
      (is (string? (:description (second ms))))
      (is (= 18 (count ms)))
      (let [first-key-spec (first (sort-by first (drop 2 ms)))]
        (is (= 3 (count first-key-spec)))
        (is (keyword? (first first-key-spec)))
        (is (map? (second first-key-spec)))
        (is (vector? (last first-key-spec)))
        (is (keyword? (first (last first-key-spec))))
        (let [props (second first-key-spec)]
          (is (select-keys props [:description :default])))))))
