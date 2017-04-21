(defproject baller "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/core.async  "0.3.442"
                  :exclusions [org.clojure/tools.reader]]
                 [infinitelives/infinitelives.pixi "0.1.1-SNAPSHOT"]
                 [infinitelives/infinitelives.utils "0.1.1-SNAPSHOT"]]

  :plugins [[lein-figwheel "0.5.10"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"
                 "checkouts/infinitelives.pixi/src"
                 "checkouts/infinitelives.utils/src"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"
                               "checkouts/infinitelives.pixi/src"
                               "checkouts/infinitelives.utils/src"]
                :figwheel {:on-jsload "baller.core/on-js-reload"
                           :open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main baller.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/baller.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"
                               "checkouts/infinitelives.pixi/src"
                               "checkouts/infinitelives.utils/src"]
                :compiler {:output-to "build/js/compiled/baller.js"
                           :main baller.core
                           :optimizations :simple
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]
              :server-logfile false}


  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.2"]
                                  [figwheel-sidecar "0.5.10"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
