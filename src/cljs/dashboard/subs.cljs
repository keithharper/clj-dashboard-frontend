(ns dashboard.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [dashboard.helpers :as helpers]))

(defn filter-commands-by
  [section search-term]
  (assoc-in section [:commands]
            (into {} (filter (fn [[command-id _]]
                               (clojure.string/includes?
                                 (clojure.string/lower-case command-id)
                                 (clojure.string/lower-case search-term)))
                             (:commands section)))))

;;; Layer 2 Subscriptions
;;; Re-run every time app-db changes
(reg-sub
  :active-page
  (fn [db _]
    (:active-page db)))

(reg-sub
  :next-page
  (fn [db _]
    (:next-page db)))

(reg-sub
  :alert-message
  (fn [db _]
    (:alert-message db)))

(reg-sub
  :user
  (fn [db _]
    (:user db)))

(reg-sub
  :user-authenticated?
  (fn [db _]
    (get-in db [:user :authenticated?])))

(reg-sub
  :authentication-error
  (fn [db _]
    (get-in db [:user :error])))

(reg-sub
  :authentication-attempts
  (fn [db _]
    (get-in db [:user :authentication-attempts])))

(reg-sub
  :advanced
  (fn [db _]
    (:advanced db)))

(reg-sub
  :available-dashboard-commands
  (fn [db _]
    (get-in db [:dashboard :available-commands])))

(reg-sub
  :available-advanced-commands
  (fn [db _]
    (get-in db [:advanced :available-commands])))

(reg-sub
  :selected-commands
  (fn [db _]
    (get-in db [:advanced :selected-commands])))

(reg-sub
  :admin-command-results
  (fn [db _]
    (get-in db [:advanced :admin-command-results])))

(reg-sub
  :menu-status
  (fn [db _]
    (get-in db [:advanced :menu-status])))

(reg-sub
  :any-request-pending
  (fn [db [_]]
    (->> (get db :pending-requests)
         (tree-seq map? vals)
         (some #(= % :pending)))))

;;; Layer 3 Subscriptions
(reg-sub
  :get-execution-result
  :<- [:available-commands]
  (fn get-command-result [commands-db [_ {:keys [section command]}]]
    (get-in commands-db [section :commands command :result])))

(reg-sub
  :get-selected-command
  :<- [:selected-commands]
  (fn [db [_ {:keys [section command]}]]
    (not (nil? (get-in db [section :commands command])))))

(reg-sub
  :get-command-loading
  :<- [:loading-queue]
  (fn get-command-loading [db [_ {:keys [section command]}]]
    (not (nil? (get-in db [section command :loading])))))

(reg-sub
  :advanced-commands-to-display
  :<- [:available-advanced-commands]
  :<- [:advanced]
  (fn [[available-advanced-commands {:keys [search-term]}] [_ _]]
    (if-not (empty? search-term)
      (->> available-advanced-commands
           (map (fn [[section-id section]]
                  (let [filtered-commands (filter-commands-by section search-term)]
                    (when (not-empty (:commands filtered-commands))
                      {section-id filtered-commands}))))
           (into {}))
      available-advanced-commands)))
