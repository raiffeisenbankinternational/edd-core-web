(ns edd.client
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [ajax.core :as ajax]
   [ajax.json :as ajax.json]
   [ajax.protocols :refer [-body -get-response-header -get-all-headers]]
   [ajax.interceptors :refer [map->ResponseFormat]]
   [day8.re-frame.http-fx :refer [http-effect]]
   [edd.json :as json]
   [edd.db :as db]
   [edd.events :as events]
   [clojure.string :as string]
   [day8.re-frame.http-fx :refer [http-effect]]))

(def interaction-id
  (if
    (and (exists? js/params) (.-interactionId js/params))
    (.-interactionId js/params)
    (str "#" (random-uuid))))

(defn service-uri [db service path]
  (str "https://" (name service) "." (get-in db [:config :HostedZoneName]) path))

(defn add-user
  [req db]
  (assoc req
         :user
         {:selected-role (get-in db [::db/user :selected-role])}))

(defn add-headers
  [db req]
  (merge {"X-Authorization" (get-in db [::db/user :id-token])
          "Accept"          "*/*"
          "Content-Type"    "application/json"}
         req))

(defn add-get-headers
  [db req]
  (merge {"X-Authorization" (get-in db [::db/user :id-token])
          "Accept"          "*/*"}
         req))

(defn add-put-headers
  [db req]
  (merge {"X-Authorization" (get-in db [::db/user :id-token])
          "Accept"          "*/*"}
         req))

(defn fetch
  [uri params]
  (.fetch js/window
          uri
          (clj->js params)))

(defn query
  [db {:keys [query service on-success on-failure]}]
  (let [uri (service-uri db service (str "/query"
                                         "?dbg_service=" service
                                         "&dbg_qid=" (:query-id query)))
        ref {:request-id     (str "#" (random-uuid))
             :interaction-id interaction-id
             :query          query}
        body-str (clj->js (json/encode-custom-fields (add-user ref db)))]
    (fetch uri {:method          :post
                :mode            :cors
                :body            (.stringify js/JSON body-str)
                :timeout         20000
                :response-format (json/custom-response-format {:keywords? true})
                :headers         (add-headers db {})
                :on-success      on-success
                :on-failure      on-failure})))

(defn commands
  [db {:keys [commands service on-success on-failure]}]
  (let [uri (service-uri db service (str "/command"
                                         "?dbg_service=" service
                                         "&dbg_cmds=" (string/join "," (map :cmd-id commands))))
        ref {:request-id     (str "#" (random-uuid))
             :interaction-id interaction-id
             :commands       commands}
        body-str (clj->js (json/encode-custom-fields (add-user ref db)))]
    (fetch uri {:method          :post
                :mode            :cors
                :body            (.stringify js/JSON body-str)
                :timeout         20000
                :response-format (json/custom-response-format {:keywords? true})
                :headers         (add-headers db {})
                :on-success      on-success
                :on-failure      on-failure})))

(defn call [data]
  (let [db @re-frame.db/app-db]
    (cond
      (:query data) (query db data)
      (:commands data) (commands db data)
      (:command data) (commands db (assoc data
                                          :commands
                                          [(:command data)]))
      :else nil)))

(defn json-and-header-response
  []
  (map->ResponseFormat
   {:read         (fn json-read-response-format [xhrio]
                    (.log js/console xhrio)
                    {:body       (ajax.json/read-json-native nil true (-body xhrio))
                     :version-id (-get-response-header xhrio "versionid")
                     :headers    (-get-all-headers xhrio)})
    :description  "JSON with headers"
    :content-type ["application/json"]}))

(rf/reg-event-fx
 ::save-success
 (fn [_ [_ on-success result]]
   (println result)
   (println on-success)
   {:dispatch (conj on-success {:version-id (:version-id result)
                                :id         (get-in result [:body :result :id])})}))

(rf/reg-event-fx
 ::save-failure
 (fn [_ [_ on-failure]]
   (println on-failure)
   {:dispatch [on-failure]}))

(rf/reg-fx
 :load
 (fn [{:keys [ref service on-success on-failure]}]
   (let [db @re-frame.db/app-db
         method :get
         uri (str
              "https://" (name :glms-document-svc)
              "." (get-in db [:config :HostedZoneName])
              "/load/" (name service)
              "/" ref)]
     (http-effect
      {:method          method
       :uri             uri
       :timeout         50000
       :response-format (ajax/raw-response-format)
       :headers         (add-get-headers db {})
       :on-success      on-success
       :on-failure      on-failure}))))

(defn fetch-document
  [{:keys [data service]}]
  (let [db @re-frame.db/app-db
        uri (str
             "https://" (name :glms-document-svc)
             "." (get-in db [:config :HostedZoneName])
             "/save/" (name service)
             "/" (random-uuid))]
    (fetch uri
           {:mode    "cors"
            :method  "PUT"
            :headers (add-put-headers db {})
            :body    data})))

