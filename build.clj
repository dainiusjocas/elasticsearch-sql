(ns build
  (:require [clojure.tools.build.api :as b]))

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"
                            :aliases [:app]}))
(defn clean [_]
  (b/delete {:path "target"}))

(defn uberjar [_]
  (println "Generating uberjar...")
  (clean nil)
  (b/copy-dir {:src-dirs   ["app" "src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis     basis
                  :src-dirs  ["app" "src"]
                  :class-dir class-dir})
  (b/uber {:class-dir         class-dir
           :uber-file         "target/esql.jar"
           :basis             basis
           :main              'esql.main
           :conflict-handlers {}})
  (println "Uberjar is ready!"))
