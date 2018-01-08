(ns dashboard.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame :refer [dispatch-sync]]
            [dashboard.subs]
            [dashboard.events]
            [dashboard.routes :as routes]
            [dashboard.views.core :as views]
    ;[devtools.core :as devtools]
            ))

;; -- Debugging aids ----------------------------------------------------------
;(devtools/install!)

(defn ^:export main []
  (re-frame/dispatch-sync [:init-db])
  (routes/initialize-routes)
  (reagent/render [views/main-panel] (.getElementById js/document "main")))

