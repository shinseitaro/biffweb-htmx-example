(ns com.example.htmx.bulk-update
  (:require [com.example.ui :as ui]
            [com.biffweb :as biff]))

(defn fetch-data [db]
  (->> (biff/q db
               '{:find  (pull e [*])
                 :where [[e :contact/email]]})
       (sort-by :contact/first-name)
       (take 4)))

(defn show-table [contacts]
  [:div {:class "flex flex-col"}

   (biff/form
    {:hx-post "/bulk-update/users"
     :hx-swap "outerHTML"}
    [:div {:class "overflow-x-auto"}
     [:table {:class "table"}
      [:thead
       [:tr
        [:th "First Name"]
        [:th "Last Name"]
        [:th "Status"]
        [:th "Update status"]]]
      [:tbody
       (for [contact contacts]
         (let [{:contact/keys [first-name last-name status]} contact
               id                                            (:xt/id contact)]
           [:tr
            [:td first-name]
            [:td last-name]
            [:td status]
            [:th [:label [:input {:type    "checkbox"
                                  :class   "checkbox"
                                  :checked status
                                  :name    "id"
                                  :value   id}]]]]))]]]
    [:button {:class "btn"} "Bulk Update"])])



(defn update-status [{:keys [params biff/db]
                      :as   ctx}]
  (let [id              (:id params)
        current-contact (fetch-data db)
        id              (cond (nil? id) [nil]
                              (string? id) [(parse-uuid id)]
                              (coll? id) (map parse-uuid id)
                              :else nil)]

    (->>
     (map (fn [contact]
            (if (.contains id (:xt/id contact))
              {:db/doc-type    :contact
               :db/op          :update
               :xt/id          (:xt/id contact)
               :contact/status true}
              {:db/doc-type    :contact
               :db/op          :update
               :xt/id          (:xt/id contact)
               :contact/status false})) current-contact)
     (biff/submit-tx ctx))

    (->>
     current-contact
     (map (fn [contact]
            (if (.contains id (:xt/id contact))
              (assoc contact :contact/status true)
              (assoc contact :contact/status false))))
     show-table
     biff/render)))



(defn app [{:keys [biff/db]
            :as   ctx}]
  (ui/page
   {}
   (show-table (fetch-data db))))

(def module
  {:routes ["/bulk-update"
            ["/" {:get app}]
            ["/users" {:post update-status}]]})


