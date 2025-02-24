(ns com.example.htmx.updating-other-content
  (:require [com.biffweb :as biff]
            [com.example.ui :as ui]))

(defn fetch-data [db]
  (->> (biff/q db
               '{:find  (pull e [*])
                 :where [[e :contact/email]]})
       (sort-by :contact/first-name)))

(defn submit-new-contact [ctx first-name last-name email]
  (biff/submit-tx ctx
                  [{:db/doc-type  :contact
                    :db.op/upsert {:contact/first-name first-name
                                   :contact/last-name  last-name
                                   :contact/email      email
                                   :contact/status     true}}]))

(defn contact-table [{:keys [biff/db]
                      :as   ctx}]
  [:tbody {:id         "contacts-table"
           :hx-get     "/updating-other-content/contact/table"
           :hx-trigger "newContact from:body"}
   (for [contact (fetch-data db)]
     (let [{:contact/keys [first-name last-name email]} contact]
       [:tr
        [:td first-name]
        [:td last-name]
        [:td email]]))])

(defn new-contact [{:keys [params]
                    :as   ctx}]
  (let [{:keys [first-name last-name email]} params]
    (submit-new-contact ctx first-name last-name email)
    ;;  https://clojuredocs.org/clojure.core/merge-with#example-55181fece4b08eb9aa0a8d3c
    (merge-with merge (biff/render [:<>]) {:headers {"HX-Trigger" "newContact"}})))


(defn app [{:keys [biff/db]
            :as   ctx}]
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
     (contact-table ctx)]]

   [:div {:class "h-3"}]
   (biff/form
    {:hx-post "/updating-other-content/contact"
     :hx-swap "none"}
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
      [:button {:class "btn"} "New Data"]]])))


(def module
  {:routes ["/updating-other-content"
            ["/" {:get app}]
            ["/contact" {:post new-contact}]
            ["/contact/table" {:get contact-table}]]})

