(ns esql.cli.keys-test
  (:require [clojure.test :refer [deftest is testing]]
            [malli.core :as m]
            [malli.util :as mu]
            [esql.cli.keys :as cli-keys]))

(deftest schema-to-cli-schema
  (testing "boolean flags should take 0 args"
    (let [schema [:map [:foo [:boolean]]]]
      (is (= {:arg-number 0}
             (m/properties (mu/get (cli-keys/boolean-handles-0-args schema)
                                   :foo))))))

  (testing "map-of params should collect into a list"
    (let [schema [:map [:params [:map-of {:a :b} :string :any]]]]
      (is (= [:map [:params [:any {:a :b
                                   :update-fn cli-keys/collect-args-into-vector}]]]
             (m/form (cli-keys/replace-map-of-with-any-and-concat-vals schema))))))

  (testing "preparing keys for CLI"
    (let [schema [:map [:foo_bar [:string]]]
          prep-schema (cli-keys/prepare-keys-for-cli schema)]
      (is (= '(:foo-bar) (mu/keys prep-schema)))
      (is (= [:map [:foo-bar :string]]
             (m/form prep-schema)))))

  (testing "adding environment variables"
    (let [schema [:map [:foo-bar [:string]]]
          prep-schema (cli-keys/add-env-var-defaults-to-keys schema)]
      (is (= [:map [:foo-bar [:string {:env-var "ESQL_FOO_BAR"}]]]
             (m/form prep-schema)))))

  (testing "final schema construction"
    (let [schema [:map [:foo_bar [:boolean]]]]
      (is (= [:map [:foo-bar [:boolean {:arg-number 0
                                        :env-var    "ESQL_FOO_BAR"}]]]
             (m/form (cli-keys/prepare schema nil)))))))
