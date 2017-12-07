(ns dashboard.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
			[re-frame.core :as rf :refer [subscribe dispatch dispatch-sync reg-event-db reg-event-fx]]
			[dashboard.subs]
			[dashboard.events]
			[dashboard.router]
			[dashboard.views.core :as views]
			[cljs.core.async :refer [<! chan]]
			[cljs-http.client :as http]
			[re-frame.db :as db]))

;; -- Debugging aids ----------------------------------------------------------
;(devtools/install!)
(enable-console-print!)

;(defn ^:export init
;  []
;  (dispatch-sync [:initialize]))
;
;(defn ^:export send-available-commands-request
;  []
;  (dispatch [:request-available-commands]))

(defn ^:export main
  []
  (dispatch-sync [:init-db])
  (reagent/render [views/main-panel]
	(.getElementById js/document "main")))

