(ns sql-schema
  (:require [cheshire.core :as json])
  (:import (java.time ZoneId)))

(defn spec [spec-file]
  (json/parse-string (slurp spec-file) true))

(defn get-endpoint-spec
  "Given a raw spec and the endpoint name returns the endpoint spec"
  [spec endpoint-name]
  (first (filter (fn sql-endpoint? [endpoint]
                   (= endpoint-name (:name endpoint)))
                 (:endpoints spec))))

(def id-type
  (first (filter (fn sql-request-response?
                   [type]
                   (and (= "_types" (get-in type [:name :namespace]))
                        (= "Id" (get type :kind))))
                 (:types spec))))

(def DurationSchema
  [:or
   [:enum -1 0 "-1" "0"]
   [:re "^\\d+(nanos|micros|ms|s|m|h|d)$"]])

(def TimezoneSchema
  [:and
   :string
   [:enum (vector (ZoneId/getAvailableZoneIds))]])

(defn type->schema [type]
  (cond
    (and (= "instance_of" (:kind type))
         (= "string" (get-in type [:type :name]))) :string
    (and (= "instance_of" (:kind type))
         (= "Id" (get-in type [:type :name]))) :string
    (and (= "instance_of" (:kind type))
         (= "integer" (get-in type [:type :name]))) :int
    (and (= "instance_of" (:kind type))
         (= "boolean" (get-in type [:type :name]))) :boolean
    (and (= "instance_of" (:kind type))
         (= "Duration" (get-in type [:type :name]))) DurationSchema
    (and (= "instance_of" (:kind type))
         (= "TimeZone" (get-in type [:type :name]))) TimezoneSchema
    (= "dictionary_of" (:kind type)) [:map-of :string :any]
    (= "array_of" (:kind type)) [:vector :any]
    :else (do #_(println (str "MISSING TYPE>>>>" type)) :any)))

(defn es-type-spec->malli-schema [type-spec]
  (into
    [:map {:description (:description type-spec)}]
    (mapv (fn [prop]
            [(keyword (:name prop))
             (let [key-props {}]
               (if (= false (:required prop))
                 (assoc key-props :optional true)
                 key-props))
             (let [value-props (cond-> {}
                                       (some? (:description prop))
                                       (assoc :description (:description prop))
                                       (some? (:serverDefault prop))
                                       (assoc :default (:serverDefault prop)))
                   type (type->schema (:type prop))]
               (if (vector? type)
                 (let [[h & t] type]
                   (into [h value-props] t))
                 [type value-props]))])
          (:properties (:body type-spec)))))

(defn get-type [spec type-ref]
  (first
    (filter (fn matches-type-ref? [type]
              (= type-ref (:name type)))
            (:types spec))))

(defn get-request-spec [spec endpoint-spec]
  (let [request (:request endpoint-spec)]
    (get-type spec request)))

(def sql-endpoint-spec-name "sql.query")

(defn parse
  [^String spec-file]
  (let [spec (spec spec-file)
        endpoint-spec (get-endpoint-spec spec sql-endpoint-spec-name)
        sql-request-spec (get-request-spec spec endpoint-spec)]
    {:endpoint endpoint-spec
     :request  (es-type-spec->malli-schema sql-request-spec)}))

;; Given Elasticsearch specification we want to get out the malli schema for one endpoint
