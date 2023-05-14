(ns clerk
  {:nextjournal.clerk/visibility {:code :hide :result :show}}
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk-slideshow :as slideshow]))

(clerk/add-viewers! [slideshow/viewer])

(clerk/serve! {:browse?     true
               :port        7777
               :watch-paths ["docs"]})

#_(clerk/build! {:paths ["docs/presentation.md"]})