(defn- handle-invalid-jwt []
  (print "invalid token")
  (rf/dispatch [::events/remove-user]))

(defn- handle-versioning-error [call]
  (-> call
      (assoc :error :wrong-version)
      (dissoc :body)))

(defn- handle-error [call itm]
  (-> call
      (assoc :error (json/parse-custom-fields (:error itm)))
      (dissoc :body)))

(defn read-responses
  [items
   responses & {:keys [on-success on-failure response-filter]}]
  (let [values (mapv
                (fn [r]
                  {:version-id (-> r
                                   (.-headers)
                                   (.get "versionid"))
                   :status     (.-status r)
                   :body       (.json r)})
                (keep identity responses))
        failed (first
                (filter
                 #(not= (:status %)
                        200)
                 values))]
    (if-not failed
      (-> (js/Promise.all
           (clj->js
            (map
             #(:body %)
             values)))
          (.then (fn [bodies]
                   (let [event-body
                         (vec
                          (map-indexed
                           (fn [idx itm]
                             (cond
                               (= ":invalid" (get-in itm [:error :jwt])) (handle-invalid-jwt)
                               (= "Wrong version" (:error itm)) (handle-versioning-error (get values idx))
                               (contains? itm :error) (handle-error (get values idx) itm)
                               :else (-> (get values idx)
                                         (assoc :result (:result itm))
                                         (#(if response-filter
                                             (response-filter %)
                                             %))
                                         (assoc :item (get items idx))
                                         (dissoc :body))))
                           (js->clj bodies :keywordize-keys true)))
                         successes (doall
                                    (mapv
                                     (fn [it]
                                       (let [item (:item it)
                                             {:keys [on-success]} item
                                             result (:result it)]
                                         (if (and
                                              (some some? event-body)
                                              result)
                                           (do (when on-success
                                                 (rf/dispatch (vec (concat on-success [it]))))
                                               true)
                                           (do (when on-failure
                                                 (rf/dispatch (vec (concat on-failure [it]))))
                                               false))))
                                     event-body))]
                     (if (some false? successes)
                       (rf/dispatch (vec (concat on-failure event-body)))
                       (rf/dispatch (vec (concat on-success event-body))))))))
      (rf/dispatch on-failure))))

(defn save-n
  [items & {:keys [on-success on-failure]}]
  (let [requests (map fetch-document items)]
    (-> (js/Promise.all (clj->js requests))
        (.then #(read-responses
                 items
                 %
                 :on-success on-success
                 :on-failure on-failure
                 :response-filter (fn [%]
                                    (assoc %
                                           :id
                                           (get-in % [:result :id]))))))))

(rf/reg-fx
 :save-n
 (fn [{:keys [items on-success on-failure]}]
   (save-n items
           :on-success on-success
           :on-failure on-failure)))

(rf/reg-fx
 :save
 (fn [{:keys [data service on-success on-failure]}]
   (save-n [{:data    data
             :service service}]
           :on-success on-success
           :on-failure on-failure)))

(defn call-n
  [items & {:keys [on-success on-failure]}]
  (let [requests (map call (filterv identity items))]
    (-> (js/Promise.all (clj->js requests))
        (.then #(read-responses
                 items
                 %
                 :on-success on-success
                 :on-failure on-failure
                 :response-filter json/parse-custom-fields)))))

(rf/reg-fx
 :call-n
 (fn [{:keys [items on-success on-failure]}]
   (call-n items
           :on-success on-success
           :on-failure on-failure)))

(rf/reg-fx
 :call
 (fn [{:keys [on-success on-failure] :as data}]
   (call-n [(dissoc data :on-success :on-failure)]
           :on-success on-success
           :on-failure on-failure)))

(defonce timeouts (r/atom {}))

(rf/reg-fx
 :timeout
 (fn [{:keys [id event time]}]
   (when-some [existing (get @timeouts id)]
     (js/clearTimeout existing)
     (swap! timeouts dissoc id))
   (when (some? event)
     (swap! timeouts assoc id
            (js/setTimeout
             (fn []
               (comp
                (swap! timeouts dissoc id)
                (rf/dispatch event)))
             time)))))

(rf/reg-fx
 :cancel-timeout
 (fn [{:keys [id]}]
   (when-some [existing (get @timeouts id)]
     (js/clearTimeout existing)
     (swap! timeouts dissoc id))))

(defn after-timeout [id event]
  (if
   (get @timeouts id)
    (js/setTimeout
     (fn [] (after-timeout id event))
     100)
    (rf/dispatch event)))

(rf/reg-fx
 :after-timeout
 (fn [{:keys [id event]}]
   (after-timeout id event)))
