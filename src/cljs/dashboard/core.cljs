(ns dashboard.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
			[re-frame.core :as rf :refer [subscribe dispatch dispatch-sync reg-event-db reg-event-fx]]
			[dashboard.subs]
			[dashboard.events]
			[dashboard.routes]
			[dashboard.views.core :as views]
			[devtools.core :as devtools]))

;; -- Debugging aids ----------------------------------------------------------
(devtools/install!)

(defn ^:export main
  []
  (dispatch-sync [:init-db])
  (dashboard.routes/routes)
  (reagent/render [views/main-panel]
	(.getElementById js/document "main")))

