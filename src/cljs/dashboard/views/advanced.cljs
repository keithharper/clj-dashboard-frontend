(ns dashboard.views.advanced
  (:require [reagent.core :as reagent]
			[re-frame.core :refer [subscribe dispatch dispatch-sync]]
			[clojure.string :as str]
			[dashboard.views.images :as images]))

(defn command-nodes-to-section [{:keys [section command active] :as section->command}]
  (let [selected? @(subscribe [:get-selected-command section->command])]
	^{:key command}
	[:div.sidebar__item.mdl-button
	 {:class    (when selected? "active")
	  :on-click #(if-not selected?
				   (dispatch [:fetch-advanced-command-result section->command])
				   (dispatch [:remove-selected-command section->command]))}
	 [:div.command-node command]
	 [:button.sidebar-button
	  [:span.plus-horizontal]
	  [:span.plus-vertical]]]))

(defn sidebar-section [{:keys [section-id section]}]
  ^{:key section-id}
  [:div
   [:button.accordion.mdl-button
	{:class    (when (:active section) "active")
	 :on-click #(dispatch [:set-section-active section])}

	[:div.sidebar__section__arrow]
	[:div.sidebar__section__label section-id]]
   [:div.sidebar__section {:class (when (:active section) "active")}
	(doall (map #(command-nodes-to-section {:section section-id :command (first %) :active (:active (second %))})
			 (:commands section)))]])

(defn sidebar-menu-nodes []
  (let [available-admin-commands @(subscribe [:available-admin-commands])]
	(doall (map #(sidebar-section {:section-id (first %) :section (second %)}) available-admin-commands))))

(defn command-card [{:keys [section command execution-ts result] :as section->command}]
  ^{:key command}
  [:div.card.command
   [:button.mdl-button.card__delete-button {:on-click #(dispatch [:remove-selected-command section->command])}
	"x"]
   [:div.card__command-name command]
   [:div.card__date execution-ts]
   [:div.card__container
	[:div.card__description
	 [:div.output result]]]])

(defn command-nodes []
  (let [selected-commands @(subscribe [:selected-commands])]
	(map (fn [[section-id section]]
		   (map (fn [[command-id command]] (command-card (conj {:section section-id} command)))
			 (:commands section)))
	  selected-commands)))

(defn card-container []
  (let [menu-status @(subscribe [:menu-status])]
	[:div.main {:on-click #(dispatch [:set-menu-inactive])}
	 [:div {:class (when menu-status "sidebar-active")}]
	 (seq (command-nodes))]))

(defn sidebar-menu []
  (let [menu-status @(subscribe [:menu-status])]
	[:div.sidebar {:class (when menu-status "active")}
	 (seq (sidebar-menu-nodes))]))

(defn alert-container []
  (let [alert-message @(subscribe [:alert-message])]
	(when (not (nil? alert-message))
	  [:div {:id "alert"}
	   [:a.alert.active alert-message]])))

(defn system-shutdown-button []
  [:button.header__button.mdl-button
   {:id         "shutdown-button"
	:aria-label "Shutdown"
	:style      {:padding "3px" :background-color "transparent"}
	:title      "Initiate system shutdown"
	:on-click   #(when (js/confirm "This will initiate a system shutdown.")
				   (dispatch [:send-system-shutdown-request]))}
   "Shutdown"
   (:shutdown-image images/svgs)])

(defn configure-wan-button []
  [:button.header__button.mdl-button
   {:id         "configure-wan-button"
	:aria-label "Configure WAN"
	:style      {:padding "3px" :background-color "transparent"}
	:title      "Configure WAN connection"
	:on-click   #(when (js/confirm "Make sure to connect the Gigabit port to the network before you continue.")
				   (dispatch [:send-configure-wan-request]))}
   "Configure WAN"
   (:configure-wan-image images/svgs)])

(defn initiate-upload-button []
  [:button.header__button.mdl-button
   {:id         "initiate-upload-button"
	:aria-label "Upload"
	:style      {:padding "3px" :background-color "transparent"}
	:title      "Initiate upload of media files"
	:on-click   #(when (js/confirm "This will toggle the upload status. Make sure the active connection is not cellular before proceeding.") ;; TODO: Add logic to first check what the active connection is?
				   (dispatch [:send-initiate-upload-request]))}
   "Upload"
   (:initiate-upload-image images/svgs)])

(defn refresh-all-button []
  [:button.header__button.mdl-button
   {:id         "refresh-button"
	:aria-label "Refresh"
	:style      {:padding "3px" :background-color "transparent"}
	:title      "Refresh output of all selected commands"
	:on-click   #(dispatch [:fetch-all-selected-command-results])}
   "Refresh All"
   (:refresh-all-image images/svgs)])

(defn deselect-all-button []
  [:button.header__button.mdl-button
   {:id         "deselect-all-button"
	:aria-label "Deselect All"
	:style      {:padding "3px" :background-color "transparent"}
	:title      "Deselect all currently selected commands"
	:on-click   #(dispatch [:deselect-all-commands])}
   "Deselect All"
   (:deselect-all-image images/svgs)])

(defn dashboard-page-button []
  [:div.header__button.mdl-button
   {:id         "dashboard-button"
	:title      "Switch to Advanced mode"
	:aria-label "Dashboard"
	:style      {:padding "3px" :background-color "transparent"}
	:on-click   #(dispatch [:set-active-page :home])}
   "Dashboard"
   (:dashboard-image images/svgs)])

(defn rocketiot-logo []
  [:div.header__title {:id "logo" :style {:display "flex" :-webkit-font-smoothing "antialiased"}}
   [:div {:style {:color "#f2a900" :cursor "default" :font-weight "bold"}} "ROCKET"]
   [:div {:style {:color "#fff" :cursor "default"}} "IoT"]])

(defn toggle-menu-button []
  (let [menu-status @(subscribe [:menu-status])]
	[:button.mdl-button.c-hamburger.c-hamburger--htla
	 {:id       "menu-button"
	  :class    (when menu-status "active")
	  :on-click #(dispatch [:toggle-menu])}
	 [:span "Toggle menu"]]))

(defn ui []
  [:div
   [:header.header
	[toggle-menu-button]
	[rocketiot-logo]
	[:div.header-buttons
	 [system-shutdown-button]
	 [configure-wan-button]
	 [initiate-upload-button]
	 [refresh-all-button]
	 [deselect-all-button]
	 [dashboard-page-button]]]
   [alert-container]
   [sidebar-menu]
   [card-container]])

(defn ^:export check-uploaded-files []
  (dispatch [:fetch-command-result {:section "System Status" :command "Media Files Uploaded"}]))

(defn ^:export print-selected-commands []
  (println @(subscribe [:selected-commands])))