(ns com.example.htmx.click-to-edit
  (:require [com.example.ui :as ui]
            [com.biffweb :as biff]
            [cheshire.core :as cheshire]
            [com.example.email :as email]))


(defn fetch-a-contact [db]
  (biff/lookup db :contact/email "mdeporte0@ox.ac.uk"))

(defn show-contact [first-name last-name email]
  [:div {:hx-target "this"}
   [:div [:label "First Name: "]  first-name]
   [:div [:label "Last Name: "]  last-name]
   [:div [:label "Email: "]  email]
   [:button {:class  "btn btn-info"
             :hx-get "/click-to-edit/edit"} "Click To Edit"]])

(defn update-a-contact [{:keys [params]
                         :as   ctx}]
  (let [{:keys [id first-name last-name email]} params]
    (biff/submit-tx ctx
                    [{:db/doc-type  :contact
                      :xt/id        (parse-uuid id)
                      :db.op/upsert {:contact/first-name first-name
                                     :contact/last-name  last-name
                                     :contact/email      email}}])
    (biff/render (show-contact first-name last-name email))))

(defn edit-contact [{:keys [biff/db]
                     :as   ctx}]
  (let [{:keys [contact/first-name contact/last-name contact/email xt/id]} (fetch-a-contact db)]
    (biff/form
     {}
     [:div {:hx-target "this"}
      [:label "First Name "]
      [:input {:type  "text"
               :name  "first-name"
               :value first-name}]]
     [:div
      [:label "Last Name "]
      [:input {:type  "text"
               :name  "last-name"
               :value last-name}]]
     [:div
      [:label "Email "]
      [:input {:type  "text"
               :name  "email"
               :value email}]]

     [:div {:class "flex p-2 gap-2"}
      [:button {:class  "btn btn-primary"
                :hx-get "/click-to-edit"} "cancel"]
      [:button {:class   "btn btn-primary"
                :hx-post "/click-to-edit/save"
                :hx-vals (cheshire/generate-string {:id id})} "save"]])))


(defn app [{:keys [biff/db]
            :as   ctx}]
  (let [{:contact/keys [first-name last-name email]} (fetch-a-contact db)]
    (ui/page
     {}
     (show-contact first-name last-name email))))

(def module
  {:routes ["/click-to-edit"
            ["" {:get app}]
            ["/edit" {:get edit-contact}]
            ["/save" {:post update-a-contact}]]})


