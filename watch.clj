(require 'cljs.build.api)

(cljs.build.api/watch "src"
	{:main 'clj-dashboard-frontend.core
	 :output-to "js/dashboard.js"})
