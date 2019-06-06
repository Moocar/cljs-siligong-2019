(defproject cljs-siligong-2019 "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1-RC1"]
                 [org.clojure/clojurescript "1.10.520"]
                 [org.clojure/core.async "0.4.490"]
                 [reagent "0.8.1"]
                 [fipp "0.6.18"]
                 [org.clojars.mysterysal/jobim "2.0.0"]
                 [org.clojars.mysterysal/jobim-figwheel "0.1.0"]]

  :profiles {:dev {:dependencies [[lein-figwheel "0.5.18"]
                                  [cider/piggieback "0.4.1"]
                                  [figwheel-sidecar "0.5.18"]]
                   :source-paths ["cljs_src"]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}}

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.18"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src" "test" "cljs_src"]
                :figwheel true
                :compiler {:output-to "resources/public/js/compiled/show.js"
                           :output-dir "resources/public/js/compiled/out"
                           :asset-path "js/compiled/out"
                           :main cljs-siligong-2019.show-test
                           :source-map true
                           :cache-analysis true}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/show.min.js"
                           :main cljs-siligong-2019.show
                           :optimizations :whitespace
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]})
