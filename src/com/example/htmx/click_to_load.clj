(ns com.example.htmx.click-to-load
  (:require [com.example.ui :as ui]
            [com.biffweb :as biff]))


(defn app [ctx]
  (ui/page
   {}
   [:p "Hello"]))

(def module
  {:routes ["/click-to-load"
            ["/" {:get app}]]})
