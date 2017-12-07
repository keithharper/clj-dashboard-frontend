(ns dashboard.views.images)

(def svgs
  {:spinner               [:svg.circular {:view-box "0 0 32 32" :width "32" :height "29"}
						   [:circle#spinner {:cx "16" :cy "16" :r "14" :fill "none"}]]
   :refresh-all-image     [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
						   [:path {:d "M21 10.12h-6.78l2.74-2.82c-2.73-2.7-7.15-2.8-9.88-.1-2.73 2.71-2.73 7.08 0 9.79 2.73 2.71 7.15 2.71 9.88 0C18.32 15.65 19 14.08 19 12.1h2c0 1.98-.88 4.55-2.64 6.29-3.51 3.48-9.21 3.48-12.72 0-3.5-3.47-3.53-9.11-.02-12.58 3.51-3.47 9.14-3.47 12.65 0L21 3v7.12zM12.5 8v4.25l3.5 2.08-.72 1.21L11 13V8h1.5z"}]
						   [:path {:d "M0 0h24v24H0V0z" :fill "none"}]]
   :advanced-image        [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
						   [:path {:d "M4 8h4V4H4v4zm6 12h4v-4h-4v4zm-6 0h4v-4H4v4zm0-6h4v-4H4v4zm6 0h4v-4h-4v4zm6-10v4h4V4h-4zm-6 4h4V4h-4v4zm6 6h4v-4h-4v4zm0 6h4v-4h-4v4z"}]
						   [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :dashboard-image       [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
						   [:path {:d "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"}]
						   [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :shutdown-image        [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
						   [:path {:d "M13 3h-2v10h2V3zm4.83 2.17l-1.42 1.42C17.99 7.86 19 9.81 19 12c0 3.87-3.13 7-7 7s-7-3.13-7-7c0-2.19 1.01-4.14 2.58-5.42L6.17 5.17C4.23 6.82 3 9.26 3 12c0 4.97 4.03 9 9 9s9-4.03 9-9c0-2.74-1.23-5.18-3.17-6.83z"}]
						   [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :configure-wan-image   [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
						   [:path {:d "M7,15H9V18H11V15H13V18H15V15H17V18H19V9H15V6H9V9H5V18H7V15M4.38,3H19.63C20.94,3 22,4.06 22,5.38V19.63A2.37,2.37 0 0,1 19.63,22H4.38C3.06,22 2,20.94 2,19.63V5.38C2,4.06 3.06,3 4.38,3Z"}]]
   :initiate-upload-image [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
						   [:path {:d "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"}]
						   [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :deselect-all-image    [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
						   [:path {:d "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"}]
						   [:path {:d "M0 0h24v24H0z" :fill "none"}]]})
