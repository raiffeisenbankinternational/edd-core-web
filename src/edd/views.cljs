(ns edd.views

  (:require [re-frame.core :as rf]
            [edd.subs :as subs]
            [reagent.core :as r]
            [edd.events :as events]
            [edd.util :as util]
            [edd.routing :refer [path-for]]
            [reagent.core :as reagent]


            [edd.i18n :refer [tr]]

            ["@material-ui/core/AppBar" :default AppBar]
            ["@material-ui/core/Toolbar" :default Toolbar]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/IconButton" :default IconButton]
            ["@material-ui/core/Drawer" :default Drawer]
            ["@material-ui/core/List" :default List]
            ["@material-ui/core/ListItem" :default ListItem]
            ["@material-ui/core/ListItemText" :default ListItemText]
            ["@material-ui/core/ListItemIcon" :default ListItemIcon]
            ["@material-ui/core/ListSubheader" :default ListSubheader]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Collapse" :default Collapse]
            ["@material-ui/icons/ChevronRight" :default ChevronRight]
            ["@material-ui/icons/StarBorder" :default StarBorder]
            ["@material-ui/icons/KeyboardArrowRight" :default KeyboardArrowRightIcon]
            ["@material-ui/core/FormControl" :default FormControl]
            ["@material-ui/core/FormHelperText" :default FormHelperText]
            ["@material-ui/core/Select" :default Select]
            ["@material-ui/core/InputLabel" :default InputLabel]
            ["@material-ui/core/MenuItem" :default MenuItem]
            ["@material-ui/icons/Menu" :default MenuIcon]))

(defn menu-item
  [{:keys [classes]} item]
  (let [lang @(rf/subscribe [::subs/selected-language])]
    [:> Grid {:item true
              :xs   12}
     [:> Button {:on-click   #(rf/dispatch [::events/change-panel (:key item)])
                 :key        (:key item)
                 :class-name (:nested classes)
                 :start-icon (reagent/as-element [:> KeyboardArrowRightIcon])}
      (get-in item [:name lang])]]))


(defn language-item
  [ctx]
  [:> Grid {:item true
            :xs   12}
   [:> FormControl
    [:> Select {:value     @(rf/subscribe [::subs/selected-language])
                :on-change #(rf/dispatch [::events/change-language (-> (.-target %) (.-value) (keyword))])}
     [:> MenuItem {:value :en} "English"]
     [:> MenuItem {:value :de} "Deutsch"]]
    [:> FormHelperText (tr :language)]]])

(defn drawer
  [{:keys [classes menu drawer] :as ctx}]
  [:> Drawer {:open     @(rf/subscribe [::subs/drawer])
              :on-close #(rf/dispatch [::events/toggle-drawer])}

   [:div {:class-name (:drawer-list classes)
          :role       "presentation"}

    (if drawer
      (drawer ctx)
      [:> Grid {:container true}
       [:> Grid {:item true
                 :xs   12}]
       [:> Grid {:item true
                 :xs   1}]
       [:> Grid {:item true
                 :xs   10}
        (into
          [:> Grid {:container true
                    :item      true
                    :spacing   1}

           (language-item ctx)]
          (map
            (fn [item]
              (menu-item ctx item))
            menu))
        ]])
    ]])

(defn page
  [{:keys [classes panels app-bar] :as ctx}]

  (if @(rf/subscribe [::subs/ready])
    [:div {:class-name (:root classes)}
     @(rf/subscribe [::subs/ready])
     (drawer ctx)
     [:> Grid {:container  true
               :class-name (:root classes)}
      [:> Grid {:item true
                :xs   12 :class-name (:header classes)}
       [:> AppBar {:class-name (:app-bar classes)
                   :position   "static"}

        [:> Toolbar
         [:> IconButton {:edge     "start"
                         :bel      "Menu"
                         :on-click #(rf/dispatch [::events/toggle-drawer])}
          [:> MenuIcon]]

         (or (app-bar) ":app-bar placeholder")]]]

      [:> Grid {:container true}
       (util/placeholder panels classes)]]]
    [:> Grid {:container true :item true} "Loading"]))


