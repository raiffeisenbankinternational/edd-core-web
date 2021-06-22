(ns edd.events
  (:import goog.history.Html5History)
  (:require
    [re-frame.core :as rf]
    [edd.db :as db]))

(rf/reg-event-db
  ::initialize-db
  (fn [db [_ event]]
    (-> db/default-db
        (merge db)
        (assoc :config event))))


(rf/reg-event-fx
  ::set-active-panel
  (fn [{:keys [db]} [_ page & [params]]]
    {:db       (assoc db ::db/active-panel page
                         ::db/drawer false)
     :dispatch [(keyword (str "initialize-" (name page) "-db"))
                params]}))

(rf/reg-event-db
  ::toggle-drawer
  (fn [db _]
    (update db ::db/drawer #(not %))))

(rf/reg-event-db
  ::change-panel
  (fn [db [_ panel]]
    (-> db
        (assoc-in [::db/active-panel] panel)
        (update ::db/drawer #(not %)))))

(rf/reg-event-db
  ::change-language
  (fn [db [_ value]]
    (assoc db ::db/selected-language value)))

(rf/reg-event-db
  :menu-toggle
  (fn [db event]
    (update-in db [::db/menu-expanded (second event)] #(not %))))

(rf/reg-event-db
  ::add-translation
  (fn [db [_ body]]
    (update-in db [::db/translations] #(merge % body))))

(rf/reg-event-db
  :navigate
  (fn [db event]
    (assoc db ::db/drawer false)))


(rf/reg-event-db
  ::register-menu-item
  (fn [db [_ {:keys [key] :as item}]]
    (assoc db [::db/menu key] item)))

(rf/reg-event-db
 ::remove-user
 (fn [db]
   (assoc-in db [::db/user] nil)))


