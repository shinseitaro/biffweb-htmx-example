(ns com.example.htmx.edit-row
  (:require [com.example.ui :as ui]
            [com.biffweb :as biff]))

(defn fetch-data [db]
  (->> (biff/q db
               '{:find  (pull e [*])
                 :where [[e :contact/email]]})
       (sort-by :contact/first-name)))

(defn edit-template [ctx]
  [:tr
   [:td [:input {:type        "text"
                 :name        "first-name"
                 :value       "first-name"
                 :class       "input input-bordered input-primary w-full max-w-xs"
                 :placeholder "first-name"}]]
   [:td [:input {:type        "text"
                 :name        "last-name"
                 :value       "last-name"
                 :class       "input input-bordered input-primary w-full max-w-xs"
                 :placeholder "last-name"}]]
   [:td [:input {:type        "text"
                 :name        "email"
                 :value       "email"
                 :class       "input input-bordered input-primary w-full max-w-xs"
                 :placeholder "email"}]]
   [:td {:class "flex flex-col gap-2"}
    [:button {:class "btn btn-xs btn-primary"} "cancel"]
    [:button {:class "btn btn-xs btn-secondary"} "save"]]])



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
          [:tr
           [:td {:name  "first-name"
                 :value first-name} first-name]
           [:td {:name  "last-name"
                 :value last-name} last-name]
           [:td {:name  "email"
                 :value email} email]
           [:td [:button {:class  "btn"
                          :hx-get "/edit-row/edit"} "Edit"]]]))]]]))


(def module
  {:routes ["/edit-row"
            ["/" {:get app}]
            ["/edit" {:get edit-template}]]})
