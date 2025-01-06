(ns com.example.htmx.updating-other-content
  (:require [com.biffweb :as biff]
            [com.example.ui :as ui]))


(defn fetch-data [db]
  (->> (biff/q db
               '{:find  (pull e [*])
                 :where [[e :contact/email]]})
       (sort-by :contact/first-name)))

(defn contact-list [db]
  ;;  解決方法１：ターゲットを拡張
  [:div {:id "table-and-form"}
   [:h2 "Contacts"]
   [:div {:class "h-1"}]
   (let [contacts (fetch-data db)]
     [:div {:class "overflow-x-auto"}
      [:table {:class "table"}
       [:thead
        [:tr
         [:th "First Name"]
         [:th "Last Name"]
         [:th "Email"]]]
       [:tbody {:id "contacts-table"}
        (for [contact contacts]
          (let [{:contact/keys [first-name last-name email]} contact]
            [:tr
             [:td first-name]
             [:td last-name]
             [:td email]]))]]])
   [:div {:class "h-3"}]
   (biff/form
    {:hx-post   "/updating-other-content/contacts"
     :hx-target "#table-and-form"} ;; コンタクトテーブルを target にする
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
      [:button {:class "btn"} "New Data"]]])])

(defn submit-new-contact [ctx first-name last-name email]
  (biff/submit-tx ctx
                  [{:db/doc-type  :contact
                    :db.op/upsert {:contact/first-name first-name
                                   :contact/last-name  last-name
                                   :contact/email      email
                                   :contact/status     true}}]))

(defn new-contact [{:keys [params]
                    :as   ctx}]
  (let [{:keys [first-name last-name email]} params]
    (submit-new-contact ctx first-name last-name email))
  (biff/render (contact-list (:biff/db (biff/merge-context ctx)))))

(defn app [{:keys [biff/db]
            :as   ctx}]
  (ui/page
   {}
   (contact-list db)))

(def module
  {:routes ["/updating-other-content"
            ["/" {:get app}]
            ["/contacts" {:post new-contact}]]})  

