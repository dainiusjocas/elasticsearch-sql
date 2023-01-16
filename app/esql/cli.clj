(ns esql.cli
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [malli.transform :as mt]
    [piotr-yuxuan.malli-cli :as malli-cli]
    [esql.cli.summary :as cli-summary]
    [esql.cli.schema :as cli-schema]))

(def default-parse-opts
  {:with-defaults?              true
   :with-optional-key-defaults? true})

(defn parse-args
  ([args] (parse-args args default-parse-opts))
  ([args {:keys [with-defaults? with-optional-key-defaults?]}]
   (m/decode cli-schema/final
             args
             (mt/transformer malli-cli/cli-transformer
                             (when with-defaults?
                               (mt/default-value-transformer
                                 {::mt/add-optional-keys
                                  with-optional-key-defaults?}))))))

(def summary
  (format "ESQL CLI parameters:\n%s"
          (malli-cli/summary (cli-summary/prepare cli-schema/final))))

(defn invalid? [config]
  (not (m/validate cli-schema/final config)))

(defn explain-errors [config]
  (format "Invalid configuration value: %s"
          (me/humanize (m/explain cli-schema/final config))))
