(ns build
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

;; Library deployment to Clojars
(def lib 'lt.jocas/elasticsearch-sql)
(defn- the-version [patch] (format "1.0.%s" patch))
(def version (the-version (b/git-count-revs nil)))
(def snapshot (the-version "999-SNAPSHOT"))

(defn deploy "Deploy the JAR to Clojars." [opts]
  (-> opts
      (assoc :lib lib :version (if (:snapshot opts) snapshot version))
      (bb/clean)
      (assoc :src-pom "pom.xml.template")
      (bb/jar)
      (bb/deploy)))

(defn trigger-library-release
  "Creates a git tag `lib-vVERSION` and pushes it to origin.
  This will trigger GH Action to release to Clojars."
  [opts]
  (let [tag (str "lib-v" (if (:snapshot opts) snapshot version))]
    (println "Initiating release for git tag:" tag)
    (b/git-process {:git-args ["tag" tag]})
    (b/git-process {:git-args ["push" "origin" tag]})))

;; App release related scripts
(defn trigger-app-release
  "Creates a git tag `vVERSION` and pushes it to origin.
  This will trigger GH Action to release the CLI app."
  [opts]
  (let [tag (str "vv" (if (:snapshot opts) snapshot version))]
    (println "Initiating release for git tag:" tag)
    (b/git-process {:git-args ["tag" tag]})
    (b/git-process {:git-args ["push" "origin" tag]})))

;; Uberjar stuff

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"
                            :aliases [:app]}))
(defn clean [_]
  (b/delete {:path "target"}))

(defn uberjar [_]
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
           :conflict-handlers {}}))
