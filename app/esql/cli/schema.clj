(ns esql.cli.schema
  (:require [esql.cli.keys :as cli-keys]
            [elasticsearch.sql.schema :as sql-schema]
            [piotr-yuxuan.malli-cli :as malli-cli]))

(def cli-schema-base
  [:map {:closed                  true
         :decode/args-transformer malli-cli/args-transformer
         :description             "Configuration of params"}
   [:help {:optional true}
    [:boolean {:description  "Display usage summary and exit."
               :short-option "-h"
               :arg-number   0}]]
   [:dry-run {:optional true}
    [:boolean {:description "Prints configuration map with defaults."
               :arg-number  0}]]])

(def final
  (cli-keys/prepare sql-schema/final cli-schema-base))
