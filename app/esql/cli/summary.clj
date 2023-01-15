(ns esql.cli.summary
  (:require
    [clojure.string :as str]
    [malli.core :as m]
    [malli.util :as mu]))

(defn remove-new-lines-from-descriptions
  "When descriptions include newlines then summary looks bad when printed.
  Replace newlines with space"
  [schema]
  (loop [schema schema
         [key & ks] (mu/keys schema)]
    (if key
      (recur
        (mu/update schema key
                   mu/update-properties assoc
                   :description (when-let [description (:description (m/properties (mu/get schema key)))]
                                  (str/replace description #"[\n\r]" " ")))
        ks)
      schema)))

(defn remove-compound-keys
  "With :or :and :map-of malli-cli prints multiple lines in the summary.
  Replace those schemas with :any schema.
  No harm, because the type is not visible."
  [schema]
  (loop [schema schema
         [key & ks] (mu/keys schema)]
    (if key
      (recur
        (mu/update
          schema key
          (fn compount-leaf-schema-to-any-preserving-properties [s]
            (if (#{:or :and :map-of} (m/type s))
              [:any (m/properties s)]
              s)))
        ks)
      schema)))

(defn sort-keys
  "It looks nicer if keys are sorted."
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
