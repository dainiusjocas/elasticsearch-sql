(ns ^:nextjournal.clerk/no-cache app
  {:nextjournal.clerk/visibility {:code :hide :result :hide}}
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as v]
            [nextjournal.clerk-slideshow :as slideshow]))


{:nextjournal.clerk/visibility {:code :hide :result :hide}}

(def text-input
  ;; We're telling Clerk to opt-out of the default behaviour (acting on the value held by
  ;; the var) and return a map containing the actual var object instead.
  {:var-from-def? true

   ;; When we specify a `:transform-fn`, it gets run on the JVM side
   ;; to pre-process our value before sending it to the front-end. In
   ;; this case we want to send the symbol for the var along with the
   ;; unwrapped value because our custom renderer will need to know
   ;; both of those things (see below).
   ;;
   ;; Normally, Clerk's front-end is very careful to fetch data from
   ;; the JVM is bite-sized chunks to avoid killing the browser. But
   ;; sometimes we need to override that mechanism, which is done by
   ;; calling `clerk/mark-presented` to ask Clerk to send the whole
   ;; value as-is to the browser.
   :transform-fn  (comp clerk/mark-presented
                        (clerk/update-val (fn [{::clerk/keys [var-from-def]}]
                                            {:var-name (symbol var-from-def) :value @@var-from-def})))

   ;; The `:render-fn` is the heart of any viewer. It will be executed
   ;; by a ClojureScript runtime in the browser, so — unlike these
   ;; other functions — we must quote it for transmission over the
   ;; wire.
   ;;
   ;; The interactive two-way binding magic comes from using Clerk's
   ;; support for Hiccup to produce an input tag whose `:on-input`
   ;; handler calls `clerk-eval` to send a quoted form back to the
   ;; JVM. This ability to easily transmit arbitrary code between the
   ;; front- and back- ends of the system is extremely
   ;; powerful — thanks, Lisp!
   :render-fn     '(fn [{:keys [var-name value]}]
                     [:input {:type          :text
                              :placeholder   "⌨️"
                              :value         value          ; Preserver value between refreshes
                              :initial-value value
                              :class         "px-3 py-3 placeholder-blueGray-300 text-blueGray-600 relative bg-white bg-white rounded text-sm border border-blueGray-300 outline-none focus:outline-none focus:ring w-full"
                              :on-input      #(v/clerk-eval `(reset! ~var-name ~(.. % -target -value)))}])})

(comment
  (clerk/html
    [:button.bg-sky-500.hover:bg-sky-700.text-white.rounded-xl.px-2.py-1
     {:on-click '(fn show-defaults [& args] (println args "<<<<"))}
     "Defaults"])

  (clerk/html
    [:textarea.resize.rounded-md.border-2.border-black
     {:rows        5
      :placeholder "{}"}
     "Should be the value but not visible"]))

^{::clerk/viewer     text-input
  ::clerk/sync       true
  ::clerk/visibility {:code :hide :result :show}}
(defonce text-state (atom "asdads"))

(v/with-viewer text-input nil #'text-state)

^{:nextjournal.clerk/visibility {:code :hide :result :show}}
(clerk/row
  {::clerk/opts {:width "100%"}}
  (clerk/col {::clerk/opts {:width 150}}
             (clerk/row (clerk/html [:span.text-left "Query"]))
             (clerk/row (clerk/html [:span.text-left "Conf"]))
             (clerk/row (clerk/html [:span.text-left "Docs"]))
             (clerk/row (clerk/html [:span.text-left "TODO"])))
  (clerk/col))


;; ## Query

;; ---

;; ## Config

;; ---

;; ## Docs

;; ---

;; ## TODOS

;; TODO: ui component that shows or hides based on button click

;; TODO: Show/hide the config table with a button click

;; TODO: Fill the hideable text input field with the default configuration values

;; TODO: Text input field to type the SQL query

;; TODO: show the resulting data in the frontend

