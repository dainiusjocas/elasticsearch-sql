(ns esql.cli.summary
  (:require
    [clojure.string :as str]
    [malli.core :as m]
    [malli.util :as mu]
    [esql.cli.util :as util]))

(defn clean-description [schema _]
  (if-let [description (:description (m/properties schema))]
    (mu/update-properties schema assoc :description (str/replace description #"[\n\r]" " "))
    schema))

(defn remove-new-lines-from-descriptions
  "When descriptions include newlines then the printed summary looks bad.
  Replace newlines with spaces."
  [schema]
  (util/update-schema-keys-with-fn schema clean-description))

(defn fix-compound-keys [schema _]
  (if (#{:or :and :map-of} (m/type schema))
    [:any (m/properties schema)]
    schema))

(defn remove-compound-keys
  "With :or :and :map-of malli-cli prints multiple lines in the summary.
  Replace those schemas with :any schema.
  No harm, because the type is not visible."
  [schema]
  (util/update-schema-keys-with-fn schema fix-compound-keys))

(defn sort-keys
  "It looks nicer if keys are sorted.
  The trick is to iterate through sorted keys and dissoc-assoc them into the schema."
  [schema]
  (loop [schema schema
         [key & ks] (sort (mu/keys schema))]
    (if key
      (recur
        (let [key-schema (mu/get schema key)]
          (-> schema
              (mu/dissoc key)
              (mu/assoc key key-schema))) ks)
      schema)))

(defn prepare
  "Prepares schema for the summary generation so that it looks good when printed."
  [schema]
  (-> schema
      (remove-compound-keys)
      (remove-new-lines-from-descriptions)
      (sort-keys)))
