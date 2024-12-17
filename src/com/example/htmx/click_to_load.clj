(ns com.example.htmx.click-to-load
  (:require [com.example.ui :as ui]
            [com.biffweb :as biff]))

;; TODO: app の初期データをセットしてから、fetch-data をゲットして入れるところから
(defn fetch-data [db n]
  (->> (biff/q db
               '{:find  (pull e [*])
                 :where [[e :contact/email]]})
       (sort-by :contact/first-name)
       (partition-all 3)
       (#(nth % n))))


(defn app [ctx]
  (ui/page
   {}
   [:p "Hello"]))

(def module
  {:routes ["/click-to-load"
            ["/" {:get app}]]})
