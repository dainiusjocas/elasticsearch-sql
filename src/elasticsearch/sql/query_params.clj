(ns elasticsearch.sql.query-params)

(defn prepare
  [{:keys [format delimiter]}]
  (cond-> {}
          (some? format) (assoc :format format)
          ; Only when format is CSV check for delimiter option
          (and (= "csv" format) delimiter)
          (assoc :delimiter delimiter)))
