(ns dashboard.events
  (:require [dashboard.db :refer [default-db selected-commands->local-store]]
			[re-frame.core :refer [reg-event-db path reg-event-fx after trim-v inject-cofx
								   debug purge-event-queue dispatch dispatch-sync]]
			[day8.re-frame.http-fx]
			[day8.re-frame.undo :as undo :refer [undoable clear-undos! undo-config!]]
			[ajax.core :as ajax]
			[dashboard.helpers :as helpers]
			[dashboard.handlers]
			[goog.crypt.base64 :as base64]))
(enable-console-print!)

(defn set-max-undos []
  (undo-config! {:max-undos 10}))
(set-max-undos)

(def ->local-store (after selected-commands->local-store))
(def selected-commands-interceptors [->local-store
									 trim-v])

(reg-event-fx
  :init-db
  [(inject-cofx :local-store-selected-commands)
   (inject-cofx :active-authentication)
   ]
  (fn [{:keys [db local-store-selected-commands authenticated?]}]
	{:dispatch [:set-user-auth-status authenticated?]
	 :db       (assoc-in default-db [:advanced :selected-commands] local-store-selected-commands)}))

(defn send-get-request [[uri on-success]]
  {:http-xhrio {:uri             (str js/location.pathname uri)
				:method          :get
				:timeout         15000
				:response-format (ajax/json-response-format {:keywords? true})
				:on-success      [on-success]
				:on-failure      [:on-post-failure]}})

(defn send-post-request [[uri params on-success]]
  {:http-xhrio {:uri             (str js/location.pathname uri)
				:method          :post
				:headers         {"X-CSRF-Token" js/csrfToken}
				:params          params
				:timeout         15000
				:format          (ajax/json-request-format)
				:response-format (ajax/json-response-format {:keywords? true})
				:on-success      [on-success]
				:on-failure      [:on-post-failure]}})

(reg-event-fx
  :authentication-success
  trim-v
  (fn [{:keys [db]} _]
	{:dispatch [:set-active-page :advanced]
	 :db       (assoc-in db [:user :authenticated?] true)}))

(reg-event-fx
  :authentication-failure
  trim-v
  (fn [{:keys [db]} _]
	{:dispatch [:set-authentication-error "invalid username/password."]
	 :db       (assoc-in db [:user :authenticated?] false)}))

(reg-event-fx
  :on-post-failure
  trim-v
  (fn [{:keys [db]} [response]]
	(if (= (:status response) 401)
	  {:dispatch [:set-active-page :login]
	   :db (assoc-in db [:user :authenticated?] false)})))

(reg-event-db
  :set-authentication-error
  trim-v
  (fn [db [error]]
	(assoc-in db [:user] {:error error})))

(reg-event-db
  :set-user-auth-status
  trim-v
  (fn [db [status]]
	(assoc-in db [:user :authenticated?] status)))

(reg-event-db
  :update-user-authentication-attempts
  trim-v
  (fn [db _]
	(update-in db [:user :authentication-attempts] inc)))

(reg-event-db
  :set-active-page
  trim-v
  (fn [db [active-page]]
	(case active-page
	  :home (assoc-in db [:active-page] active-page)
	  (if-not (get-in db [:user :authenticated?])
		(assoc-in db [:active-page] :login)
		(assoc-in db [:active-page] active-page)))))

(reg-event-fx
  :authenticate
  trim-v
  (fn [{:keys [db]} [{:keys [username password]}]]
	{:dispatch   [:set-authentication-error nil]
	 :http-xhrio {:uri             (str js/location.pathname "/auth/login")
				  :method          :post
				  :headers         {"X-CSRF-Token" js/csrfToken}
				  :params          {:credentials (base64/encodeString (str username ":" password))}
				  :timeout         15000
				  :format          (ajax/json-request-format)
				  :response-format (ajax/json-response-format {:keywords? true})
				  :on-success      [:authentication-success]
				  :on-failure      [:authentication-failure]}}))

(reg-event-fx
  :logout
  trim-v
  (fn [{:keys [db]} _]
	{:dispatch   [:set-user-auth-status false]
	 :http-xhrio {:uri             (str js/location.pathname "/auth/logout")
				  :method          :post
				  :headers         {"X-CSRF-Token" js/csrfToken}
				  :timeout         15000
				  :format          (ajax/text-request-format)
				  :response-format (ajax/raw-response-format)
				  :on-success      [:set-user-auth-status false]}}))

(reg-event-db
  :check-and-set-alert-message
  trim-v
  (fn [db [[{:keys [section command result]}]]]
	(if (= true (get-in db [:dashboard :available-commands section :commands command :important]))
	  (if-not (= "PASS" (helpers/replace-newline-and-apply-upper result))
		(assoc-in db [:alert-message] "WARNING: This unit still has unuploaded files on it. Do not run it through EOL test or the files will be lost!")
		(dissoc db :alert-message))
	  db)))

(reg-event-fx
  :set-command-result
  trim-v
  (fn [{:keys [db]} [{:keys [section command result]} :as section->command]]
	{:dispatch [:check-and-set-alert-message section->command]
	 :db       (assoc-in db [:dashboard :available-commands section :commands command :result]
				 (helpers/replace-newline-and-apply-upper result))}))

(reg-event-fx
  :add-available-dashboard-commands
  trim-v
  (fn [{:keys [db]} [commands]]
	{:dispatch [:fetch-all-dashboard-command-results]
	 :db       (assoc-in db [:dashboard :available-commands] (helpers/merge-by commands [:section] [:commands :command]))}))

