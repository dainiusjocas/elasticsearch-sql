(ns clerk
  {:nextjournal.clerk/visibility {:code :hide :result :show}}
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk-slideshow :as slideshow]))

(clerk/serve! {:browse? true
               :port    7777})
