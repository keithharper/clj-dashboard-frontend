(ns dashboard.router
  (:require [re-frame.core :refer [reg-sub subscribe]]
			[dashboard.views.home]
			[dashboard.views.advanced]))

(defn ui []
  (let [active-page @(subscribe [:active-page])]
	(case
	  :home-page [dashboard.views.home/ui]
	  :advanced-page [dashboard.views.advanced/ui])))