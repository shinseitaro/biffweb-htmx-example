(ns com.example.htmx.delete-row
  (:require [com.example.ui :as ui]
            [com.biffweb :as biff]))

(defn fetch-data [db]
  (->> (biff/q db
               '{:find  (pull e [*])
                 :where [[e :contact/email]]})
       (sort-by :contact/first-name)))


(defn delete-contact [{:keys [path-params]
                       :as   ctx}]
  (biff/submit-tx ctx
                  [{:xt/id (parse-uuid (:id path-params))
                    :db/op :delete}])
  [:<>])

(defn app [{:keys [biff/db]
            :as   ctx}]
  (let [contacts (fetch-data db)]
    (ui/page
     {}
     [:div {:class "overflow-x-auto"}
      [:table {:class "table"}
       [:thead
        [:tr
         [:th "First Name"]
         [:th "Last Name"]
         [:th ""]]]
       [:tbody {:hx-confirm "are you sure?"
                :hx-target  "closest tr"
                :hx-swap    "outerHTML"}
        (for [contact contacts]
          (let [{:contact/keys [first-name last-name]} contact
                id                                     (:xt/id contact)]
            [:tr
             [:td first-name]
             [:td last-name]
             [:td [:button {:class     "btn btn-warning"
                            :hx-delete (str "/delete-row/delete/" id)} "DELETE"]]]))]]])))

(def module
  {:routes ["/delete-row"
            ["/" {:get app}]
            ["/delete/:id" {:delete delete-contact}]]})
