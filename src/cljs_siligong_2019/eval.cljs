(ns cljs-siligong-2019.eval
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.tools.reader :refer [read-string]]
            [cljs.js :refer [empty-state eval js-eval]]
            [cljs.env :refer [*compiler*]]
            [cljs.pprint :refer [pprint]]))

(defn eval-str [s]
  (eval (empty-state)
        (read-string s)
        {:eval       js-eval
         :source-map true
         :context    :expr}
        (fn [result] result)))

(defn editor-did-mount [state repl-key]
  (fn [this]
    (let [cm (.fromTextArea  js/CodeMirror
                             (reagent/dom-node this)
                             #js {:mode "clojure"
                                  :lineNumbers true})]
      (.on cm "change" #(swap! state assoc-in [:repls repl-key :input] (.getValue %))))))

(defn editor [state repl-key]
  (reagent/create-class
   {:render (fn [] [:textarea
                            {:default-value ""
                             :auto-complete "off"}])
    :component-did-mount (editor-did-mount state repl-key)}))

(defn render-code [this]
  (->> this reagent/dom-node (.highlightBlock js/hljs)))

(defn result-view [state repl-key]
  (reagent/create-class
   {:render (fn []
              [:pre>code.clj
               (with-out-str (pprint (get-in @state [:repls repl-key :output])))])
    :component-did-update render-code}))

(defn compile-button [state repl-key]
  [:div
   [:button
    {:on-click #(swap! state
                       assoc-in
                       [:repls repl-key :output]
                       (eval-str (get-in @state [:repls repl-key :input])))}
    "run"]])

(defn whole-component [state repl-key]
  (fn [& _]
    [:div
     [:link {:rel "stylesheet"
             :href "//cdnjs.cloudflare.com/ajax/libs/codemirror/5.8.0/codemirror.min.css"}]
     [:link {:rel "stylesheet"
             :href "//cdnjs.cloudflare.com/ajax/libs/highlight.js/8.9.1/styles/default.min.css"}]
     [:script {:src "//cdnjs.cloudflare.com/ajax/libs/highlight.js/8.9.1/highlight.min.js"}]
     [:script {:src "//cdnjs.cloudflare.com/ajax/libs/codemirror/5.8.0/codemirror.min.js"}]
     [:script {:src "//cdnjs.cloudflare.com/ajax/libs/codemirror/5.8.0/mode/clojure/clojure.min.js"}]
     [editor state repl-key]
     (compile-button state repl-key)
     [:div
      [result-view state repl-key]]]))
