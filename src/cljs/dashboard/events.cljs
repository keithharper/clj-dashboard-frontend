(ns dashboard.events
  (:require [dashboard.db :refer [default-db]]
			[re-frame.core :refer [reg-event-db reg-event-fx dispatch trim-v]]
			[day8.re-frame.http-fx]
			[ajax.core :as ajax]
			[dashboard.helpers :as helpers]))
(enable-console-print!)

(reg-event-db
  :init-db
  (fn [_ _]
	default-db))

(reg-event-db
  :set-active-page
  trim-v
  (fn [db [active-page]]
	(assoc-in db [:active-page] active-page)))

(reg-event-db
  :check-and-set-alert-message
  trim-v
  (fn [db [[{:keys [section command result]}]]]
	(if (= true (get-in db [:available-commands section :commands command :important]))
	  (if (= "FAIL" (helpers/replace-return-and-apply-upper result))
		(assoc-in db [:alert-message] "WARNING: This unit still has unuploaded files on it. Do not run it through EOL test or the files will be lost!")
		(dissoc db :alert-message))
	  db)))

(reg-event-fx
  :set-command-result
  trim-v
  (fn [{:keys [db]} [{:keys [section command result]} :as section->command]]
	{:dispatch [:check-and-set-alert-message section->command]
	 :db       (assoc-in db
				 [:available-commands section :commands command :result]
				 (helpers/replace-return-and-apply-upper result))}))

(reg-event-fx
  :add-available-commands
  trim-v
  (fn [{:keys [db]} [commands]]
	{:dispatch [:fetch-all-command-results]
	 :db       (assoc-in db [:available-commands] (helpers/merge-by commands [:section] [:commands :command]))}))

(reg-event-db
  :add-available-admin-commands
  trim-v
  (fn [db [commands]]
	(assoc-in db [:available-admin-commands] (helpers/merge-by commands [:section] [:commands :command]))))

(reg-event-fx
  :fetch-available-commands
  (fn [{:keys [db]} _]
	(when (empty? (get-in db [:available-commands]))
	  {:http-xhrio {:uri             (str js/location.pathname "/commands")
					:method          :get
					:timeout         8000
					:response-format (ajax/json-response-format {:keywords? true})
					:on-success      [:add-available-commands]}})))

(reg-event-fx
  :fetch-available-admin-commands
  (fn [{:keys [db]} _]
	(when (empty? (get-in db [:available-admin-commands]))
	  {:http-xhrio {:uri             "/advanced/commands"
					:method          :get
					:timeout         8000
					:response-format (ajax/json-response-format {:keywords? true})
					:on-success      [:add-available-admin-commands]}})))

(reg-event-fx
  :fetch-command-result
  trim-v
  (fn [{:keys [db]} [{:keys [section command] :as section->command}]]
	{:dispatch   [:set-command-result {:section section :command command :result "loading"}]
	 :http-xhrio {:uri             (str js/location.pathname "/run/" section "/" command)
				  :method          :get
				  :timeout         15000
				  :response-format (ajax/json-response-format {:keywords? true})
				  :on-success      [:set-command-result]
				  :on-failure      [:set-command-result {:section section :command command :result "fail"}]}}))

(reg-event-fx
  :fetch-all-command-results
  (fn [{:keys [db]} _]
	{:dispatch-n (helpers/group-section-and-command :fetch-command-result (:available-commands db))}))

(reg-event-fx
  :fetch-all-selected-command-results
  (fn [{:keys [db]} _]
	{:dispatch-n (helpers/group-section-and-command :fetch-advanced-command-result (:selected-commands db))}))

;;; Admin Events ;;;
(reg-event-db
  :toggle-menu
  trim-v
  (fn [db _]
	(assoc-in db [:menu-status] (not (:menu-status db)))))

(reg-event-db
  :set-command-active
  trim-v
  (fn [db [{:keys [section command]}]]
	(assoc-in db [:available-admin-commands section :commands command :active] true)))

(reg-event-db
  :set-command-inactive
  trim-v
  (fn [db [{:keys [section command]}]]
	(update-in db [:available-admin-commands section :commands command] dissoc :active)))

(reg-event-db
  :add-selected-command
  trim-v
  (fn [db [{:keys [section command execution-ts result] :as section->command}]]
	(assoc-in db [:selected-commands section :commands command] (dissoc section->command :section))))

(reg-event-db
  :remove-selected-command
  trim-v
  (fn [db [{:keys [section command] :as section->command}]]
	(update-in db [:selected-commands section :commands] dissoc command)))

(reg-event-db
  :set-menu-inactive
  trim-v
  (fn [db _]
	(assoc-in db [:menu-status] false)))

(defn send-request [uri]
  {:http-xhrio {:uri             (str js/location.pathname uri)
				:method          :get
				:timeout         15000
				:response-format (ajax/json-response-format {:keywords? true})
				:on-success      [:set-command-result]}})

(reg-event-fx
  :send-system-shutdown-request
  trim-v
  (fn [{:keys [db]} _]
	(send-request "/run/Admin/Shutdown")))

(reg-event-fx
  :send-configure-wan-request
  trim-v
  (fn [{:keys [db]} _]
	(send-request "/run/Admin/Gigabit")))

(reg-event-fx
  :send-initiate-upload-request
  trim-v
  (fn [{:keys [db]} _]
	(send-request "/run/Admin/Upload")))

(reg-event-db
  :deselect-all-commands
  trim-v
  (fn [db _]
	(dissoc db :selected-commands)))

(reg-event-db
  :set-section-active
  trim-v
  (fn [db [{:keys [section]}]]
	(let [active? (get-in db [:available-admin-commands section :active])]
	  (assoc-in db [:available-admin-commands section :active]
		(if active?
		  false
		  true)))))

(reg-event-fx
  :fetch-advanced-command-result
  trim-v
  (fn [{:keys [db]} [{:keys [section command] :as section->command}]]
	{:http-xhrio {:uri             (str "/advanced/run/" section "/" command)
				  :method          :get
				  :timeout         15000
				  :response-format (ajax/json-response-format {:keywords? true})
				  :on-success      [:add-selected-command]}}))