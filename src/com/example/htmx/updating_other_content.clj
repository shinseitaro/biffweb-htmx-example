(ns com.example.htmx.updating-other-content
  (:require [com.biffweb :as biff]
            [com.example.ui :as ui]))

(defn fetch-data [db]
  (->> (biff/q db
               '{:find  (pull e [*])
                 :where [[e :contact/email]]})
       (sort-by :contact/first-name)))

(defn contact-table [])

(defn contacts "データを格納して contact-table でテーブルを再描画" [])

(defn app [{:keys [biff/db]
            :as   ctx}]
  (let [contacts (fetch-data db)]
    (ui/page
     {}
     [:h2 "Contacts"]
     [:div {:class "h-1"}]
     [:div {:class "overflow-x-auto"}
      [:table {:class "table"}
       [:thead
        [:tr
         [:th "First Name"]
         [:th "Last Name"]
         [:th "Email"]]]
       [:tbody {:id         "contacts-table"
                :hx-get     "/updating-other-content/contact/table"
                :hx-trigger "newContact from:body"}
        (for [contact contacts]
          (let [{:contact/keys [first-name last-name email]} contact]
            [:tr
             [:td first-name]
             [:td last-name]
             [:td email]]))]]]
     [:div {:class "h-3"}]
     (biff/form
      {:hx-post "/updating-other-content/contact"}
      [:div {:class "flex flex-col gap-1"}
       [:h2  "Add Contact"]
       [:input {:type        "text"
                :name        "first-name"
                :placeholder "First Name"}]

       [:input {:type        "text"
                :name        "last-name"
                :placeholder "Last Name"}]

       [:input {:type        "text"
                :name        "email"
                :placeholder "Email"}]
       [:div
        [:button {:class "btn"} "New Data"]]]))))


(def module
  {:routes ["/updating-other-content"
            ["/" {:get app}]
            ["/contacts" {:post contacts}]
            ["/contact/table" {:get contact-table}]]})

