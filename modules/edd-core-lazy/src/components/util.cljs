(ns components.util
  (:require-macros [components.util])
  (:require [reagent.core :as r]
            [shadow.lazy :as lazy]
            [malli.error :as me]
            [malli.core :as m]
            ["react" :as react]))

(defn create-lazy-component
  [loadable spec]
  (fn [& [props]]
    (.info js/console "Loading component" loadable spec)
    (if-not (m/validate spec props)
      (let [error (.stringify js/JSON (-> spec
                                          (m/explain props)
                                          (me/humanize)
                                          (clj->js)))]
        (.info js/console "Error loading component component: " error)
        (throw (str "Error rendering component: " error)))
      [:> react/Suspense {:fallback (r/as-element [:div "Loading ..."])}
       [:> (react/lazy
             (fn [_]
               (-> (lazy/load loadable)
                   (.then (fn [_]
                            #js {:default
                                 (r/reactify-component
                                   (fn [_]
                                     [@loadable props]))})))))]])))
