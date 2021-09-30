(ns edd.core
  (:require
   [re-frame.core :as rf]
   [edd.events :as events]
   [edd.views :as views]
   ["@material-ui/core/styles" :refer [withStyles, createMuiTheme, MuiThemeProvider]]
   [clojure.walk :refer [keywordize-keys]]
   [reagent.core :as r]
   [reagent.dom :as dom]
   [edd.i18n :as i18n]
   [bidi.bidi :as bidi]
   [edd.client :as client]
   [pushy.core :as pushy]))

(defn with-custom-styles
  [{:keys [styles]} component]
  ((withStyles
    (fn [theme]
      (clj->js
       (styles theme)))) component))

(defn body
  [{:keys [theme panels] :as ctx}]
  "Initialize body with custom style"
  [:> MuiThemeProvider {:theme (createMuiTheme (clj->js theme))}
   [:> (with-custom-styles
         ctx
         (r/reactify-component
          (fn [props]
            (views/page
             (assoc
              ctx
              :classes (keywordize-keys
                        (:classes (js->clj props))))))))]])

(defn- dispatch-route
  [url])

(defn mount-root
  [{:keys [routes] :as ctx}]
  (pushy/start!
   (pushy/pushy #(rf/dispatch [::events/navigate %])
                (fn [url] url)))

  (dom/render
   (body ctx)
   (.getElementById js/document "app")))

(defn init
  [{:keys [translations] :as ctx}]
  (let [ctx (-> ctx
                (assoc :config (js->clj
                                (.-eddconfig js/window)
                                :keywordize-keys true))
                (merge (:config ctx {})))]
    (rf/clear-subscription-cache!)
    (rf/dispatch [::events/initialize-db ctx])
    (rf/dispatch [::events/add-translation i18n/base-translations])
    (when translations
      (rf/dispatch [::events/add-translation translations]))
    (mount-root ctx)))
