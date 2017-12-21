(ns dashboard.views.advanced
  (:require [reagent.core :as reagent]
			[re-frame.core :refer [subscribe dispatch dispatch-sync purge-event-queue]]
			[clojure.string :as str]
			[dashboard.views.images :as images]
			[dashboard.views.components :as components])
  (:require-macros [reagent.core :refer [with-let]]))

(defn sidebar-commands [{:keys [section command active] :as section->command}]
  (let [selected? @(subscribe [:get-selected-command section->command])]
	^{:key command}
	[:div.sidebar__item.mdl-button.navbar
	 {:class    (when selected? "active")
	  :on-click #(if-not selected?
				   (dispatch [:fetch-selected-command-result section->command])
				   (dispatch [:remove-selected-command section->command]))}
	 [:div.command-node command]
	 [:button.sidebar-button
	  [:span.plus-horizontal]
	  [:span.plus-vertical]]]))

(defn sidebar-section [{:keys [section-id section]}]
  ^{:key section-id}
  [:div.nav-tabs
   [:button.accordion.mdl-button.navbar
	{:class    (when (:active section) "active")
	 :on-click #(dispatch [:toggle-menu-section section])}
	[:div.sidebar__section__label section-id]
	[:div.sidebar__section__arrow]]
   [:div.sidebar__section {:class (when-not (:active section) "collapse")}
	(doall (map #(sidebar-commands {:section section-id :command (first %) :active (:active (second %))})
			 (:commands section)))]])

(defn sidebar-menu-nodes []
  (let [available-admin-commands @(subscribe [:advanced-commands-to-display])]
	(doall (map #(sidebar-section {:section-id (first %) :section (second %)}) available-admin-commands))))

(defn command-card [{:keys [section command execution-ts result status] :as section->command}]
  ^{:key command}
  [:div.card.command
   [:div.card-headers
	[:div.card-buttons
	 [:button.mdl-button {:role     "button"
						  :on-click #(dispatch [:fetch-selected-command-result section->command])}
	  (:refresh-single-image images/svgs)]
	 [:button.mdl-button.card__delete-button {:on-click #(dispatch [:remove-selected-command section->command])}
	  (:deselect-single-image-black images/svgs)]]
	[:div.card__command-name command]
	[:div.card__date execution-ts]]
   [:div.card__container
	[:div.card__description
	 [:div.output (if (= "pending" status)
					[:div.card__spinner (:spinner images/svgs)]
					result)]]]])

(defn command-nodes []
  (let [selected-commands @(subscribe [:selected-commands])]
	(map (fn [[section-id section]]
		   (map (fn [[command-id command]] (command-card (conj {:section section-id} command)))
			 (:commands section)))
	  selected-commands)))

(defn card-container []
  [:div (seq (command-nodes))])

(defn sidebar-overlay []
  (let [menu-status @(subscribe [:menu-status])]
	[:div {:on-click #(dispatch [:set-menu-inactive])}
	 [:div {:class (when menu-status "overlay-active")}]]))

(defn sidebar-menu []
  (let [menu-active? @(subscribe [:menu-status])]
	[:div.sidebar-container.col-md-12.col-lg-4.col-xl-3 {:class (when-not menu-active? "collapse")}
	 [:div.sidebar
	  [:div.navbar-brand.sidebar-header
	   [:div.sidebar-title "Advanced Commands"]]
	  [:div.input-group.filter-input
	   [:input.form-control {:name      "filter" :placeholder "Filter" :type "text"
							 :on-change #(dispatch [:filter-advanced-commands (str (.-target.value %))])}]]
	  (seq (sidebar-menu-nodes))]]))

(defn alert-container []
  (let [alert-message @(subscribe [:alert-message])]
	(when (not (nil? alert-message))
	  [:div.alert.command-alert.active alert-message])))

(defn system-shutdown-button []
  [:button.header__button.mdl-button
   {:id         "shutdown-button"
	:aria-label "Shutdown"
	:title      "Initiate system shutdown"
	:on-click   #(when (js/confirm "This will initiate a system shutdown.")
				   (dispatch [:send-system-shutdown-request]))}
   "Shutdown"
   (:shutdown-image images/svgs)])

(defn configure-wan-button []
  [:button.header__button.mdl-button
   {:id         "configure-wan-button"
	:aria-label "Configure WAN"
	:title      "Configure WAN connection"
	:on-click   #(when (js/confirm "Make sure to connect the Gigabit port to the network before you continue.")
				   (dispatch [:send-configure-wan-request]))}
   "Configure WAN"
   (:configure-wan-image images/svgs)])

(defn initiate-upload-button []
  [:button.header__button.mdl-button
   {:id         "initiate-upload-button"
	:aria-label "Upload"
	:title      "Initiate upload of media files"
	:on-click   #(when (js/confirm "This will toggle the upload status. Make sure the active connection is not cellular before proceeding.") ;; TODO: Add logic to first check what the active connection is?
				   (dispatch [:send-initiate-upload-request]))}
   "Upload"
   (:initiate-upload-image images/svgs)])


(defn deselect-all-button []
  [:button.header__button.mdl-button
   {:id         "deselect-all-button"
	:aria-label "Deselect All"
	:title      "Deselect all currently selected commands"
	:on-click   #(dispatch [:deselect-all-commands])}
   "Deselect All"
   (:deselect-all-image images/svgs)])

(defn toggle-sidebar-button []
  (let [menu-status @(subscribe [:menu-status])]
	[:button.mdl-button.c-hamburger.c-hamburger--htla
	 {:id       "menu-button"
	  :class    (when menu-status "active")
	  :on-click #(dispatch [:toggle-menu])}
	 [:span "Toggle menu"]]))

(defn header-navbar []
  [:div.header.navbar.navbar-inverse.modal-header
   [:div.container-fluid
	[:div.nav
	 [toggle-sidebar-button]
	 [components/rocketiot-logo]]
	[:div.nav
	 [:div.advanced-header-commands
	  [system-shutdown-button]
	  [configure-wan-button]
	  [initiate-upload-button]
	  [deselect-all-button]]
	 [components/refresh-all-button #(dispatch [:fetch-all-selected-command-results])]
	 [components/home-page-button]]]])

(defn undo-container
  [explanation]
  [:div.undo-container-parent.fixed-bottom
   [:div.undo-container
	[:div.undo-desc explanation]
	[:div.undo-button.mdl-button {:on-click #(dispatch [:undo])}
	 "UNDO"]
	[:div.mdl-button {:on-click #(dispatch [:purge-undos])} (:deselect-single-image-white images/svgs)]]])


(defn undo-parent-container
  []
  (when-let [undos? @(subscribe [:undos?])]
	(let [explanations @(subscribe [:undo-explanations])]
	  (dispatch [:purge-undos-later])
	  [undo-container (last explanations)])))

(defn ui []
  [:div.base-container
   [header-navbar]
   [:div.row.main-container
	[sidebar-menu]
	[:div.modal-body.command-container {:class (when @(subscribe [:menu-status])
												 "col-md-12 col-lg-8 col-xl-9 sidebar-active")}
	 [alert-container]
	 [card-container]]]
   [undo-parent-container]])