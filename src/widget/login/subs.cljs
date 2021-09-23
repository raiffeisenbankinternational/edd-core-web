(ns widget.login.subs
  (:require
    [widget.login.db :as db]
    [re-frame.core :as rf]
    [goog.crypt.base64 :as b64]
    [widget.login.utils :refer [json-parser]]))

(rf/reg-sub
  :init-login-db
  (fn [db]
    (merge db db/default-db)))

(rf/reg-sub
  ::username
  (fn [db]
    (:db/username db)))

(rf/reg-sub
  ::password
  (fn [db]
    (:db/pasword db)))

(rf/reg-sub
  ::dialog-visible
  (fn [db]
    (or (::db/dialog-visible db) false)))

(rf/reg-sub
  ::form-type
  (fn [db]
    (::db/form-type db)))

(rf/reg-sub
  ::user-name
  (fn [{:keys [auth]}]
    (let [decoded (b64/decodeString
                    (second
                      (clojure.string/split (:id-token auth) #"\.")))]
      (when
        (some? auth) (-> (json-parser false true decoded)
                         (:email))))))

(rf/reg-sub
  ::confirmation-visible
  (fn [db]
    (::db/confirmation-visible db)))

(rf/reg-sub
  ::error-message-visible
  (fn [db]
    (::db/error-message-visible db)))

(rf/reg-sub
  ::error-message
  (fn [db]
    (::db/error-message db)))


