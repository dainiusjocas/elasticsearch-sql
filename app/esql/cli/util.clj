(ns esql.cli.util
  (:require [malli.util :as mu]))

(defn update-schema-keys-with-fn
  "Iterate through keys in the schema and apply f on the key schema."
  [schema f]
  (loop [schema schema
         [key & ks] (mu/keys schema)]
    (if key
      (recur (mu/update schema key f key) ks)
      schema)))
