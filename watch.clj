(require 'cljs.build.api)

(cljs.build.api/watch "src"
	{:main 'clj-dashboard-frontend.core
	 :output-to "js/clj_dashboard_frontend.js"})
