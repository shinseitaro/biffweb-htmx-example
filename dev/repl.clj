(ns repl
  (:require [com.example :as main]
            [com.biffweb :as biff :refer [q]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [cheshire.core :as cheshire]))

(defn get-context []
  (biff/merge-context @main/system))

(defn add-fixtures []
  (biff/submit-tx (get-context)
                  (-> (io/resource "fixtures.edn")
                      slurp
                      edn/read-string)))

;; add json fixture data
(defn add-json-fixtures []
  (biff/submit-tx (get-context)
                  (map #(merge % {:db/doc-type :contact})
                       (-> (io/resource "contact.json")
                           slurp
                           (cheshire/parse-string true)))))

(defn check-config []
  (let [prod-config (biff/use-aero-config {:biff.config/profile "prod"})
        dev-config  (biff/use-aero-config {:biff.config/profile "dev"})
        ;; Add keys for any other secrets you've added to resources/config.edn
        secret-keys [:biff.middleware/cookie-secret
                     :biff/jwt-secret
                     :mailersend/api-key
                     :recaptcha/secret-key]
                     ; ...

        get-secrets (fn [{:keys [biff/secret] :as config}]
                      (into {}
                            (map (fn [k]
                                   [k (secret k)]))
                            secret-keys))]
    {:prod-config prod-config
     :dev-config dev-config
     :prod-secrets (get-secrets prod-config)
     :dev-secrets (get-secrets dev-config)}))

(comment
  ;; Call this function if you make a change to main/initial-system,
  ;; main/components, :tasks, :queues, config.env, or deps.edn.
  (main/refresh)

  ;; Call this in dev if you'd like to add some seed data to your database. If
  ;; you edit the seed data (in resources/fixtures.edn), you can reset the
  ;; database by running `rm -r storage/xtdb` (DON'T run that in prod),
  ;; restarting your app, and calling add-fixtures again.
  (add-fixtures)

  (add-json-fixtures)

  ;; query contact data
  (let [{:keys [biff/db] :as ctx} (get-context)]
    (q db
       '{:find (pull e [*])
         :where [[e :contact/email]]}))

  ;; Update an existing user's email address
  (let [{:keys [biff/db] :as ctx} (get-context)
        user-id (biff/lookup-id db :user/email "hello@example.com")]
    (biff/submit-tx ctx
                    [{:db/doc-type :user
                      :xt/id user-id
                      :db/op :update
                      :user/email "new.address@example.com"}]))

  (sort (keys (get-context)))

  ;; Check the terminal for output.
  (biff/submit-job (get-context) :echo {:foo "bar"})
  (deref (biff/submit-job-for-result (get-context) :echo {:foo "bar"})))


(comment
  ;; test to get a data 
  (biff/lookup (:biff/db (get-context)) :contact/email "mdeporte0@ox.ac.uk")
  :rcf)

(comment
  (biff/form {})
  :rcf)