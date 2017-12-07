(ns dashboard.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

;;; Layer 2 Subscriptions
;;; Re-run every time app-db changes
(reg-sub
  :active-page
  (fn [db _]
	(:active-page db)))

(reg-sub
  :available-commands         ;; usage: (subscribe [:available-commands])
  (fn [db _]
	(:available-commands db)))

(reg-sub
  :available-admin-commands
  (fn [db _]
	(:available-admin-commands db)))

(reg-sub
  :selected-commands          ;; usage: (subscribe [:execution-results])
  (fn [db _]
	(:selected-commands db)))

(reg-sub
  :alert-message
  (fn [db _]
	(:alert-message db)))


(reg-sub
  :menu-status
  (fn [db _]
	(if (:menu-status db)
	  "active"
	  "")))

(reg-sub
  :any-request-pending
  (fn [db [_]]
	(->> (get db :pending-requests)
	  (tree-seq map? vals)
	  (some #(= % :pending)))))

;;; Layer 3 Subscriptions
(reg-sub
  :get-execution-result       ;; usage: (subscribe [:get-execution-result] {:section :command})
  ;:<- [:execution-results]
  (fn [_]
	(subscribe [:available-commands]))
  (fn get-command-result [commands-db [_ {:keys [section command]}]]
	(get-in commands-db [section :commands command :result])))

(reg-sub
  :get-command-loading
  :<- [:loading-queue]
  (fn get-command-loading [db [_ {:keys [section command]}]]
	(not (nil? (get-in db [section command :loading])))))