(reg-event-db
  :add-available-advanced-commands
  trim-v
  (fn [db [commands]]
	(assoc-in db [:advanced :available-commands] (helpers/merge-by commands [:section] [:commands :command]))))

(reg-event-fx
  :fetch-dashboard-commands
  (fn [{:keys [db]} _]
	(when (empty? (get-in db [:dashboard :available-commands]))
	  (send-get-request ["/home/commands" :add-available-dashboard-commands]))))

(reg-event-fx
  :fetch-advanced-commands
  (fn [{:keys [db]} _]
	(when (empty? (get-in db [:advanced :available-commands]))
	  (send-get-request ["/advanced/commands" :add-available-advanced-commands]))))

(reg-event-fx
  :fetch-dashboard-command-result
  trim-v
  (fn [{:keys [db]} [{:keys [section command] :as section->command}]]
	{:dispatch   [:set-command-result (assoc section->command :result "loading")]
	 :http-xhrio {:method          :post
				  :uri             (str js/location.pathname "/home/run")
				  :headers         {"X-CSRF-Token" js/csrfToken}
				  :params          section->command
				  :timeout         15000
				  :format          (ajax/json-request-format)
				  :response-format (ajax/json-response-format {:keywords? true})
				  :on-success      [:set-command-result]
				  :on-failure      [:set-command-result (assoc section->command :result "fail")]}}))

(reg-event-fx
  :fetch-all-dashboard-command-results
  (fn [{:keys [db]} _]
	{:dispatch-n (helpers/group-dispatcher-and-command
				   :fetch-dashboard-command-result
				   (get-in db [:dashboard :available-commands]))}))

(reg-event-fx
  :fetch-selected-command-result
  trim-v
  (fn [{:keys [db]} [{:keys [section command] :as section->command}]]
	{:dispatch   [:set-selected-command-loading section->command]
	 :http-xhrio {:uri             (str js/location.pathname "/advanced/run")
				  :method          :post
				  :headers         {"X-CSRF-Token" js/csrfToken}
				  :params          section->command
				  :timeout         15000
				  :format          (ajax/json-request-format)
				  :response-format (ajax/json-response-format {:keywords? true})
				  :on-success      [:add-selected-command]
				  :on-failure      [:on-post-failure]}}))

(reg-event-fx
  :fetch-all-selected-command-results
  (fn [{:keys [db]} _]
	{:dispatch-n (helpers/group-dispatcher-and-command
				   :fetch-selected-command-result
				   (get-in db [:advanced :selected-commands]))}))

;;; Advanced Events ;;;
(reg-event-db
  :toggle-menu
  trim-v
  (fn [db _]
	(assoc-in db [:advanced :menu-status] (not (get-in db [:advanced :menu-status])))))

(reg-event-db
  :set-menu-inactive
  trim-v
  (fn [db _]
	(assoc-in db [:advanced :menu-status] false)))

(reg-event-db
  :toggle-menu-section
  trim-v
  (fn [db [{:keys [section]}]]
	(let [active? (get-in db [:advanced :available-commands section :active])]
	  (assoc-in db [:advanced :available-commands section :active]
		(not active?)))))

(reg-event-db
  :set-advanced-command-active
  trim-v
  (fn [db [{:keys [section command]}]]
	(assoc-in db [:advanced :available-commands section :commands command :active] true)))

(reg-event-db
  :set-advanced-command-inactive
  trim-v
  (fn [db [{:keys [section command]}]]
	(update-in db [:advanced :available-commands section :commands command] dissoc :active)))

(reg-event-db
  :set-selected-command-loading
  trim-v
  (fn [db [{:keys [section command]} :as section->command]]
	(assoc-in db [:advanced :selected-commands section :commands command] {:command command :status "pending"})))

(reg-event-db
  :add-selected-command
  [(path :advanced)
   selected-commands-interceptors]
  (fn [db [{:keys [section command execution-ts result] :as section->command}]]
	(assoc-in db [:selected-commands section :commands command]
	  (-> section->command
		(dissoc :section)
		(assoc :status "success")))))

(reg-event-db
  :remove-selected-command
  [(path :advanced)
   selected-commands-interceptors
   (undoable (fn [_ [{command :command}]] (str "Removed " command ".")))]
  (fn [db [{:keys [section command] :as section->command}]]
	(update-in db [:selected-commands section :commands] dissoc command)))

(reg-event-fx
  :send-system-shutdown-request
  trim-v
  (fn [{:keys [db]} _]
	(send-post-request ["/advanced/run" {:section "Admin" :command "Shutdown"} :set-command-result])))

(reg-event-fx
  :send-configure-wan-request
  trim-v
  (fn [{:keys [db]} _]
	(send-post-request ["/advanced/run" {:section "Admin" :command "Gigabit"} :set-command-result])))

(reg-event-fx
  :send-initiate-upload-request
  trim-v
  (fn [{:keys [db]} _]
	(send-post-request ["/advanced/run" {:section "Admin" :command "Upload"} :set-command-result])))

(reg-event-db
  :deselect-all-commands
  [selected-commands-interceptors
   (undoable "Removed all selected commands.")]
  (fn [db _]
	(update-in db [:advanced] dissoc :selected-commands)))

(reg-event-db
  :filter-advanced-commands
  trim-v
  (fn [db [search-string]]
	(assoc-in db [:advanced :search-term] search-string)))

(reg-event-fx
  :purge-undos
  trim-v
  (fn [{:keys [db]} _]
	(clear-undos!)))

(reg-event-fx
  :purge-undos-later
  trim-v
  (fn [cofx _]
	{:dispatch-debounce {:key   ::undo
						 :event [:purge-undos]
						 :delay 5000}}))