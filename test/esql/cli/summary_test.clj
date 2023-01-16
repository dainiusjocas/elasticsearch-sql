(ns esql.cli.summary-test
  (:require [clojure.test :refer [deftest is testing]]
            [malli.core :as m]
            [esql.cli.summary :as cli-summary]))

(deftest param-handling
  (testing "Removing newlines from description"
    (let [schema [:map [:foo [:boolean {:description "foo\nbar"}]]]]
      (is (= [:map [:foo [:boolean {:description "foo bar"}]]]
             (m/form
               (cli-summary/remove-new-lines-from-descriptions schema))))))

  (testing "Fixing compound type keys"
    (let [schema [:map [:foo [:or {:a :b} :string :int]]]]
      (is (= [:map [:foo [:any {:a :b}]]]
             (m/form
               (cli-summary/remove-compound-keys schema))))))

  (testing "Key sorting"
    (let [schema [:map [:b [:any {:a :b}]] [:a [:any {:a :b}]]]]
      (is (= [:map [:a [:any {:a :b}]] [:b [:any {:a :b}]]]
             (m/form
               (cli-summary/sort-keys schema)))))))
