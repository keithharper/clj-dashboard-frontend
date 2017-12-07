(defproject clj-dashboard-frontend "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License - v 1.0"
			:url  "http://www.eclipse.org/legal/epl-v10.html"}

  :main dashboard.handler
  :source-paths ["src/clj"]
  :cljsbuild {:builds [{:id           "dev"
						:source-paths ["src/clj" "src/cljs"]
						:figwheel     true
						:compiler
									  {:optimizations :none
									   :output-to     "dev-resources/public/js/compiled/dashboard.js"
									   :output-dir    "dev-resources/public/js/"
									   :pretty-print  true
									   :source-map    true
									   :preloads      [devtools.preload]}}
					   {:id           "min"
						:source-paths ["src/cljs"]
						:compiler     {:output-to     "resources/public/js/compiled/dashboard.js"
									   :main          dashboard.core
									   :optimizations :advanced
									   :pretty-print  false}}]}

  :figwheel {:ring-handler dashboard.handler/app
			 :css-dirs     ["dev-resources/css/styles.css"]}

  :plugins [[lein-cljsbuild "1.1.7"]
			[lein-figwheel "0.5.13"]]

  :dependencies [[org.clojure/clojure "1.8.0"]
				 [org.clojure/clojurescript "1.9.908"]
				 [re-frame "0.10.3-alpha1"]
				 ;[ring "1.6.2" :exclusions [org.clojure/tools.namespace org.clojure/java.classpath]]
				 [ring/ring-core "1.6.0"]
				 [ring/ring-jetty-adapter "1.6.0"]
				 [ring/ring-defaults "0.3.1" :exclusions [ring/ring-core]]
				 [compojure "1.5.1" :exclusions [ring/ring-core]]
				 [cljs-http "0.1.43" :exclusions [commons-codec]]
				 [reagent "0.7.0"]
				 [binaryage/devtools "0.9.4"]
				 [day8.re-frame/http-fx "0.1.4"]
				 [bk/ring-gzip "0.2.1"]]

  :profiles {:dev     {:dependencies [[com.cemerick/piggieback "0.2.2"]
									  [figwheel-sidecar "0.5.13"]]
					   :source-paths ["src/clj" "src/cljs"]}
			 :uberjar {:aot :all}}     ;; <-- Note the addition of "dev"

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
