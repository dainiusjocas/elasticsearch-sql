(ns elasticsearch.sql.body
  (:require [cheshire.core :as json]
            [clj-yaml.core :as yaml]
            [clojure.java.io :as io])
  #?(:clj (:import (java.io InputStream))))

(defn prepare-filter-param [filter-param]
  (if (string? filter-param)
    (json/parse-string filter-param)
    filter-param))

(defn prep-initial [{:keys [index_include_frozen filter params columnar
                            query fetch_size field_multi_value_leniency
                            keep_alive keep_on_completion page_timeout
                            request_timeout time_zone wait_for_completion_timeout]}]
  (cond-> {:query query}
          (some? fetch_size)
          (assoc :fetch_size fetch_size)
          (some? field_multi_value_leniency)
          (assoc :field_multi_value_leniency field_multi_value_leniency)
          (some? columnar)
          (assoc :columnar columnar)
          (some? params)
          (assoc :params params)
          (some? filter)
          (assoc :filter (prepare-filter-param filter))
          (some? index_include_frozen)
          (assoc :index_include_frozen index_include_frozen)
          (some? keep_alive)
          (assoc :keep_alive keep_alive)
          (some? keep_on_completion)
          (assoc :keep_on_completion keep_on_completion)
          (some? page_timeout)
          (assoc :page_timeout page_timeout)
          (some? request_timeout)
          (assoc :request_timeout request_timeout)
          (some? time_zone)
          (assoc :time_zone time_zone)
          (some? wait_for_completion_timeout)
          (assoc :wait_for_completion_timeout wait_for_completion_timeout)))

(defn decode [{{:keys [content-type]} :headers resp-body :body}]
  (cond (re-matches #"^text/.*" content-type)
        resp-body
        (= "application/json" content-type)
        (json/parse-string resp-body true)
        (= "application/cbor" content-type)
        #?(:bb  nil
           :clj (json/parse-cbor (.readAllBytes ^InputStream resp-body) true))
        (= "application/smile" content-type)
        #?(:bb  nil
           :clj (json/parse-smile (.readAllBytes ^InputStream resp-body) true))
        (= "application/yaml" content-type)
        (yaml/parse-stream (io/reader resp-body))
        :else resp-body))

(defn apply-rf [rf state body]
  (if (string? body)
    ; Nothing to do, pass it through reducing function
    (rf state body)
    ; Structured data: first pass columns, then rows to reducing function.
    (do
      (when-let [columns (:columns body)]
        (rf state columns))
      (if-let [rows (seq (:rows body))]
        (rf state rows)
        (when-let [values (seq (:values body))]
          (rf state values))))))
