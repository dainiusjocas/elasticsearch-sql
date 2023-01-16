(ns esql.cli.keys
  (:require
    [clojure.string :as str]
    [malli.core :as m]
    [malli.util :as mu]
    [esql.cli.util :as util]))

(defn dashify-keys [acc key]
  (assoc acc key (-> key (name) (str/replace "_" "-") (keyword))))

(defn prepare-keys-for-cli
  "Malli schema doesn't handle underscores in keys,
   e.g. --fetch_size can't be used, while --fetch-size can."
  [schema]
  (->> (mu/keys schema)
       (reduce dashify-keys {})
       (mu/rename-keys schema)))

(def env-var-prefix "ESQL_")

(defn key->env-var [key]
  (str env-var-prefix (-> key name (str/upper-case) (str/replace #"\W" "_"))))

(defn add-env-var-from-name [schema key]
  (if (:env-var (m/properties schema))
    schema
    (mu/update-properties schema assoc :env-var (key->env-var key))))

(defn add-env-var-defaults-to-keys
  "For every key add an environment variable."
  [schema]
  (util/update-schema-keys-with-fn schema add-env-var-from-name))

(defn if-boolean-0-arg [schema _]
  (if (= :boolean (m/type schema))
    (mu/update-properties schema assoc :arg-number 0)
    schema))

(defn boolean-handles-0-args
  "If key in a map has boolean type then in CLI it should not take any params."
  [schema]
  (util/update-schema-keys-with-fn schema if-boolean-0-arg))

(defn collect-args-into-vector [options {:keys [in]} _cli-args]
  (update-in options in concat _cli-args))

(defn replace-map-of-with-any-and-concat-vals
  [schema]
  (util/update-schema-keys-with-fn
    schema
    (fn [schema _]
      (if (= :map-of (m/type schema))
        (let [updated-props (assoc (m/properties schema) :update-fn collect-args-into-vector)]
          (m/schema [:any updated-props]))
        schema))))

(defn prepare [schema cli-schema-base]
  (-> schema
      (mu/dissoc :runtime_mappings)
      (mu/dissoc :catalog)
      (mu/merge cli-schema-base)
      (add-env-var-defaults-to-keys)
      (boolean-handles-0-args)
      (prepare-keys-for-cli)
      (replace-map-of-with-any-and-concat-vals)))
