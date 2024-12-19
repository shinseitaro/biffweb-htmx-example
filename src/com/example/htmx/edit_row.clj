(ns com.example.htmx.edit-row
  (:require [com.example.ui :as ui]
            [com.biffweb :as biff]
            [cheshire.core :as cheshire]))

(defn fetch-data [db]
  (->> (biff/q db
               '{:find  (pull e [*])
                 :where [[e :contact/email]]})
       (sort-by :contact/first-name)))

(defn edit-template [{:keys [params]
                      :as   ctx}]
  (let [{:keys [id first-name last-name email]} params]
    [:tr
     [:td [:input {:type  "text"
                   :name  "first-name"
                   :value first-name
                   :class "input input-bordered input-primary w-full max-w-xs"}]]
     [:td [:input {:type  "text"
                   :name  "last-name"
                   :value last-name
                   :class "input input-bordered input-primary w-full max-w-xs"}]]
     [:td [:input {:type  "text"
                   :name  "email"
                   :value email
                   :class "input input-bordered input-primary w-full max-w-xs"}]]
     [:td {:class "flex flex-col gap-2"}
      [:button {:class   "btn btn-xs btn-primary"
                :hx-get  "/edit-row/cancel"
                :hx-vals (cheshire/generate-string {:id         id
                                                    :first-name first-name
                                                    :last-name  last-name
                                                    :email      email})} "cancel"]
      [:button {:class      "btn btn-xs btn-secondary"
                :hx-post    "/edit-row/update"
                :hx-include "closest tr"
                :hx-vals    (cheshire/generate-string {:id id})} "save"]]]))

(defn tr [id first-name last-name email]
  [:tr
   [:td first-name]
   [:td last-name]
   [:td email]
   [:td [:button {:class   "btn"
                  :hx-get  "/edit-row/edit"
                  :hx-vals (cheshire/generate-string {:id         id
                                                      :first-name first-name
                                                      :last-name  last-name
                                                      :email      email})} "Edit"]]])
(defn app [{:keys [biff/db]}]
  (ui/page
   {}
   [:div {:class "flex"}
    [:table {:class "table w-full"}
     [:thead
      [:tr
       [:th "First Name"]
       [:th "Last Name"]
       [:th "Email"]
       [:th ""]]]
     [:tbody {:hx-target "closest tr" ;; 各Editボタンに最も近い tr を
              :hx-swap   "outerHTML"} ;; サーバから返ったデータで置き換える 
      (for [contact (fetch-data db)]
        (let [{:contact/keys [first-name last-name email]} contact
              id                                           (:xt/id contact)]
          (tr id first-name last-name email)))]]]))

(defn cancel [{:keys [params]
               :as   ctx}]
  (let [{:keys [id first-name last-name email]} params]
    (biff/render (tr id first-name last-name email))))

(defn update [{:keys [params]
               :as   ctx}]
  (let [{:keys [id first-name last-name email]} params]
    (biff/submit-tx ctx [{:db/doc-type       :contact
                          :xt/id             (parse-uuid id)
                          :db.op/upsert      {:contact/first-name first-name}
                          :contact/last-name last-name
                          :contact/email     email}])
    (biff/render (tr id first-name last-name email))))

(def module
  {:routes ["/edit-row"
            ["/" {:get app}]
            ["/edit" {:get edit-template}]
            ["/cancel" {:get cancel}]
            ["/update" {:post update}]]})
