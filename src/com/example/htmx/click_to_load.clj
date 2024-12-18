(ns com.example.htmx.click-to-load
  (:require [com.example.ui :as ui]
            [com.biffweb :as biff]))

(defn fetch-data [db n]
  (try
    (->> (biff/q db
                 '{:find  (pull e [*])
                   :where [[e :contact/email]]})
         (sort-by :contact/first-name)
         (partition-all 3)
         (#(nth % n)))
    (catch Exception _ nil)))

(defn load-button [n]
  [:tr {:id "replaceMe"}
   [:td
    (biff/form
     {:method    "get"
      :hx-get    (str "/click-to-load/contacts/" n)
      :hx-target "#replaceMe"
      :hx-swap   "outerHTML"}
     [:button.a {:class "btn btn-info"}
      "Load More ..."
      [:img {:class "htmx-indicator"
             :src   "https://htmx.org/img/bars.svg"}]])]])

(defn load-rows [{:keys [path-params biff/db]
                  :as   ctx}]
  (let [{:keys [n]} path-params
        n           (Integer/parseInt n)
        contacts    (fetch-data db n)]
    (if contacts
      (->
       (for [contact contacts]
         (let [{:contact/keys [first-name last-name status]} contact]
           [:tr
            [:td first-name]
            [:td last-name]
            [:td status]]))
       (concat [(load-button (inc n))])
       biff/render)
      (biff/render [:tr [:td [:button {:class    "btn"
                                       :disabled "disabled"} "No More Data ... "]]]))))


(defn show-table [contacts]
  [:div {:class "flex flex-col"}
   [:div {:class "overflow-x-auto"}
    [:table {:class "table"}
     [:thead
      [:tr
       [:th "First Name"]
       [:th "Last Name"]
       [:th "Status"]]]
     [:tbody
      (for [contact contacts]
        (let [{:contact/keys [first-name last-name status]} contact]
          [:tr
           [:td first-name]
           [:td last-name]
           [:td status]]))
      (load-button 1)]]]])

(defn app [{:keys [biff/db]
            :as   ctx}]
  (ui/page
   {}
   (show-table (fetch-data db 0))))

(def module
  {:routes ["/click-to-load"
            ["/" {:get app}]
            ["/contacts/:n" {:get load-rows}]]})
